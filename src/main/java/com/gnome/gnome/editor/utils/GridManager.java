package com.gnome.gnome.editor.utils;

import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.GridPane;
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

    // The GridPane instance that this manager operates on.
    private GridPane gridPane;

    /**
     * Default constructor. Initially, no grid pane is set.
     * You can set the grid pane later via {@link #setGridPane(GridPane)} or by using createEmptyGrid().
     */
    public GridManager() {
    }

    /**
     * Constructor that accepts a GridPane instance.
     *
     * @param gridPane the GridPane to manage.
     */
    public GridManager(GridPane gridPane) {
        this.gridPane = gridPane;
        attachDragAndDrop();
    }

    /**
     * Handles a drag-and-drop event on the managed grid pane.
     * <p>
     * It determines the drop cell based on the event's coordinates,
     * updates the underlying grid data, and updates the cell color.
     *
     * @param event the drag event.
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
                // Reinitialize GenerateGrid with the new grid.
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
     * Checks if the given cell coordinates are within the bounds of the grid.
     *
     * @param grid the underlying grid array.
     * @param row  the row index.
     * @param col  the column index.
     * @return true if valid; false otherwise.
     */
    private boolean isValidCell(int [][] grid, int row, int col) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid[0].length;
    }

    /**
     * Returns the numeric value corresponding to the specified tile type.
     *
     * @param type the tile type (e.g., "Goblin", "Dragon").
     * @return the corresponding numeric value.
     */
    private int getValueForType(String type) {
        return switch (type) {
            case "Goblin"    -> TypeOfObjects.GOBLIN.getValue();
            case "Dragon"    -> TypeOfObjects.DRAGON.getValue();
            case "Tree"      -> TypeOfObjects.TREE.getValue();
            case "Rock"      -> TypeOfObjects.ROCK.getValue();
            case "River"     -> TypeOfObjects.RIVER.getValue();
            case "Villager"  -> TypeOfObjects.VILLAGER.getValue();
            case "Merchant"  -> TypeOfObjects.MERCHANT.getValue();
            case "Mountain"  -> TypeOfObjects.MOUNTAIN.getValue();
            case "Hatch"     -> TypeOfObjects.HATCH.getValue();
            case "Floor"     -> TypeOfObjects.FLOOR.getValue();
            case "WallOne"   -> TypeOfObjects.WALL_ONE.getValue();
            case "WallTwo"   -> TypeOfObjects.WALL_TWO.getValue();
            default          -> TypeOfObjects.EMPTY.getValue();
        };
    }

    /**
     * Updates the cell at the specified row and column by setting its fill color
     * based on the new value.
     *
     * @param row      the row index.
     * @param col      the column index.
     * @param newValue the new value for the cell.
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
            logger.info("Checking node: " + node + " at (" + nodeRow + ", " + nodeCol + ")");
            if (nodeRow == row && nodeCol == col && node instanceof Rectangle rect) {
                rect.setFill(getColorForValue(newValue));
                found = true;
                break;
            }
        }
        if (!found) {
            logger.warning("No cell found at (" + row + ", " + col + ") to update.");
        }
    }

    /**
     * Returns the {@link Color} corresponding to the specified tile value.
     *
     * @param value the numeric tile value.
     * @return the corresponding Color.
     */
    private Color getColorForValue(int value) {
        return TypeOfObjects.fromValue(value).getColor();
    }

    /**
     * Attaches drag-and-drop functionality to the grid pane.
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
     * Applies zoom functionality based on scroll input.
     *
     * @param scaleTransform The scale transformation applied to the grid.
     * @param deltaY         The delta from the scroll event.
     * @param minScale       The minimum allowed scale.
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
     * Clamps a value between a minimum and maximum.
     *
     * @param value the value to clamp.
     * @param min   the minimum allowed value.
     * @param max   the maximum allowed value.
     * @return the clamped value.
     */
    public double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Creates an empty grid pane with default settings.
     * <p>
     * The created grid is stored in this manager and drag-and-drop functionality is attached.
     *
     * @return the newly created GridPane.
     */
    public GridPane createEmptyGrid() {
        int[][] emptyGrid = new int[GRID_SIZE][GRID_SIZE];
        for (int row = 0; row < GRID_SIZE; row++) {
            Arrays.fill(emptyGrid[row], 0);
        }
        GenerateGrid gridGen = GenerateGrid.getInstance(emptyGrid);
        GridPane newGrid = gridGen.generateGrid();
        newGrid.getTransforms().clear();
        // Set the new grid into the manager.
        setGridPane(newGrid);
        // Attach drag-and-drop functionality to the new grid.
        attachDragAndDrop();
        return newGrid;
    }
}
