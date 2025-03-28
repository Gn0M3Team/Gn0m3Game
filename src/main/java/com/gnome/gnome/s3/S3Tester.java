package com.gnome.gnome.s3;

import software.amazon.awssdk.services.omics.model.SequenceStoreS3Access;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Interactive tester for performing various AWS S3 operations asynchronously.
 * <p>
 * This class simulates a step-by-step tutorial to demonstrate:
 * <ul>
 *     <li>Creating a bucket</li>
 *     <li>Uploading and downloading files</li>
 *     <li>Multipart uploads</li>
 *     <li>Listing objects</li>
 *     <li>Deleting objects and the bucket itself</li>
 * </ul>
 *
 * <p>All steps are executed interactively with user confirmation between actions.
 */
public class S3Tester {
    public static Scanner scanner = new Scanner(System.in);
    private final S3Actions s3Actions = new S3Actions();
    public static final String DASHES = new String(new char[80]).replace("\0", "-");
    private static final Logger logger = Logger.getLogger(S3Tester.class.getName());

    /**
     * Starts the interactive scenario for demonstrating AWS S3 functionality.
     *
     * @throws IOException if an I/O error occurs
     */
    public void starter() throws IOException {
        final String usage = """
                Usage:
                    <bucketName> <key> <objectPath> <savePath> <toBucket>
                Where:
                    bucketName - The name fo the S3 bucket.
                    key - The unique identifier for the object stored in the S3 bucket.
                    objectPath - The full file path of the object within the S3 bucket (e.g., "documents/reports/annual_report.pdf").
                    savePath - The local file path where the object will be downloaded and saved (e.g., "C:/Users/username/Downloads/annual_report.pdf").
                    toBucket - The name of the S3 bucket to which the object will be copied.
                """;

        logger.info(usage);

        String bucketName = "";
        String key = "";
        String objectPath = "";
        String savePath = "";
        String toBucket = "";

        logger.info(DASHES);
        logger.info("Welcome to the AWS S3 example");
        logger.info("This scenario walks you through how to perform key operations for this service.");
        waitForInputToContinue(scanner);
        logger.info(DASHES);

        try {
            runScenario(bucketName, key, objectPath, savePath, toBucket);

        } catch (Throwable rt) {
            Throwable cause = rt.getCause();
            if (cause instanceof S3Exception kmsEx)
                logger.info("KMS error occurred: Error message: " + kmsEx.getMessage() + ", Error code: " + kmsEx.awsErrorDetails());
            else
                logger.info("An unexpected error occurred: " + rt.getMessage());
        }
    }

    /**
     * Waits for the user to press 'c' followed by ENTER to proceed.
     *
     * @param scanner Scanner for reading user input
     */
    private void waitForInputToContinue(Scanner scanner) {
        while(true) {
            logger.info("");
            logger.info("Enter 'c' followed by <ENTER> to continue:");
            String input = scanner.nextLine();

            if (input.trim().equalsIgnoreCase("c")) {
                logger.info("Continuing with the program...");
                logger.info("");
                break;
            } else {
                logger.info("Invalid input. Please try again.");
            }
        }
    }

