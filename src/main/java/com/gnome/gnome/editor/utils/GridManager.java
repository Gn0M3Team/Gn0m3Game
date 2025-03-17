package com.gnome.gnome.editor.utils;

import javafx.scene.input.DragEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;

import java.util.Arrays;
import java.util.logging.Logger;

import static com.gnome.gnome.editor.utils.EditorConstants.*;
import static javafx.scene.input.TransferMode.COPY;


/**
 * The {@code GridManager} class handles grid related functionalities,
 * such as drag-and-drop events, zooming, and grid cell updates.
 */
public class GridManager {

    private static final Logger logger = Logger.getLogger(GridManager.class.getName());

    /**
     * Handles a drag-and-drop event on the provided grid pane.
     */
    public void handleDragDrop(GridPane gridPane, DragEvent event) {
        var db = event.getDragboard();
        boolean success = false;
        if (db.hasString()) {
            String draggedType = db.getString();
            int col = (int) (event.getX() / TILE_SIZE);
            int row = (int) (event.getY() / TILE_SIZE);
            logger.info("Dropped " + draggedType + " at (" + row + ", " + col + ")");

            int [][] mapGrid = GenerateGrid.getInstance().getMapGrid();
            if (isValidCell(mapGrid, row, col)) {
                int newValue = getValueForType(draggedType);
                mapGrid[row][col] = newValue;
                updateGridCell(gridPane, row, col, newValue);
            }
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    /**
     * Checks if the given cell coordinates are within the bounds of the grid.
     *
     */
    private boolean isValidCell(int [][] grid, int row, int col) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid[0].length;
    }
    /**
     * Returns the numeric value corresponding to the specified tile type.
     *
     */
    private int getValueForType(String type) {
        return switch (type) {
            case "Goblin" -> 2;
            case "Dragon" -> 3;
            case "Tree" -> 4;
            case "Rock" -> 5;
            case "River" -> 6;
            case "Villager" -> 7;
            case "Merchant" -> 8;
            case "Mountain" -> 1;
            default -> 0;
        };
    }

    /**
     * Updates the color of the cell at the specified row and column in the grid pane.
     *
     */
    public void updateGridCell(GridPane gridPane, int row, int col, int newValue) {
        gridPane.getChildren().stream().filter(node -> {
            Integer nodeRow = GridPane.getRowIndex(node);
            Integer nodeCol = GridPane.getColumnIndex(node);
            nodeRow = (nodeRow == null) ? 0 : nodeRow;
            nodeCol = (nodeCol == null) ? 0 : nodeCol;
            return nodeRow == row && nodeCol == col && node instanceof Rectangle;
        }).findFirst().ifPresent(node -> {
            Rectangle rect = (Rectangle) node;
            rect.setFill(getColorForValue(newValue));
        });
    }

    /**
     * Returns the {@link Color} corresponding to the specified tile value.
     *
     */
    private Color getColorForValue(int value) {
        return switch (value) {
            case 1 -> Color.RED;
            case 2 -> Color.BLUE;
            case 3 -> Color.BEIGE;
            case 4 -> Color.BISQUE;
            case 5 -> Color.GRAY;
            case 6 -> Color.BLANCHEDALMOND;
            case 7 -> Color.BLUEVIOLET;
            case 8 -> Color.BROWN;
            default -> Color.GREEN;
        };
    }

    /**
     * Attaches drag-and-drop functionality to the grid pane.
     */
    public void attachDragAndDrop(GridPane gridPane) {
        gridPane.setOnDragOver(event -> {
            if (event.getGestureSource() != gridPane && event.getDragboard().hasString())
                event.acceptTransferModes(COPY);
            event.consume();
        });
        gridPane.setOnDragDropped(event -> handleDragDrop(gridPane,event));
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
        if (newScale >= minScale && newScale <= MAX_SCALE) {
            scaleTransform.setX(newScale);
            scaleTransform.setY(newScale);
        } else if (newScale < minScale) {
            scaleTransform.setX(minScale);
            scaleTransform.setY(minScale);
        }
    }

    /**
     * Clamps a value between a minimum and maximum.
     */
    public double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    /**
     * Creates an empty grid pane with default settings and attaches drag-and-drop functionality.
     *
     */
    public GridPane createEmptyGrid() {
        int [][] emptyGrid = new int[GRID_SIZE][GRID_SIZE];
        for (int row = 0; row < GRID_SIZE;row++)
            Arrays.fill(emptyGrid[row], 0);
        GenerateGrid gridGen = GenerateGrid.getInstance(emptyGrid);
        GridPane gridPane = gridGen.generateGrid();
        gridPane.getTransforms().clear();
        attachDragAndDrop(gridPane);
        return gridPane;
    }
}
