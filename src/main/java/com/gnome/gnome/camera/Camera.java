package com.gnome.gnome.camera;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.AllArgsConstructor;

import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;

/**
 * A camera that displays a 10×10 viewport and supports a margin (outside cells)
 * and highlights the player's cell in yellow.
 */
@AllArgsConstructor
public class Camera {
    private int[][] mapGrid;

    // Camera center.
    private int cameraCenterX;
    private int cameraCenterY;

    // Player's position on the map.
    private int playerX;
    private int playerY;

    // Fixed viewport size: 15x15
    private final int viewportSize = 15;


    /**
     * Returns a 10×10 GridPane representing the current viewport.
     * Cells outside the map boundaries will use DARKGRAY.
     * The player's tile is highlighted in yellow.
     */
    public GridPane getViewport() {
        GridPane viewport = new GridPane();
        int totalRows = mapGrid.length;
        int totalCols = mapGrid[0].length;
        int half = viewportSize / 2;

        // Compute the starting row and column for the viewport based on the camera center.
        int startRow = Math.max(0, Math.min(cameraCenterY - half, totalRows - viewportSize));
        int startCol = Math.max(0, Math.min(cameraCenterX - half, totalCols - viewportSize));


        for (int i = 0; i < viewportSize; i++) {
            for (int j = 0; j < viewportSize; j++) {
                int row = startRow + i;
                int col = startCol + j;
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);

                if (row >= 0 && row < totalRows && col >= 0 && col < totalCols) {
                    tile.setFill(TypeOfObjects.fromValue(mapGrid[row][col]).getColor());
                } else {
                    tile.setFill(Color.DARKGRAY);
                }
                tile.setStroke(Color.BLACK);
                tile.setStrokeWidth(1);

                // Highlight the player's cell.
                if (row == playerY && col == playerX) {
                    tile.setStroke(Color.YELLOW);
                }
                viewport.add(tile, j, i);
            }
        }
        return viewport;
    }

    // --- Movement methods; these make the camera follow the player ---
    public void movePlayerLeft() {
        playerX--;
        clampPlayer();
        followPlayer();
    }

    public void movePlayerRight() {
        playerX++;
        clampPlayer();
        followPlayer();
    }

    public void movePlayerUp() {
        playerY--;
        clampPlayer();
        followPlayer();
    }

    public void movePlayerDown() {
        playerY++;
        clampPlayer();
        followPlayer();
    }

    public void movePlayer(int dx, int dy) {
        playerX += dx;
        playerY += dy;
        clampPlayer();
        followPlayer();
    }

    /**
     * Clamps the player's position so that it does not exceed the map boundaries.
     */
    private void clampPlayer() {
        int maxX = mapGrid[0].length - 1;
        int maxY = mapGrid.length - 1;
        playerX = Math.max(0, Math.min(playerX, maxX));
        playerY = Math.max(0, Math.min(playerY, maxY));
        System.out.println("Player position: " + playerX + ", " + playerY);
    }

    /**
     * Updates the camera center to follow the player.
     */
    private void followPlayer() {
        cameraCenterX = playerX;
        cameraCenterY = playerY;
        clampCameraCenter();
    }

    /**
     * Clamps the camera center so the viewport fits within the map boundaries.
     */
    private void clampCameraCenter() {
        int half = viewportSize / 2;
        int totalCols = mapGrid[0].length;
        int totalRows = mapGrid.length;
        cameraCenterX = Math.max(half, Math.min(cameraCenterX, totalCols - viewportSize + half));
        cameraCenterY = Math.max(half, Math.min(cameraCenterY, totalRows - viewportSize + half));
        System.out.println("Camera center: " + cameraCenterX + ", " + cameraCenterY);
    }
}
