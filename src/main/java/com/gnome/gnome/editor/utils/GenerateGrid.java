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
    private int[][] mapGrid;

    private BotType selectedBotType = null;

    private GenerateGrid(int[][] mapGrid) {
        this.mapGrid = mapGrid.clone();
    }

    private GenerateGrid() {}

    /**
     * Returns the singleton instance of GenerateGrid with the specified grid.
     * If an instance already exists, it updates the existing mapGrid.
     *
     * @param mapGrid the grid to initialize or update the instance with
     * @return the singleton instance of GenerateGrid
     */
    public static GenerateGrid getInstance(int[][] mapGrid) {
        if (instance == null) {
            synchronized (GenerateGrid.class) {
                if (instance == null)
                    instance = new GenerateGrid(mapGrid);
            }
        } else {
            instance.setMapGrid(mapGrid.clone());
        }

        return instance;
    }

    /**
     * Returns the singleton instance of GenerateGrid without initializing the grid.
     * If it does not exist, it creates a new empty instance.
     *
     * @return the singleton instance of GenerateGrid
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
     * Generates a GridPane representation of the current mapGrid.
     * Each cell is converted into a visual tile with an image.
     *
     * @return the generated GridPane
     */
    public GridPane generateGrid() {
        GridPane gridPane = new GridPane();
        gridPane.setGridLinesVisible(false);

        for (int row = 0; row < mapGrid.length; row++) {
            for (int col = 0; col < mapGrid[row].length; col++) {
                gridPane.add(createTile(row, col), col, row);
            }
        }

        return gridPane;
    }

    /**
     * Creates a grid tile at the specified row and column, styles it, initializes its image,
     * and attaches mouse click handlers for placing or clearing a monster.
     * <p>
     * The returned {@link StackPane} is configured with:
     * <ul>
     *   <li>A fixed preferred size of {@code TILE_SIZE x TILE_SIZE}.</li>
     *   <li>A black border and transparent background.</li>
     *   <li>User data storing its [row, col] coordinates.</li>
     *   <li>An initial image based on the current value in {@code mapGrid[row][col]}.</li>
     *   <li>A mouse click listener that:
     *     <ul>
     *       <li><b>Primary click</b> (left button): if {@code selectedBotType} is non-null,
     *           retrieves its {@link TypeOfObjects}, updates the {@code mapGrid} at this cell,
     *           and refreshes the tile image to the monster.</li>
     *       <li><b>Secondary click</b> (right button): clears the cell to empty (value 0)
     *           and updates the tile image to {@link TypeOfObjects#EMPTY}.</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @param row the zero-based row index of this tile in the grid
     * @param col the zero-based column index of this tile in the grid
     * @return a {@link StackPane} representing the fully configured tile
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
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY ) {  // Левая кнопка мыши
                // Проверяем, выбран ли монстр
                if (selectedBotType != null) {
                    int[] indices = (int[]) tilePane.getUserData();
                    int r = indices[0];
                    int c = indices[1];

                    TypeOfObjects monsterType = selectedBotType.getMonsterType();

                    mapGrid[r][c] = monsterType.getValue();
                    updateTileImage(tilePane, monsterType);
                }
            } else if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
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