    /**
     * Runs the full AWS S3 scenario including all key operations.
     *
     * @param bucketName  the name of the S3 bucket
     * @param key         the object key
     * @param objectPath  the local file path of the object to upload
     * @param savePath    the local path to save the downloaded file
     * @param toBucket    the target bucket for copy operation
     * @throws Throwable if an exception occurs during S3 operations
     */
    private void runScenario(String bucketName, String key, String objectPath, String savePath, String toBucket) throws Throwable {
        logger.info(DASHES);
        logger.info("1. Create AWS S3 bucket");
        try {
            CompletableFuture<Void> future = s3Actions.createBucketAsync(bucketName);
            future.join();
            waitForInputToContinue(scanner);
        } catch (RuntimeException rt) {
            logS3Exception(rt);
            throw rt.getCause();

        }

        logger.info(DASHES);

        logger.info(DASHES);
        logger.info("2. Upload a local file to the AWS S3 bucket.");
        waitForInputToContinue(scanner);
        try {
            CompletableFuture<PutObjectResponse> future = s3Actions.uploadLocalFileAsync(bucketName, key, objectPath);
            future.join();
            logger.info("File uploaded successfully to " + bucketName + "/" + key);

        } catch (RuntimeException rt) {
            logS3Exception(rt);
            throw rt.getCause();
        }
        waitForInputToContinue(scanner);
        logger.info(DASHES);

        logger.info(DASHES);
        logger.info("3. Download the object to another local file.");
        waitForInputToContinue(scanner);
        try {
            CompletableFuture<Void> future = s3Actions.getObjectBytesAsync(bucketName, key, savePath);
            future.join();
            logger.info("Successfully obtained bytes from S3 object and wrote to file " + savePath);

        } catch (RuntimeException rt) {
            logS3Exception(rt);
            throw rt.getCause();
        }
        waitForInputToContinue(scanner);
        logger.info(DASHES);

        logger.info(DASHES);
        logger.info("4. Perform a multipart upload.");
        waitForInputToContinue(scanner);
        String multipartKey = "multiPartKey";
        try {
            CompletableFuture<Void> future = s3Actions.multipartUpload(bucketName, multipartKey);
            future.join();
            logger.info("Multipart upload completed successfully for bucket '" + bucketName + "' and key '" + multipartKey + "'");

        } catch (RuntimeException rt) {
            logS3Exception(rt);
            throw rt.getCause();
        }
        waitForInputToContinue(scanner);
        logger.info(DASHES);

        logger.info(DASHES);
        logger.info("5. List all objects located in the AWS S3 bucket.");
        waitForInputToContinue(scanner);
        try {
            CompletableFuture<Void> future = s3Actions.listAllObjectsAsync(bucketName);
            future.join();
            logger.info("Object listing completed successfully.");

        } catch (RuntimeException rt) {
            logS3Exception(rt);
            throw rt.getCause();
        }

        waitForInputToContinue(scanner);
        logger.info(DASHES);

        logger.info(DASHES);
        logger.info("8. Delete objects from the Amazon S3 bucket.");
        waitForInputToContinue(scanner);
        try {
            CompletableFuture<Void> future = s3Actions.deleteObjectFromBucketAsync(bucketName, key);
            future.join();

        } catch (RuntimeException rt) {
            logS3Exception(rt);
            throw rt.getCause();
        }

        try {
            CompletableFuture<Void> future = s3Actions.deleteObjectFromBucketAsync(bucketName, "multiPartKey");
            future.join();

        } catch (RuntimeException rt) {
            logS3Exception(rt);
            throw rt.getCause();
        }
        waitForInputToContinue(scanner);
        logger.info(DASHES);

        logger.info(DASHES);
        logger.info("9. Delete the Amazon S3 bucket.");
        waitForInputToContinue(scanner);
        try {
            CompletableFuture<Void> future = s3Actions.deleteBucketAsync(bucketName);
            future.join();
        } catch (RuntimeException rt) {
            logS3Exception(rt);
            throw rt.getCause();
        }

        waitForInputToContinue(scanner);
        logger.info(DASHES);

        logger.info(DASHES);
        logger.info("You successfully completed scenario.");
        logger.info(DASHES);
    }

    /**
     * Logging an exception that came from S3
     * @param t
     */
    private static void logS3Exception(Throwable t) {
        Throwable cause = t.getCause();
        if (cause instanceof S3Exception s3Ex) {
            logger.warning("S3 error occurred: Message = " + s3Ex.getMessage()
                    + ", Code = " + s3Ex.awsErrorDetails().errorCode());
        } else {
            logger.warning("Unexpected error occurred: " + t.getMessage());
        }
    }
}
