package com.gnome.gnome.editor.utils;

import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.logging.Logger;

import static com.gnome.gnome.editor.utils.EditorConstants.*;
import static javafx.scene.input.TransferMode.COPY;

/**
 * The {@code GridManager} class handles grid-related functionalities such as
 * drag-and-drop events, zooming, and grid cell updates. In this upgraded version,
 * the grid pane is stored as an instance field so that methods do not require it
 * as a parameter.
 */
@Getter
@Setter
public class GridManager {

    private static final Logger logger = Logger.getLogger(GridManager.class.getName());

    /** The GridPane instance that this manager operates on. */
    private GridPane gridPane;

    /**
     * Default constructor.
     * Use {@link #setGridPane(GridPane)} and {@link #attachDragAndDrop()} to configure.
     */
    public GridManager() {}

    /**
     * Constructs a GridManager with the given GridPane and immediately attaches drag-and-drop events.
     *
     * @param gridPane the GridPane to manage
     */
    public GridManager(GridPane gridPane) {
        this.gridPane = gridPane;
        attachDragAndDrop();
    }

    /**
     * Handles the drop event when an object is dragged onto the grid.
     * Converts the object type to a numeric value and updates the grid cell accordingly.
     *
     * @param event the drag event
     */
    public void handleDragDrop(DragEvent event) {
        var db = event.getDragboard();
        boolean success = false;
        if (db.hasString()) {
            String draggedType = db.getString();
            int col = (int) (event.getX() / TILE_SIZE);
            int row = (int) (event.getY() / TILE_SIZE);
            logger.info("Dropped " + draggedType + " at (" + row + ", " + col + ")");

            int[][] mapGrid = GenerateGrid.getInstance().getMapGrid();
            if (mapGrid == null) {
                logger.warning("Map grid is null. Creating an empty grid.");
                int[][] emptyGrid = new int[GRID_SIZE][GRID_SIZE];
                for (int rowTwo = 0; rowTwo < GRID_SIZE; rowTwo++) {
                    Arrays.fill(emptyGrid[rowTwo], 0);
                }
                GenerateGrid newGridInstance = GenerateGrid.getInstance(emptyGrid);
                mapGrid = newGridInstance.getMapGrid();
            }

            if (isValidCell(mapGrid, row, col)) {
                int newValue = getValueForType(draggedType);
                mapGrid[row][col] = newValue;
                updateGridCell(row, col, newValue);
            }
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Checks if the given coordinates are within the bounds of the grid.
     *
     * @param grid the map grid
     * @param row  the row index
     * @param col  the column index
     * @return true if the cell is valid, false otherwise
     */
    private boolean isValidCell(int[][] grid, int row, int col) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid[0].length;
    }

    /**
     * Returns the numeric value corresponding to a given object type name.
     *
     * @param type the name of the object type (e.g., "Goblin", "Tree")
     * @return the corresponding integer value for the map grid
     */
    private int getValueForType(String type) {
        return switch (type) {
            // Empty / Start / Finish
            case "Empty"        -> TypeOfObjects.EMPTY.getValue();
            case "Start_Point"   -> TypeOfObjects.START_POINT.getValue();
            case "Finish_Point"  -> TypeOfObjects.FINISH_POINT.getValue();

            // Monsters
            case "Demon"        -> TypeOfObjects.DEMON.getValue();
            case "Butterfly"    -> TypeOfObjects.BUTTERFLY.getValue();
            case "Goblin"       -> TypeOfObjects.GOBLIN.getValue();
            case "Scorpion"     -> TypeOfObjects.SCORPION.getValue();
            case "Skeleton"     -> TypeOfObjects.SKELETON.getValue();

            // Props
            case "Floor"        -> TypeOfObjects.FLOOR.getValue();
            case "Blocked"      -> TypeOfObjects.BLOCKED.getValue();
            case "Bookshelf"    -> TypeOfObjects.BOOKSHELF.getValue();
            case "Table"        -> TypeOfObjects.TABLE.getValue();

            // Environment
            case "Tree"         -> TypeOfObjects.TREE.getValue();
            case "Rock"         -> TypeOfObjects.ROCK.getValue();
            case "River"        -> TypeOfObjects.RIVER.getValue();
            case "Cactus"       -> TypeOfObjects.CACTUS.getValue();
            case "Web"          -> TypeOfObjects.WEB.getValue();
            case "Stump"        -> TypeOfObjects.STUMP.getValue();
            case "Mountain"     -> TypeOfObjects.MOUNTAIN.getValue();

            // Walls
            case "Wall_1"       -> TypeOfObjects.WALL_1.getValue();
            case "Wall_2"       -> TypeOfObjects.WALL_2.getValue();
            case "Wall_3"       -> TypeOfObjects.WALL_3.getValue();
            case "Wall_4"       -> TypeOfObjects.WALL_4.getValue();
            case "Wall_5"       -> TypeOfObjects.WALL_5.getValue();
            case "Wall_6"       -> TypeOfObjects.WALL_6.getValue();
            case "Wall_7"       -> TypeOfObjects.WALL_7.getValue();
            case "Wall_8"       -> TypeOfObjects.WALL_8.getValue();
            case "Wall_9"       -> TypeOfObjects.WALL_9.getValue();
            case "Wall_10"      -> TypeOfObjects.WALL_10.getValue();
            case "Wall_11"      -> TypeOfObjects.WALL_11.getValue();

            // Chests
            case "Chest_1"      -> TypeOfObjects.CHEST_1.getValue();
            case "Chest_2"      -> TypeOfObjects.CHEST_2.getValue();
            case "Chest_3"      -> TypeOfObjects.CHEST_3.getValue();
            case "Chest_4"      -> TypeOfObjects.CHEST_4.getValue();
            case "Chest_5"      -> TypeOfObjects.CHEST_5.getValue();
            case "Chest_6"      -> TypeOfObjects.CHEST_6.getValue();

            // Doors
            case "Door_1"       -> TypeOfObjects.DOOR_1.getValue();
            case "Door_2"       -> TypeOfObjects.DOOR_2.getValue();
            case "Door_3"       -> TypeOfObjects.DOOR_3.getValue();
            case "Door_4"       -> TypeOfObjects.DOOR_4.getValue();
            case "Door_5"       -> TypeOfObjects.DOOR_5.getValue();
            case "Door_6"       -> TypeOfObjects.DOOR_6.getValue();
            case "Door_7"       -> TypeOfObjects.DOOR_7.getValue();
            case "Door_8"       -> TypeOfObjects.DOOR_8.getValue();
            case "Door_9"       -> TypeOfObjects.DOOR_9.getValue();
            case "Door_10"      -> TypeOfObjects.DOOR_10.getValue();
            case "Door_11"      -> TypeOfObjects.DOOR_11.getValue();
            case "Door_12"      -> TypeOfObjects.DOOR_12.getValue();
            case "Door_13"      -> TypeOfObjects.DOOR_13.getValue();

            // Default fallback
            default             -> TypeOfObjects.EMPTY.getValue();
        };
    }

    /**
     * Updates a single cell in the grid with a new object type image.
     *
     * @param row      the row index of the cell
     * @param col      the column index of the cell
     * @param newValue the new value to be placed in the cell
     */
    public void updateGridCell(int row, int col, int newValue) {
        var children = gridPane.getChildren();
        logger.info("Updating cell at (" + row + ", " + col + ") with new value: " + newValue);
        boolean found = false;

        for (Node node : children) {
            Integer nodeRow = GridPane.getRowIndex(node);
            Integer nodeCol = GridPane.getColumnIndex(node);
            nodeRow = (nodeRow == null) ? 0 : nodeRow;
            nodeCol = (nodeCol == null) ? 0 : nodeCol;

            if (nodeRow == row && nodeCol == col && node instanceof StackPane pane) {
                TypeOfObjects newType = TypeOfObjects.fromValue(newValue);
                GenerateGrid.updateTileImage(pane, newType);
                found = true;
                break;
            }
        }

        if (!found) {
            logger.warning("No cell found at (" + row + ", " + col + ") to update.");
        }
    }

    /**
     * Attaches drag-and-drop event handlers to the current GridPane.
     */
    public void attachDragAndDrop() {
        if (gridPane == null) {
            logger.warning("GridPane not set. Cannot attach drag-and-drop functionality.");
            return;
        }
        gridPane.setOnDragOver(event -> {
            if (event.getGestureSource() != gridPane && event.getDragboard().hasString())
                event.acceptTransferModes(COPY);
            event.consume();
        });
        gridPane.setOnDragDropped(this::handleDragDrop);
    }

    /**
     * Applies zoom transformation to the grid using the provided scale object.
     *
     * @param scaleTransform the scale transform applied to the grid
     * @param deltaY         the mouse scroll direction (positive to zoom in, negative to zoom out)
     * @param minScale       the minimum scale allowed
     */
    public void zoom(Scale scaleTransform, double deltaY, double minScale) {
        double zoomFactor = (deltaY > 0) ? 1.1 : 0.9;
        double newScale = scaleTransform.getX() * zoomFactor;

        if (newScale < minScale) {
            newScale = minScale;
        } else if (newScale > MAX_SCALE) {
            newScale = MAX_SCALE;
        }

        scaleTransform.setX(newScale);
        scaleTransform.setY(newScale);
    }

    /**
     * Clamps a value to ensure it falls within the given range.
     *
     * @param value the value to clamp
     * @param min   the minimum allowed value
     * @param max   the maximum allowed value
     * @return the clamped value
     */
    public double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Creates a new empty grid and initializes the {@link GenerateGrid} singleton with it.
     * Also sets this grid as the current one and attaches drag-and-drop events.
     *
     * @return a new empty GridPane
     */
    public GridPane createEmptyGrid() {
        int[][] emptyGrid = new int[GRID_SIZE][GRID_SIZE];
        for (int row = 0; row < GRID_SIZE; row++) {
            Arrays.fill(emptyGrid[row], 0);
        }
        GenerateGrid gridGen = GenerateGrid.getInstance(emptyGrid);
        GridPane newGrid = gridGen.generateGrid();
        newGrid.getTransforms().clear();
        setGridPane(newGrid);
        attachDragAndDrop();
        return newGrid;
    }
}
