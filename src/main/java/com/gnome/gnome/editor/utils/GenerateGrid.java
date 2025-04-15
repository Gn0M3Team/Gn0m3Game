package com.gnome.gnome.editor.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;

@Getter
@Setter
public class GenerateGrid {

    /** Singleton instance */
    private static volatile GenerateGrid instance;

    /** 2D grid representing the game state */
    private int [][] mapGrid;

    private GenerateGrid(int [][] mapGrid) {
        this.mapGrid = mapGrid.clone();
    }

    private GenerateGrid() {}

    /**
     * Provides a thread-safe singleton instance.
     *
     * @param mapGrid The initial level grid.
     * @return Singleton instance of GenerateGrid.
     */
    public static GenerateGrid getInstance(int [][] mapGrid) {
        if (instance == null) {
            synchronized (GenerateGrid.class) {
                if (instance == null)
                    instance = new GenerateGrid(mapGrid);
            }
        }
        else {
            // update map if the instance already exists
            instance.setMapGrid(mapGrid.clone());
        }

        return instance;
    }

    /**
     * Provides a thread-safe singleton instance.
     *
     * @return Singleton instance of GenerateGrid.
     */
    public static GenerateGrid getInstance() {
        if (instance == null) {
            synchronized (GenerateGrid.class) {
                if (instance == null)
                    instance = new GenerateGrid();
            }
        }

        return instance;
    }

    /**
     * Generates a GridPane representing the level.
     *
     * @return GridPane containing tiles.
     */
    public GridPane generateGrid() {
        GridPane gridPane = new GridPane();
        gridPane.setGridLinesVisible(false);

        for (int row = 0; row < mapGrid.length; row++)
            for (int col = 0; col < mapGrid[row].length; col++)
                gridPane.add(createTile(row, col), col, row);

        return gridPane;
    }

    /**
     * Creates a single tile for the grid based on the provided coordinates.
     * Sets the tile's style, image, and click behavior.
     *
     * @param row the row index of the tile
     * @param col the column index of the tile
     * @return a StackPane representing the tile
     */
    private StackPane createTile(int row, int col) {
        StackPane tilePane = new StackPane();
        tilePane.setPrefSize(TILE_SIZE, TILE_SIZE);
        tilePane.setStyle(
                "-fx-border-color: black; " +
                        "-fx-border-width: 1; " +
                        "-fx-border-style: solid; " +
                        "-fx-border-insets: 0; " +
                        "-fx-padding: 0;" +
                        "-fx-background-color: transparent;"
        );
        tilePane.setUserData(new int[]{row, col});

        TypeOfObjects tileType = TypeOfObjects.fromValue(mapGrid[row][col]);
        updateTileImage(tilePane, tileType);

        tilePane.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                int[] indices = (int[]) tilePane.getUserData();
                int r = indices[0];
                int c = indices[1];

                mapGrid[r][c] = 0;
                updateTileImage(tilePane, TypeOfObjects.EMPTY);
            }
        });

        return tilePane;
    }

    /**
     * Updates the tile image for the given type.
     * Made static to be callable from outside (e.g., GridManager)
     *
     * @param tilePane the StackPane representing the tile
     * @param type     the TypeOfObjects that determines the image
     */
    public static void updateTileImage(StackPane tilePane, TypeOfObjects type) {
        tilePane.getChildren().clear();
        ImageView icon = new ImageView(
                new Image(Objects.requireNonNull(
                        GenerateGrid.class.getResourceAsStream(type.getImagePath())
                ))
        );
        icon.setFitWidth(TILE_SIZE);
        icon.setFitHeight(TILE_SIZE);
        icon.setPreserveRatio(true);
        tilePane.getChildren().add(icon);
    }
}
