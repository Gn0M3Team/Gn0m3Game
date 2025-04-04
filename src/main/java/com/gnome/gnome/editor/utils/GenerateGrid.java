package com.gnome.gnome.editor.utils;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;

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
     * Creates an interactive tile for the grid.
     *
     * @param row The row index.
     * @param col The column index.
     * @return A Rectangle representing the tile.
     */
    private Rectangle createTile(int row, int col) {
        Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
        // cord of the elements to track the state
        tile.setUserData(new int[]{row, col});

        AtomicReference<TypeOfObjects> tileType = new AtomicReference<>(TypeOfObjects.fromValue(mapGrid[row][col]));
        updateTileColor(tile, tileType.get());

        tile.setOnMouseClicked(event -> {
                    // when left mouse button clicked, reset state of the cell to default
                    if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                        // get cord of the cells
                        int[] indices = (int[]) tile.getUserData();
                        int r = indices[0], c = indices[1];

                        // update state on cord
                        mapGrid[r][c] = 0;
                        tileType.set(TypeOfObjects.fromValue(0));

                        // reset color
                        updateTileColor(tile, tileType.get());
                    }
        });
        return tile;
    }

    /**
     * Updates the tile color based on its type.
     *
     * @param tile The tile rectangle.
     * @param type The type of the tile.
     */
    private void updateTileColor(Rectangle tile, TypeOfObjects type) {
        tile.setFill(type.getColor());
        tile.setStroke(javafx.scene.paint.Color.BLACK);
    }
}
