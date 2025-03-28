package com.gnome.gnome.s3;

import com.gnome.gnome.exceptions.S3ActionException;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Publisher;
import software.amazon.awssdk.services.s3.waiters.S3AsyncWaiter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * S3Actions is a utility class that encapsulates asynchronous operations on Amazon S3.
 * It provides methods to create/delete buckets, upload/download files, perform multipart uploads,
 * list objects/buckets
 */
public class S3Actions {

    private static final Logger logger = Logger.getLogger(S3Actions.class.getName());
    // Shared S3AsyncClient instance.
    private static S3AsyncClient s3AsyncClient;

    /**
     * Returns a singleton instance of S3AsyncClient.
     * If the client is not yet initialized, it creates a new one using Netty's async HTTP client.
     *
     * @return S3AsyncClient instance.
     */
    public static S3AsyncClient getS3AsyncClient() {
        if (s3AsyncClient == null) {

            SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                    .maxConcurrency(50)
                    .connectionTimeout(Duration.ofSeconds(60))
                    .readTimeout(Duration.ofSeconds(60))
                    .writeTimeout(Duration.ofSeconds(60))
                    .build();

            ClientOverrideConfiguration overrideConfig = ClientOverrideConfiguration.builder()
                    .apiCallTimeout(Duration.ofMinutes(2))
                    .apiCallAttemptTimeout(Duration.ofSeconds(90))
                    .retryStrategy(RetryMode.STANDARD)
                    .build();

            s3AsyncClient = S3AsyncClient.builder()
                    .region(Region.EU_CENTRAL_1)
                    .httpClient(httpClient)
                    .overrideConfiguration(overrideConfig)
                    .build();
        }
        return s3AsyncClient;
    }

    /**
     * Shuts down the S3AsyncClient, releasing its underlying resources.
     * Always call this method when the client is no longer needed.
     */
    public static void shutdownS3Client() {
        if (s3AsyncClient != null) {
            s3AsyncClient.close();
            s3AsyncClient = null;
            logger.info("S3AsyncClient has been shut down.");
        }
    }

    /**
     * Creates an S3 bucket asynchronously.
     *
     * @param bucketName the name of the S3 bucket to create
     * @return a {@link CompletableFuture} that completes when the bucket is created and ready
     * @throws RuntimeException if there is a failure while creating the bucket
     */
    public CompletableFuture<Void> createBucketAsync(String bucketName) {
        CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();

        CompletableFuture<CreateBucketResponse> response = getS3AsyncClient().createBucket(bucketRequest);
        return response.thenCompose(resp -> {
            S3AsyncWaiter s3Waiter = getS3AsyncClient().waiter();
            HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            CompletableFuture<WaiterResponse<HeadBucketResponse>> waiterResponseFuture =
                    s3Waiter.waitUntilBucketExists(bucketRequestWait);
            return waiterResponseFuture.thenAccept(waiterResponse -> {
                waiterResponse.matched().response().ifPresent(headBucketResponse -> {
                    logger.info(bucketName + " is ready");
                });
            });
        }).whenComplete((resp, ex) -> {
            if (ex != null) {
                throw new S3ActionException("Failed to create bucket", ex);
            }
        });
    }

