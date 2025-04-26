package com.gnome.gnome.camera;
import com.gnome.gnome.continueGame.component.Coin;
import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.player.Player;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;

/**
 * The Camera class is responsible for rendering a 15x15 viewport of the map,
 * centered around a specific point, typically the player's position.
 * It only shows a portion of the full map to simulate a camera view.
 */
@Data
public class Camera {
    private int[][] mapGrid; // A two-dimensional array representing the game card. Each element of the array is an integer,
                             // corresponding to a particular type of tile (for example, 0 for floor, -1 for goblin, etc.)
    private int cameraCenterX; //    X-coordinate of the camera centre (in tiles, not pixels). This is usually the player's X position
    private int cameraCenterY; // Y coordinate of the camera centre (in tiles). This is usually the player's Y position
    private final int viewportSize = 15; // The size of the viewport in tiles. The camera always displays a 15x15 tile square
                                         // This means that we see 15 tiles in width and 15 in height
    private int startRow; // The starting line (Y) of the map, from which we start drawing tiles in the preview window
    private int startCol; // The initial column (X) of the map from which we start drawing tiles in the preview window
    private Player player; // A reference to the Player object. We use it to know where the player is,
                           // and centre the camera on his position, as well as highlight his tile on the map

    // A static variable for caching images:
    private static final Map<String, Image> imageCache = new HashMap<>();   // Cache for images. We store tile images
                                                                            // (e.g. floor texture) in this dictionary (Map),
                                                                            // so that we don't have to load them from files every time we draw a map.
                                                                            // The key is the path to the image (String), the value is the Image object itself

    /**
     * Constructor of the Camera class. Used to create a new Camera object.
     *
     * @param fieldMap A two-dimensional array representing the game map (with tiles, monsters, etc.).
     * @param cameraCenterX The initial X coordinate of the camera centre (usually the player's position).
     * @param cameraCenterY Initial Y coordinate of the camera centre (usually the player's position).
     * @param player A player object so we can keep track of the player's position.
     */
    public Camera(int[][] fieldMap, int cameraCenterX, int cameraCenterY, Player player) {
        this.mapGrid = fieldMap; // Initialise the map passed as a parameter
        this.cameraCenterX = cameraCenterX; // Set the initial X-coordinate of the camera centre
        this.cameraCenterY = cameraCenterY; // Set the initial Y-coordinate of the camera center
        this.player = player; // Save the reference to the player object
    }


