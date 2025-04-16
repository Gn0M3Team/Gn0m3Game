package com.gnome.gnome.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * Utility class for parsing and splitting images into smaller tiles.
 * <p>
 * This class reads an image from the resources and splits it into
 * 16x16 pixel tiles, saving them individually into the specified directory.
 * <p>
 * Note: If the source image's dimensions are not exact multiples of 16,
 * the rightmost and bottommost partial tiles are ignored.
 */
public class ImageParser {
    // Logger for logging info and warnings.
    private static final Logger logger = Logger.getLogger(ImageParser.class.getName());

    // Constants for the tile dimensions.
    private static final int TILE_WIDTH = 16;
    private static final int TILE_HEIGHT = 16;

    /**
     * Splits the specified image into 16x16 pixel tiles and saves them to
     * the {@code src/main/resources/com/gnome/gnome/images/tiles} directory.
     * <p>
     * The image must be located in the resource path relative to this class.
     *
     * @param imageName the path to the image resource (e.g., "/images/sprite.png")
     * @throws IOException if the image cannot be found or an error occurs while reading or writing files
     */
    public static void splitImage(String imageName) throws IOException {
        // Open the image resource as an InputStream using this class's resource loader.
        try (InputStream stream = ImageParser.class.getResourceAsStream("/com/gnome/gnome/images/colored_packed.png")) {
            if (stream == null) {
                logger.warning("Image not found:" + imageName);
                System.out.println("❌ Image not found: " + imageName);
                return;
            }

            BufferedImage source = ImageIO.read(stream);
            int imageWidth = source.getWidth();
            int imageHeight = source.getHeight();

            logger.info("Loaded image size: " + imageWidth + "x" + imageHeight);
            System.out.println("Loaded image size: " + imageWidth + "x" + imageHeight);

            int cols = imageWidth / TILE_WIDTH;
            int rows = imageHeight / TILE_HEIGHT;

            // Define the output directory where the tiles will be saved.
            File outputDir = new File("src/main/resources/com/gnome/gnome/images/tiles");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            int idx = 0; // Counter for naming the tiles.
            // Iterate over each tile position.
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    // Extract a subimage representing a single tile.
                    BufferedImage tile = source.getSubimage(
                            x * TILE_WIDTH,
                            y * TILE_HEIGHT,
                            TILE_WIDTH,
                            TILE_HEIGHT
                    );
                    // Define the output file for the current tile.
                    File output = new File(outputDir, "tile_" + idx++ + ".png");
                    // Write the tile image as a PNG file.
                    ImageIO.write(tile, "png", output);
                }
            }
            logger.info("Splitting done! Total tiles: " + idx);
            System.out.println("Splitting done! Total tiles: " + idx);
        }
        catch (Exception e) {
            System.out.println("❌ Failed to parse image: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