    /**
     * Uploads a local file to an AWS S3 bucket asynchronously.
     *
     * @param bucketName the name of the S3 bucket to upload the file to
     * @param key        the key (object name) to use for the uploaded file
     * @param objectPath the local file path of the file to be uploaded
     * @return a {@link CompletableFuture} that completes with the {@link PutObjectResponse} when the upload is successful, or throws a {@link RuntimeException} if the upload fails
     */
    public CompletableFuture<PutObjectResponse> uploadLocalFileAsync(String bucketName, String key, String objectPath) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        CompletableFuture<PutObjectResponse> response = getS3AsyncClient().putObject(objectRequest, AsyncRequestBody.fromFile(Paths.get(objectPath)));
        return response.whenComplete((resp, ex) -> {
            if (ex != null) {
                throw new S3ActionException("Failed to upload file", ex);
            }
        });
    }

    /**
     * Asynchronously retrieves the bytes of an object from an Amazon S3 bucket and writes them to a local file.
     *
     * @param bucketName the name of the S3 bucket containing the object
     * @param keyName    the key (or name) of the S3 object to retrieve
     * @param path       the local file path where the object's bytes will be written
     * @return a {@link CompletableFuture} that completes when the object bytes have been written to the local file
     */
    public CompletableFuture<Void> getObjectBytesAsync(String bucketName, String keyName, String path) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .key(keyName)
                .bucket(bucketName)
                .build();

        CompletableFuture<ResponseBytes<GetObjectResponse>> response = getS3AsyncClient().getObject(objectRequest, AsyncResponseTransformer.toBytes());
        return response.thenAccept(objectBytes -> {
            try {
                byte [] data = objectBytes.asByteArray();
                Path filePath = Paths.get(path);
                Files.write(filePath, data);
                logger.info("Successfully obtained bytes from an S3 object");
            } catch (IOException ex) {
                throw new RuntimeException("Failed to write data to file", ex);
            }
        }).whenComplete((resp, ex) -> {
            if (ex != null)
                throw new S3ActionException("Failed to get object bytes from S3", ex);
        });
    }

    /**
     * Asynchronously lists all objects in the specified S3 bucket.
     *
     * @param bucketName the name of the S3 bucket to list objects for
     * @return a {@link CompletableFuture} that completes when all objects have been listed
     */
    public CompletableFuture<Void> listAllObjectsAsync(String bucketName) {
        ListObjectsV2Request initialRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .maxKeys(1)
                .build();

        ListObjectsV2Publisher paginator = getS3AsyncClient().listObjectsV2Paginator(initialRequest);
        return paginator.subscribe(response -> {
            response.contents().forEach(s3Object -> {
                logger.info("Object key: " + s3Object.key());
            });
        }).thenRun(() -> {
            logger.info("Successfully listed all objects in the bucket: " + bucketName);
        }).exceptionally(ex -> {
            throw new S3ActionException("Failed to list objects", ex);
        });
    }

    /**
     * Performs a multipart upload to an Amazon S3 bucket.
     *
     * @param bucketName the name of the S3 bucket to upload the file to
     * @param key        the key (name) of the file to be uploaded
     * @return a {@link CompletableFuture} that completes when the multipart upload is successful
     */
    public CompletableFuture<Void> multipartUpload(String bucketName, String key) {
        int mB = 1024 * 1024;

        CreateMultipartUploadRequest createMultipartUploadRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return getS3AsyncClient().createMultipartUpload(createMultipartUploadRequest)
                .thenCompose(createResponse -> {
                    String uploadId = createResponse.uploadId();
                    System.out.println("Upload ID: "+ uploadId);

                    // Upload part 1
                    UploadPartRequest uploadPartRequest1 = UploadPartRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .uploadId(uploadId)
                            .partNumber(1)
                            .contentLength((long) (5 * mB))
                            .build();

                    CompletableFuture<CompletedPart> part1Future = getS3AsyncClient().uploadPart(uploadPartRequest1,
                                AsyncRequestBody.fromByteBuffer(getRandomByteBuffer(5 * mB)))
                            .thenApply(uploadPartResponse -> CompletedPart.builder()
                                    .partNumber(1)
                                    .eTag(uploadPartResponse.eTag())
                                    .build());

                    UploadPartRequest uploadPartRequest2 = UploadPartRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .partNumber(2)
                            .contentLength((long) (3 * mB))
                            .build();

                    CompletableFuture<CompletedPart> part2Future = getS3AsyncClient().uploadPart(uploadPartRequest2,
                                AsyncRequestBody.fromByteBuffer(getRandomByteBuffer(3 * mB)))
                            .thenApply(uploadPartResponse -> CompletedPart.builder()
                                    .partNumber(2)
                                    .eTag(uploadPartResponse.eTag())
                                    .build());

                    return CompletableFuture.allOf(part1Future, part2Future)
                            .thenCompose(v -> {
                                CompletedPart part1 = part1Future.join();
                                CompletedPart part2 = part2Future.join();

                                CompletedMultipartUpload completedMultipartUpload = CompletedMultipartUpload.builder()
                                        .parts(part1, part2)
                                        .build();

                                CompleteMultipartUploadRequest completeMultipartUploadRequest = CompleteMultipartUploadRequest.builder()
                                        .bucket(bucketName)
                                        .key(key)
                                        .uploadId(uploadId)
                                        .multipartUpload(completedMultipartUpload)
                                        .build();

                                return getS3AsyncClient().completeMultipartUpload(completeMultipartUploadRequest);
                            });
                })
                .thenAccept(response -> System.out.println("Multipart upload completed successfully"))
                .exceptionally(ex -> {
                    System.err.println("Failed to complete multipart upload: " + ex.getMessage());
                    throw new S3ActionException(ex.getMessage());
                });
    }

    private static ByteBuffer getRandomByteBuffer(int size) {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        for (int i = 0;i < size;i++)
            buffer.put((byte) (Math.random() * 256));
        buffer.flip();
        return buffer;
    }

    /**
     * Deletes an object from an S3 bucket asynchronously.
     *
     * @param bucketName the name of the S3 bucket
     * @param key        the key (file name) of the object to be deleted
     * @return a {@link CompletableFuture} that completes when the object has been deleted
     */
    public CompletableFuture<Void> deleteObjectFromBucketAsync(String bucketName, String key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        CompletableFuture<DeleteObjectResponse> response = getS3AsyncClient().deleteObject(deleteObjectRequest);
        response.whenComplete((deleteRes, ex) -> {
            if (deleteRes != null)
                logger.info(key + " was deleted");
            else
                throw new S3ActionException("An S3 exception occurred during delete", ex);
        });
        return response.thenApply(r -> null);
    }

    /**
     * Deletes an S3 bucket asynchronously.
     *
     * @param bucket the name of the bucket to be deleted
     * @return a {@link CompletableFuture} that completes when the bucket deletion is successful, or throws a {@link RuntimeException}
     * if an error occurs during the deletion process
     */
    public CompletableFuture<Void> deleteBucketAsync(String bucket) {
        DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder()
                .bucket(bucket)
                .build();

        CompletableFuture<DeleteBucketResponse> response = getS3AsyncClient().deleteBucket(deleteBucketRequest);
        response.whenComplete((deleteRes, ex) -> {
            if (deleteRes != null)
                logger.info(bucket + " was deleted.");
            else
                throw new S3ActionException("An S3 exception occurred during bucket deletion", ex);
        });
        return response.thenApply(r -> null);
    }
}
