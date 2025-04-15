package com.gnome.gnome.camera;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import lombok.AllArgsConstructor;

import java.util.Objects;

import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;

/**
 * A camera that displays a 15×15 viewport using image icons and highlights the player's cell.
 * The camera can follow the player's movement and clamp itself within the boundaries of the map.
 */
@AllArgsConstructor
public class Camera {

    /** 2D grid representing the game state */
    private int[][] mapGrid;

    /** X coordinate of the camera's center */
    private int cameraCenterX;

    /** Y coordinate of the camera's center */
    private int cameraCenterY;

    /** X coordinate of the player */
    private int playerX;

    /** Y coordinate of the player */
    private int playerY;

    /** Fixed viewport size (15x15) */
    private final int viewportSize = 15;

    /**
     * Returns a GridPane representing the current 15x15 viewport with image tiles.
     * The player's current cell is highlighted with a yellow border.
     *
     * @return the GridPane containing the viewport tiles
     */
    public GridPane getViewport() {
        GridPane viewport = new GridPane();
        int totalRows = mapGrid.length;
        int totalCols = mapGrid[0].length;
        int half = viewportSize / 2;

        int startRow = Math.max(0, Math.min(cameraCenterY - half, totalRows - viewportSize));
        int startCol = Math.max(0, Math.min(cameraCenterX - half, totalCols - viewportSize));

        for (int i = 0; i < viewportSize; i++) {
            for (int j = 0; j < viewportSize; j++) {
                int row = startRow + i;
                int col = startCol + j;

                StackPane tilePane = new StackPane();
                tilePane.setPrefSize(TILE_SIZE, TILE_SIZE);

                if (row >= 0 && row < totalRows && col >= 0 && col < totalCols) {
                    TypeOfObjects type = TypeOfObjects.fromValue(mapGrid[row][col]);
                    ImageView icon = new ImageView(new Image(
                            Objects.requireNonNull(TypeOfObjects.class.getResourceAsStream(type.getImagePath()))
                    ));
                    icon.setFitWidth(TILE_SIZE);
                    icon.setFitHeight(TILE_SIZE);
                    icon.setPreserveRatio(true);
                    tilePane.getChildren().add(icon);
                } else {
                    tilePane.setStyle("-fx-background-color: darkgray;");
                }

                // Border
                tilePane.setStyle(tilePane.getStyle() + "-fx-border-color: black; -fx-border-width: 1;");

                // Highlight player
                if (row == playerY && col == playerX) {
                    tilePane.setStyle(tilePane.getStyle() + "-fx-border-color: yellow; -fx-border-width: 2;");
                }

                viewport.add(tilePane, j, i);
            }
        }

        return viewport;
    }

    // ------------------- Player movement methods -------------------

    /**
     * Moves the player one tile to the left and updates the camera position accordingly.
     */
    public void movePlayerLeft() {
        playerX--;
        clampPlayer();
        followPlayer();
    }

    /**
     * Moves the player one tile to the right and updates the camera position accordingly.
     */
    public void movePlayerRight() {
        playerX++;
        clampPlayer();
        followPlayer();
    }

    /**
     * Moves the player one tile up and updates the camera position accordingly.
     */
    public void movePlayerUp() {
        playerY--;
        clampPlayer();
        followPlayer();
    }

    /**
     * Moves the player one tile down and updates the camera position accordingly.
     */
    public void movePlayerDown() {
        playerY++;
        clampPlayer();
        followPlayer();
    }

    /**
     * Moves the player by a specified offset in both x and y directions.
     *
     * @param dx horizontal offset
     * @param dy vertical offset
     */
    public void movePlayer(int dx, int dy) {
        playerX += dx;
        playerY += dy;
        clampPlayer();
        followPlayer();
    }

    // ------------------- Internal logic -------------------

    /**
     * Clamps the player's coordinates to ensure they remain within the map boundaries.
     */
    private void clampPlayer() {
        int maxX = mapGrid[0].length - 1;
        int maxY = mapGrid.length - 1;
        playerX = Math.max(0, Math.min(playerX, maxX));
        playerY = Math.max(0, Math.min(playerY, maxY));
    }

    /**
     * Updates the camera center to follow the player's position.
     */
    private void followPlayer() {
        cameraCenterX = playerX;
        cameraCenterY = playerY;
        clampCameraCenter();
    }

    /**
     * Clamps the camera center to ensure the viewport does not go out of map bounds.
     */
    private void clampCameraCenter() {
        int half = viewportSize / 2;
        int totalCols = mapGrid[0].length;
        int totalRows = mapGrid.length;
        cameraCenterX = Math.max(half, Math.min(cameraCenterX, totalCols - viewportSize + half));
        cameraCenterY = Math.max(half, Math.min(cameraCenterY, totalRows - viewportSize + half));
    }
}
