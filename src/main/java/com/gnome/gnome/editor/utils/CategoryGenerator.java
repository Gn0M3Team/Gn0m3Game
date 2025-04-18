package com.gnome.gnome.editor.utils;

import com.gnome.gnome.annotations.config.Value;
import com.gnome.gnome.s3.S3Actions;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.Setter;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


/**
 * CategoryGenerator class that can generate category items in two ways:
 * <ul>
 *   <li>If {@code test} is true, it returns a list of simple String items from a local map.</li>
 *   <li>If {@code test} is false, it retrieves image-based items from an S3 bucket,
 *       downloads them locally, and converts them into {@link ImageView} objects.</li>
 * </ul>
 *
 * <p>Efficiency improvements:
 * <ul>
 *   <li>Caching: Already downloaded images are stored in a cache to avoid duplicate downloads.</li>
 * </ul>
 */
@Getter
@Setter
public class CategoryGenerator {

    // Logger for logging events and errors.
    private static final Logger logger = Logger.getLogger(CategoryGenerator.class.getName());

    // Flag to determine whether to use test data (strings) or real S3 images.
    @Value("app.is_test")
    private boolean test;

    // Local categories for test = true
    private final Map<String, List<String>> localCategoryMap = Map.of(
            "Monsters",     List.of("Demon", "Butterfly", "Goblin", "Scorpion", "Skeleton"),
            "Props",        List.of("Tree", "Rock", "Hatch"),
            "NPCs",         List.of("Villager", "Merchant"),
            "Environment",  List.of("Mountain", "River", "Floor", "WallOne", "WallTwo")
    );

    // Reference to your S3 utility class
    private final S3Actions s3Actions = new S3Actions();

    // Name of the S3 bucket (adjust as needed)
    private static final String BUCKET_NAME = "my-example-bucket";

    // A thread-safe cache to store downloaded ImageViews keyed by the S3 object key.
    private final Map<String, ImageView> imageCache = new ConcurrentHashMap<>();

    /**
     * Returns the items for a given category.
     * <p>
     * If {@code test} is true, returns a list of local strings for the category.
     * Otherwise, it asynchronously retrieves images from S3 and returns a list of {@link ImageView} objects.
     *
     * @param category The category name, e.g. "Monsters".
     * @return a CompletableFuture that completes with a List of either Strings or ImageViews.
     */
    public CompletableFuture<List<Object>> getItemsForCategory(String category) {
        if (test) {
            // Return a copy of the local list so modifications won't affect the original map.
            List<String> items  = localCategoryMap.getOrDefault(category, Collections.emptyList());
            return CompletableFuture.completedFuture(new ArrayList<>(items));
        } else {
            // Retrieve images from S3.
            return loadImagesFromS3(category);
        }
    }

    /**
     * Loads images for a given category from S3.
     * <p>
     * The method:
     * <ol>
     *   <li>Constructs a prefix from the category (e.g. "Monsters/").</li>
     *   <li>Sends a ListObjectsV2Request to list objects under that prefix.</li>
     *   <li>For each object found, downloads the file to a temporary location and creates an ImageView.</li>
     *   <li>Combines all the asynchronous operations and returns a list of ImageView objects.</li>
     * </ol>
     *
     * @param category The category name.
     * @return a CompletableFuture that completes with a List of ImageView objects.
     */
    private CompletableFuture<List<Object>> loadImagesFromS3(String category) {
        // Construct the prefix for S3 objects (e.g., "Monsters/").
        String prefix = category + "/";

        // Create a request to list objects in the S3 bucket under the given prefix.
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(BUCKET_NAME)
                .prefix(prefix)
                .build();

        // Asynchronously list objects from S3.
        return S3Actions.getS3AsyncClient().listObjectsV2(request)
                .thenCompose((ListObjectsV2Response response) -> {
                    // If no objects are found, log and return an empty list.
                    if (response.contents().isEmpty()) {
                        logger.info("No objects found in S3 for prefix: " + prefix);
                        return CompletableFuture.completedFuture(Collections.emptyList());
                    }

                    // Create a list to hold futures for each ImageView creation.
                    List<CompletableFuture<ImageView>> futureImageViews = new ArrayList<>();

                    // For each S3 object, download and create an ImageView.
                    response.contents().forEach(s3Object -> {
                        String key = s3Object.key();

                        // Check if the image is already cached.
                        if (imageCache.containsKey(key)) {
                            // Use the cached ImageView.
                            futureImageViews.add(CompletableFuture.completedFuture(imageCache.get(key)));
                        } else {
                            // Proceed with download, creation, and then cache the ImageView.
                            String localFileName = "temp/" + key.replace("/", "_");
                            CompletableFuture<Void> downloadFuture = s3Actions.getObjectBytesAsync(BUCKET_NAME, key, localFileName);
                            CompletableFuture<ImageView> imageViewFuture = downloadFuture.thenApply((Void v) -> {
                                try (FileInputStream fis = new FileInputStream(new File(localFileName))) {
                                    Image image = new Image(fis);
                                    ImageView imageView = new ImageView(image);
                                    imageView.setFitWidth(64);
                                    imageView.setPreserveRatio(true);
                                    // Cache the image for future use.
                                    imageCache.put(key, imageView);
                                    return imageView;
                                } catch (Exception e) {
                                    logger.warning("Failed to create ImageView for " + key + ": " + e.getMessage());
                                    return null;
                                }
                            });
                            futureImageViews.add(imageViewFuture);
                        }
                    });

                    // Combine all ImageView futures into one.
                    CompletableFuture<Void> allDone = CompletableFuture.allOf(
                            futureImageViews.toArray(new CompletableFuture[0])
                    );

                    // Once all downloads and ImageView creations are complete, collect the results.
                    return allDone.thenApply(ignored -> {
                        List<Object> imageViews = new ArrayList<>();
                        for (CompletableFuture<ImageView> f : futureImageViews) {
                            ImageView iv = f.join();
                            if (iv != null)
                                imageViews.add(iv);
                        }
                        return imageViews;
                    });
                });
    }

}