    /**
     * The drawViewport method is responsible for drawing the visible part of the map (the 15x15 tile viewport) on the Canvas.
     * It also draws the coins that are in the visible area and highlights the player's tile with a yellow border.
     *
     * @param canvas The Canvas object on which to draw the map (this is the area in JavaFX where you can draw graphics).
     * @param coins A list of coins to display if they are in the visible area.
     */
    public void drawViewport(Canvas canvas, List<Coin> coins) {
        // We get a GraphicsContext, which is a tool for drawing on Canvas
        // We use it to draw tiles, frames, coins, etc
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Get the size of the map (number of rows and columns)
        int totalRows = mapGrid.length; // Number of lines in the map
        int totalCols = mapGrid[0].length; // Number of columns in the map

        // Calculate half the size of the viewport (viewportSize / 2)
        // This is necessary to determine which map tiles we will draw (depending on the camera centre)
        int half = viewportSize / 2;

        // Calculate the starting row (startRow) and column (startCol) for drawing
        // We want the centre of the camera (cameraCenterX, cameraCenterY) to be in the centre of the viewport
        // - Math.max(0, ...) ensures that we do not go beyond the map on the left or top (do not draw negative coordinates)
        // - Math.min(..., totalRows - viewportSize) ensures that we don't go beyond the map on the right or bottom
        startRow = Math.max(0, Math.min(cameraCenterY - half, totalRows - viewportSize));
        startCol = Math.max(0, Math.min(cameraCenterX - half, totalCols - viewportSize));

        // Clear the entire Canvas before drawing to remove previous content
        // This prevents old images from overlapping with new ones
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Go through all the tiles in the visible area (15x15) and draw them
        for (int i = 0; i < viewportSize; i++) { // Loop through the lines of the viewport
            for (int j = 0; j < viewportSize; j++) { // Loop through the columns of the viewport
                // Calculate the real coordinates of the tile on the map:
                int row = startRow + i; // Real line on the map
                int col = startCol + j; // Real column on the map

                // Calculate the coordinates in pixels for drawing tiles on Canvas:
                // Each tile has a size of TILE_SIZE x TILE_SIZE (50x50 pixels)
                double x = j * TILE_SIZE; // X position in pixels (for example, 0, 50, 100, ...)
                double y = i * TILE_SIZE; // Y-axis in pixels

                // Check if the tile coordinates are within the map
                if (row >= 0 && row < totalRows && col >= 0 && col < totalCols) {
                    // If the tile is within the map, determine its type
                    // The mapGrid[row][col] contains a number representing the type of tile
                    TypeOfObjects type = TypeOfObjects.fromValue(mapGrid[row][col]);

                    // Fetch the image for this tile type from the cache (or load it if it is not already in the cache)
                    Image tileImage = getCachedImage(type.getImagePath());

                    // If the image is successfully uploaded, draw it on Canvas
                    if (tileImage != null) {
                        // Draw the tile image at the coordinates (x, y) with the size TILE_SIZE x TILE_SIZE
                        gc.drawImage(tileImage, x, y, TILE_SIZE, TILE_SIZE);
                    } else {
                        // If the image fails to load, draw a grey square as a backup
                        gc.setFill(Color.GRAY); // Set the fill colour to grey
                        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE); // Draw a square
                    }
                } else {
                    // If the tile coordinates are outside the map (for example, if the map is smaller than 15x15),
                    // draw a dark grey square to indicate the ‘empty’ area
                    gc.setFill(Color.DARKGRAY);
                    gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }

                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
                gc.strokeRect(x, y, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw coins (Coin) that are in the visible area
        for (Coin coin : coins) {
            // Get the coordinates of the coin on the map (in tiles)
            int gx = coin.getGridX(), gy = coin.getGridY();

            // Check that the coin is in the visible area (15x15 tiles)
            if (gx >= startCol && gx < startCol + viewportSize &&
                    gy >= startRow && gy < startRow + viewportSize) {
                // If the coin is visible, we get its image and dimensions
                Image img = coin.getImageView().getImage(); //  Image of the coin
                double w = coin.getImageView().getFitWidth(); // The width of the coin image
                double h = coin.getImageView().getFitHeight(); // The height of the coin image

                // Calculate the offset to centre the coin image inside the tile
                // For example, if the tile is 50x50 and the coin is 30x30, we shift it by (50-30)/2 = 10 pixels
                double ox = (TILE_SIZE - w) / 2; // X offset
                double oy = (TILE_SIZE - h) / 2; // Y offset

                // Calculate the coordinates of the coin in pixels on Canvas:
                // (gx - startCol) * TILE_SIZE is the position of the coin relative to the beginning of the viewport
                double px = (gx - startCol) * TILE_SIZE + ox; // The final position is X
                double py = (gy - startRow) * TILE_SIZE + oy; // The final position is Y

                // Draw an image of the coin on Canvas
                gc.drawImage(img, px, py, w, h);
            }
        }
    }

    /**
     * Returns a cached image; if it’s not already loaded, loads it from the resource.
     *
     * @param imagePath the path to the image
     * @return the cached Image
     */
    private Image getCachedImage(String imagePath) {
        // Use the computeIfAbsent method for caching:
        // - If the image with this path is already in the imageCache, we return it
        // - If the image is not available, we download it from resources and add it to the cache
        return imageCache.computeIfAbsent(imagePath, path ->
                // Load the image from resources (the file must be in the resources folder)
                // Objects.requireNonNull throws an exception if the image is not found
                new Image(Objects.requireNonNull(
                        TypeOfObjects.class.getResourceAsStream(path)
                )));
    }


    /**
     * Updates the camera center to follow the player's position.
     */
    public void updateCameraCenter() {
        // Update the coordinates of the camera centre, setting them equal to the player's position
        cameraCenterX = player.getX(); // X-coordinate of the player
        cameraCenterY = player.getY(); // Y-coordinate of the player

        // Call the clampCameraCentre method to make sure that the camera does not extend beyond the map
        clampCameraCenter();
    }

    /**
     * Ensures the camera's center stays within the valid map area
     * so that the viewport doesn't go out of bounds.
     */
    private void clampCameraCenter() {
        int half = viewportSize / 2; // Calculate half the size of the viewport (viewportSize / 2)

        int totalCols = mapGrid[0].length; // Number of columns
        int totalRows = mapGrid.length; // Number of lines

        // Bound cameraCenterX so that the viewport does not extend beyond the map:
        // - Math.max(half, ...) ensures that the centre is not too close to the left edge (so as not to show negative coordinates)
        // - Math.min(..., totalCols - viewportSize + half) ensures that the centre is not too close to the right edge
        cameraCenterX = Math.max(half, Math.min(cameraCenterX, totalCols - viewportSize + half));

        // Similarly, we constrain cameraCenterY for the top and bottom edges of the map
        cameraCenterY = Math.max(half, Math.min(cameraCenterY, totalRows - viewportSize + half));
    }

}
