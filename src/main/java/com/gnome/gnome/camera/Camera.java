package com.gnome.gnome.camera;
import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.player.Player;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import lombok.Data;
import java.util.Objects;
import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;

/**
 * The Camera class is responsible for rendering a 15x15 viewport of the map,
 * centered around a specific point, typically the player's position.
 * It only shows a portion of the full map to simulate a camera view.
 */
@Data
public class Camera {
    // The full map represented as a 2D array of integers (object types).
    private int[][] mapGrid;
    private int cameraCenterX;

    /** Y coordinate of the camera's center */
    private int cameraCenterY;

    // Size of the viewport (15x15 tiles).
    private final int viewportSize = 15;

    // Calculated starting row and column of the visible viewport.
    private int startRow;
    private int startCol;

    // Reference to the player (used to track his position).
    private Player player;

    public Camera(int[][] fieldMap, int cameraCenterX, int cameraCenterY, Player player) {
        this.mapGrid = fieldMap;
        this.cameraCenterX = cameraCenterX;
        this.cameraCenterY = cameraCenterY;
        this.player = player;
    }

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

        // Calculate top-left corner of the viewport while keeping it inside map bounds.
        startRow = Math.max(0, Math.min(cameraCenterY - half, totalRows - viewportSize));
        startCol = Math.max(0, Math.min(cameraCenterX - half, totalCols - viewportSize));

        for (int i = 0; i < viewportSize; i++) {
            for (int j = 0; j < viewportSize; j++) {
                int row = startRow + i;
                int col = startCol + j;

                StackPane tilePane = new StackPane();
                tilePane.setPrefSize(TILE_SIZE, TILE_SIZE);

                // Only add image if the tile is within map bounds.
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
                if (row == player.getY() && col == player.getX()) {
                    tilePane.setStyle(tilePane.getStyle() + "-fx-border-color: yellow; -fx-border-width: 2;");
                }

                viewport.add(tilePane, j, i);
            }
        }

        return viewport;
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
