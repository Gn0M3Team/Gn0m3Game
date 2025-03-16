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

    private enum TileType {
        EMPTY(Color.GREEN, 0),
        MOUNTAIN(Color.RED, 1),
        GOBLIN(Color.BLUE, 2),
        DRAGON(Color.BEIGE, 3),
        TREE(Color.BISQUE, 4),
        ROCK(Color.GRAY, 5),
        RIVER(Color.BLANCHEDALMOND, 6),
        VILLAGER(Color.BLUEVIOLET, 7),
        MERCHANT(Color.BROWN, 8);

        @Getter
        private final Color color;
        private final int value;

        TileType(Color color, int value) {
            this.color = color;
            this.value = value;
        }

        /** Finds a tile type by its integer value */
        public static TileType fromValue(int value) {
            return switch (value) {
                case 0 -> EMPTY;
                case 1 -> MOUNTAIN;
                case 2 -> GOBLIN;
                case 3 -> DRAGON;
                case 4 -> TREE;
                case 5 -> ROCK;
                case 6 -> RIVER;
                case 7 -> VILLAGER;
                case 8 -> MERCHANT;
                default -> EMPTY;
            };
        }

    }

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
        AtomicReference<TileType> tileType = new AtomicReference<>(TileType.fromValue(mapGrid[row][col]));

        updateTileColor(tile, tileType.get());

        return tile;
    }

    /**
     * Updates the tile color based on its type.
     *
     * @param tile The tile rectangle.
     * @param type The type of the tile.
     */
    private void updateTileColor(Rectangle tile, TileType type) {
        tile.setFill(type.getColor());
        tile.setStroke(javafx.scene.paint.Color.BLACK);
    }
}
