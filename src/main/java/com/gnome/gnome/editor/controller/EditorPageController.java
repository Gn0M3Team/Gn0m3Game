package com.gnome.gnome.editor.controller;

import com.gnome.gnome.editor.javafxobj.SaveMapDialogBox;
import com.gnome.gnome.editor.javafxobj.SelectorMapDialogBox;
import com.gnome.gnome.editor.utils.GenerateGrid;
import com.gnome.gnome.editor.utils.GridManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.gnome.gnome.editor.utils.EditorConstants.*;
import static javafx.scene.input.TransferMode.COPY;

public class EditorPageController {

    private static final Logger logger = Logger.getLogger(EditorPageController.class.getName());

    @FXML
    private StackPane container;
    @FXML private ScrollPane scrollPane;
    @FXML private TitledPane monsterPane;
    @FXML private TitledPane propPane;
    @FXML private TitledPane npcPane;
    @FXML private TitledPane environmentPane;

    private String currentSelection;

    /** Default zoom level */
    private final double scale = 1.0;
    /** Minimum zoom scale dynamically calculated based on the viewport size  */
    private double minScale;

    /** Mouse anchor positions for panning */
    private double mouseAnchorX, mouseAnchorY;
    /** Initial scroll position when panning starts */
    private double hScrollStart, vScrollStart;

    /** Scale transformation applied to the grid */
    private final Scale scaleTransform;

    private final GridManager gridManager;

    /**
     * Default constructor, initializes the level grid.
     */
    public EditorPageController() {
        scaleTransform = new Scale(scale, scale);
        gridManager = new GridManager();
    }

    /**
     * Initializes the editor scene, sets up the grid, and enables zooming and panning.
     */
    @FXML
    public void initialize() {
        setupSidebarDragEvents();
        setupContainerDragAndDrop();
    }

    /**
     * Sets up the container's drag-and-drop event handlers.
     */
    private void setupContainerDragAndDrop() {
        container.setOnDragDropped(event -> {
            GridPane gridPane = container.getChildren().isEmpty()
                    ? createAndSetEmptyGrid()
                    : (GridPane) container.getChildren().getFirst();
            gridManager.handleDragDrop(gridPane, event);
        });

        container.setOnDragOver(event -> {
            if (event.getGestureSource() != container && event.getDragboard().hasString())
                event.acceptTransferModes(COPY);
            event.consume();
        });
    }

    /**
     * Sets up drag event handlers for each sidebar titled pane.
     */
    private void setupSidebarDragEvents() {
        setupDragForPane(monsterPane);
        setupDragForPane(propPane);
        setupDragForPane(npcPane);
        setupDragForPane(environmentPane);
    }

    /**
     * Attaches a drag detection handler to all {@code Label} nodes contained within the given titled pane.
     *
     */
    private void setupDragForPane(TitledPane pane) {
        if (pane.getContent() instanceof VBox box) {
            box.getChildren().forEach(node -> {
                if (node instanceof Label label) {
                    label.setOnDragDetected(event -> {
                        var db = label.startDragAndDrop(COPY);
                        ClipboardContent content = new ClipboardContent();
                        content.putString(label.getText());
                        db.setContent(content);
                        logger.info("Dragging: " + label.getText());
                        event.consume();
                    });
                }
            });
        }
    }

