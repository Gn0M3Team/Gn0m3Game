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
    private int[][] mapGrid;
    private int cameraCenterX;
    private int cameraCenterY;
    private final int viewportSize = 15;
    private int startRow;
    private int startCol;
    private Player player;

    private static final Map<String, Image> imageCache = new HashMap<>();

    public Camera(int[][] fieldMap, int cameraCenterX, int cameraCenterY, Player player) {
        this.mapGrid = fieldMap;
        this.cameraCenterX = cameraCenterX;
        this.cameraCenterY = cameraCenterY;
        this.player = player;
    }

    /**
     * Draws the current viewport onto the provided canvas.
     *
     * @param canvas the Canvas to draw on
     */
    public void drawViewport(Canvas canvas, List<Coin> coins) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        int totalRows = mapGrid.length;
        int totalCols = mapGrid[0].length;
        int half = viewportSize / 2;

        // Calculate the starting row and column for the viewport
        startRow = Math.max(0, Math.min(cameraCenterY - half, totalRows - viewportSize));
        startCol = Math.max(0, Math.min(cameraCenterX - half, totalCols - viewportSize));

        // Clear the canvas
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw each visible tile
        for (int i = 0; i < viewportSize; i++) {
            for (int j = 0; j < viewportSize; j++) {
                int row = startRow + i;
                int col = startCol + j;
                double x = j * TILE_SIZE;
                double y = i * TILE_SIZE;

                if (row >= 0 && row < totalRows && col >= 0 && col < totalCols) {
                    // Get tile type and its cached image
                    TypeOfObjects type = TypeOfObjects.fromValue(mapGrid[row][col]);
                    Image tileImage = getCachedImage(type.getImagePath());
                    if (tileImage != null) {
                        gc.drawImage(tileImage, x, y, TILE_SIZE, TILE_SIZE);
                    } else {
                        gc.setFill(Color.GRAY);
                        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    }
                } else {
                    // Draw a dark gray tile for areas outside the map bounds
                    gc.setFill(Color.DARKGRAY);
                    gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }

                // Draw borders, highlighting the player's tile with a yellow border
                if (row == player.getY() && col == player.getX()) {
                    gc.setStroke(Color.YELLOW);
                    gc.setLineWidth(2);
                } else {
                    gc.setStroke(Color.BLACK);
                    gc.setLineWidth(1);
                }
                gc.strokeRect(x, y, TILE_SIZE, TILE_SIZE);
            }
        }

        for (Coin coin : coins) {
            int gx = coin.getGridX(), gy = coin.getGridY();
            if (gx >= startCol && gx < startCol + viewportSize &&
                    gy >= startRow && gy < startRow + viewportSize) {
                Image img = coin.getImageView().getImage();
                double w = coin.getImageView().getFitWidth();
                double h = coin.getImageView().getFitHeight();
                double ox = (TILE_SIZE - w) / 2;
                double oy = (TILE_SIZE - h) / 2;
                double px = (gx - startCol) * TILE_SIZE + ox;
                double py = (gy - startRow) * TILE_SIZE + oy;
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
        return imageCache.computeIfAbsent(imagePath, path ->
                new Image(Objects.requireNonNull(
                        TypeOfObjects.class.getResourceAsStream(path)
                )));
    }


    /**
     * Updates the camera center to follow the player's position.
     */
    public void updateCameraCenter() {
        cameraCenterX = player.getX();
        cameraCenterY = player.getY();
        clampCameraCenter();
    }

    /**
     * Ensures the camera's center stays within the valid map area
     * so that the viewport doesn't go out of bounds.
     */
    private void clampCameraCenter() {
        int half = viewportSize / 2;
        int totalCols = mapGrid[0].length;
        int totalRows = mapGrid.length;
        cameraCenterX = Math.max(half, Math.min(cameraCenterX, totalCols - viewportSize + half));
        cameraCenterY = Math.max(half, Math.min(cameraCenterY, totalRows - viewportSize + half));
    }

}