    /**
     * Processes a drag-and-drop event on the grid.
     *
     */
    private void processDropEvent(GridPane gridPane, DragEvent event) {
        var db = event.getDragboard();
        boolean success = false;

        if (db.hasString()) {
            String draggedType = db.getString();
            int col = (int)(event.getX() / TILE_SIZE);
            int row = (int)(event.getY() /  TILE_SIZE);
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

    private boolean isValidCell(int [] [] grid, int row, int col) {
        return row >= 0 && row < grid.length && col >= 0 && col < grid[0].length;
    }

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

    private void updateGridCell(GridPane gridPane, int row, int col, int newValue) {
        for (Node node : gridPane.getChildren()) {
            Integer nodeRow = GridPane.getRowIndex(node);
            Integer nodeCol = GridPane.getColumnIndex(node);
            nodeRow = (nodeRow == null) ? 0 : nodeRow;
            nodeCol = (nodeCol == null) ? 0 : nodeCol;
            if (nodeRow == row && nodeCol == col && node instanceof Rectangle rect) {
                rect.setFill(newValue == 1 ? Color.RED
                        : newValue == 2 ? Color.BLUE
                        : newValue == 3 ? Color.BEIGE
                        : newValue == 4 ? Color.BISQUE
                        : newValue == 5 ? Color.GRAY
                        : newValue == 6 ? Color.BLANCHEDALMOND
                        : newValue == 7 ? Color.BLUEVIOLET
                        : newValue == 8 ? Color.BROWN : Color.GREEN);
                break;
            }
        }
    }

    private void addDragAndDropFunctionality(GridPane gridPane) {
        gridPane.setOnDragOver(event -> {
            if (event.getGestureSource() != gridPane && event.getDragboard().hasString())
                event.acceptTransferModes(COPY);
            event.consume();
        });

        gridPane.setOnDragDropped(event -> processDropEvent(gridPane, event));
    }

    /**
     * Calculates the minimum zoom level dynamically based on the viewport size.
     */
    private void calculateMinScale() {
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        double gridWidth = GRID_SIZE * TILE_SIZE;
        double gridHeight = GRID_SIZE * TILE_SIZE;

        double scaleX = viewportWidth / gridWidth;
        double scaleY = viewportHeight / gridHeight;

        minScale = Math.max(Math.min(scaleX, scaleY), 1.0);
    }

    /**
     * Enables zooming functionality when the user scrolls while holding CTRL.
     *
     * @param gridPane The grid to which zooming is applied.
     */
    private void addZoomFunctionality(GridPane gridPane) {
        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                event.consume();
                gridManager.zoom(scaleTransform, event.getDeltaY(), minScale);
                centerGrid();
            }
        });
        scrollPane.viewportBoundsProperty().addListener((obs, oldVal, newVal) -> {
            calculateMinScale();
            centerGrid();
        });
    }
    /**
     * Centers the grid when zooming out or resizing.
     */
    private void centerGrid() {
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        double gridWidth = (GRID_SIZE * TILE_SIZE) * scaleTransform.getX();
        double gridHeight = (GRID_SIZE * TILE_SIZE) * scaleTransform.getY();

        scrollPane.setHvalue((gridWidth > viewportWidth) ? 0.5 : 0);
        scrollPane.setVvalue((gridHeight > viewportHeight) ? 0.5 : 0);
    }

    /**
     * Enables panning functionality by dragging the middle mouse button.
     */
    /**
     * Enables panning functionality by dragging the middle mouse button.
     */
    private void addPanningFunctionality() {
        container.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.MIDDLE) {
                mouseAnchorX = event.getSceneX();
                mouseAnchorY = event.getSceneY();
                hScrollStart = scrollPane.getHvalue();
                vScrollStart = scrollPane.getVvalue();
            }
        });

        container.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.MIDDLE) {
                double deltaX = (mouseAnchorX - event.getSceneX()) / container.getWidth();
                double deltaY = (mouseAnchorY - event.getSceneY()) / container.getHeight();
                scrollPane.setHvalue(gridManager.clamp(hScrollStart + deltaX, 0, 1));
                scrollPane.setVvalue(gridManager.clamp(vScrollStart + deltaY, 0, 1));
            }
        });

        container.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.MIDDLE) {
                mouseAnchorX = mouseAnchorY = 0;
            }
        });
    }

    /**
     * Handles returning to the main menu when the back button is clicked.
     *
     * @param event Button click event.
     * @throws IOException If the FXML file cannot be loaded.
     */
    @FXML
    protected void onBackButtonClick(ActionEvent event) throws IOException {
        URL fxmlUrl = getClass().getResource("/com/gnome/gnome/pages/hello-view.fxml");
        Objects.requireNonNull(fxmlUrl, "FXML file not found!");

        Parent mainRoot = FXMLLoader.load(fxmlUrl);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(mainRoot);
    }

    @FXML
    protected void onLoadMapButtonClick(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose the map");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Map Files", "*.map")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            loadMapFromFile(selectedFile);
        }
    }

    /**
     * Loads a map from the specified file and sets up the grid accordingly.
     *
     */
    private void loadMapFromFile(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());

            int rows = lines.size();
            int cols = lines.getFirst().split(" ").length;

            int [] [] levelGrid = new int[rows][cols];
            for (int row = 0; row < rows;row++) {
                String[] values = lines.get(row).split(" ");
                for (int col = 0; col < cols; col++)
                    levelGrid[row][col] = Integer.parseInt(values[col]);
            }


            setupGrid(levelGrid, cols, rows);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error occurred during reading the lines from file", e);
        }
    }

    /**
     * Sets up the grid with the provided level data.
     *
     */
    private void setupGrid(int[][] levelGrid, int cols, int rows) {
        GenerateGrid gridGen = GenerateGrid.getInstance(levelGrid);
        GridPane gridPane = gridGen.generateGrid();
        double gridWidth = cols * TILE_SIZE;
        double gridHeight = rows * TILE_SIZE;

        GridPane.setHalignment(gridPane, HPos.CENTER);
        gridPane.setMinSize(gridWidth, gridHeight);
        gridPane.setMaxSize(gridWidth, gridHeight);
        gridPane.getTransforms().clear();
        gridPane.getTransforms().add(scaleTransform);

        container.getChildren().clear();
        container.getChildren().add(gridPane);
        container.setAlignment(Pos.CENTER);
        container.setPrefSize(gridWidth, gridHeight);

        calculateMinScale();
        centerGrid();
        setupGridFunctionalities(gridPane);
        scrollPane.setPannable(true);
        logger.info("Map loaded successfully.");
    }

    /**
     * Creates an empty grid, attaches grid functionalities, and adds it to the container.
     */
    private GridPane createAndSetEmptyGrid() {
        GridPane gridPane = gridManager.createEmptyGrid();

        gridPane.getTransforms().clear();
        gridPane.getTransforms().add(scaleTransform);

        setupGridFunctionalities(gridPane);
        container.getChildren().clear();
        container.getChildren().add(gridPane);
        container.setAlignment(Pos.CENTER);
        container.setPrefSize(GRID_SIZE * TILE_SIZE, GRID_SIZE * TILE_SIZE);
        return gridPane;
    }

    /**
     * Sets up common functionalities for the grid including zoom, panning, and drag-and-drop.
     *
     */
    private void setupGridFunctionalities(GridPane gridPane) {
        addZoomFunctionality(gridPane);
        addPanningFunctionality();
        addDragAndDropFunctionality(gridPane);
    }


    /**
     * Handles loading a map from the database.
     *
     */
    @FXML
    protected void onLoadMapFromDatabase(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SelectorMapDialogBox mapDialog = new SelectorMapDialogBox();
        Optional<String> result = mapDialog.showDialog(stage);

        result.ifPresent(selectedItem -> System.out.println("Selected map: " + selectedItem));
    }

    /**
     * Handles saving the map to a local device.
     *
     */
    @FXML
    protected void onSaveToLocalDevice(ActionEvent event) {
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SaveMapDialogBox saveMapDialog = new SaveMapDialogBox();
        Optional<String> result = saveMapDialog.showDialog(primaryStage);

        result.ifPresent(fileName -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Folder to Save File");
            File selectedDirectory = directoryChooser.showDialog(primaryStage);

            if (selectedDirectory != null) {
                File fileToSave = new File(selectedDirectory, fileName + ".map");
                try {
                    int[][] map = GenerateGrid.getInstance().getMapGrid();
                    List<String> lines = Arrays.stream(map)
                            .map(row -> Arrays.stream(row)
                                    .mapToObj(String::valueOf)
                                    .collect(Collectors.joining(" ")))
                            .collect(Collectors.toList());
                    Files.write(fileToSave.toPath(), lines);
                    logger.info("Map successfully saved to local device.");
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error saving map to local device", e);
                }
            }
        });
    }

    /**
     * Handles saving the map to the database.
     *
     */
    @FXML
    protected void onSaveToDatabase(ActionEvent event) {
        Stage primaryStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        SaveMapDialogBox mapDialog = new SaveMapDialogBox();
        Optional<String> result = mapDialog.showDialog(primaryStage);
        result.ifPresent(fileName -> logger.info("Save to DB requested for: " + fileName));
    }
}
