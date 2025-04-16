package com.gnome.gnome.editor.controller;

import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.models.Map;
import com.gnome.gnome.editor.javafxobj.SaveMapDialogBox;
import com.gnome.gnome.editor.javafxobj.SelectorMapDialogBox;
import com.gnome.gnome.editor.utils.CategoryGenerator;
import com.gnome.gnome.editor.utils.GenerateGrid;
import com.gnome.gnome.editor.utils.GridManager;
import com.gnome.gnome.exceptions.DataAccessException;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Group;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.gnome.gnome.editor.utils.EditorConstants.*;
import static javafx.scene.input.TransferMode.COPY;

/**
 * Controller class for the Editor Page.
 * <p>
 * This class handles:
 * <ul>
 *     <li>Grid initialization and update</li>
 *     <li>Zooming and panning functionalities on the grid</li>
 *     <li>Drag-and-drop events for adding items to the grid</li>
 *     <li>Dynamically generating inline category buttons using {@link CategoryGenerator}</li>
 *     <li>Loading and saving maps from/to file or database</li>
 * </ul>
 */
public class EditorPageController {

    // Logger instance for logging events and errors.
    private static final Logger logger = Logger.getLogger(EditorPageController.class.getName());

    // FXML UI components.
    @FXML private StackPane container;
    @FXML private ScrollPane scrollPane;
    @FXML private HBox mainButtonsBox;
    @FXML private ScrollPane inlineScrollPane;
    @FXML private HBox inlineButtonsBox;

    private final MapDAO mapDAO = new MapDAO();

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

    /** GridManager instance that now holds the grid pane internally. */
    private final GridManager gridManager;

    /** Instance of CategoryGenerator to dynamically create inline buttons. */
    private final CategoryGenerator categoryGenerator = new CategoryGenerator();

    private boolean autoCenterEnabled = true;

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
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);

        initializeGrid();
        setupContainerDragAndDrop();
        setupGridFunctionalities();
    }

    /**
     * Initialize the grid.
     */
    private void initializeGrid() {
        GridPane gridPane = container.getChildren().isEmpty()
                ? createAndSetEmptyGrid()
                : (GridPane) container.getChildren().getFirst();
        gridManager.setGridPane(gridPane);
    }

    /**
     * Configures drag-and-drop event handlers on the container.
     * <p>
     * If the GridManager does not hold a grid, an empty grid is created.
     */
    private void setupContainerDragAndDrop() {
        container.setOnDragDropped(gridManager::handleDragDrop);

        container.setOnDragOver(event -> {
            if (event.getGestureSource() != container && event.getDragboard().hasString())
                event.acceptTransferModes(COPY);
            event.consume();
        });
    }

    /**
     * Handles clicks on any main category button.
     * <p>
     * This method uses CategoryGenerator to asynchronously retrieve the items for the selected category.
     * Items are either strings (in test mode) or ImageViews (from S3). After the items are retrieved,
     * the method dynamically creates inline buttons or adds images, then shows the inline scroll pane
     * while hiding the main category row.
     *
     * @param event the button click event
     */
    @FXML
    private void onCategoryButtonClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        String category = (String) clickedButton.getUserData();

        // Clear previous inline buttons.
        inlineButtonsBox.getChildren().clear();

        // Used the CategoryGenerator to retrieve items for the given category.
        // The returned items will be Strings (when test==true) or ImageViews (when test==false).
        CompletableFuture<List<Object>> futureItems = categoryGenerator.getItemsForCategory(category);
        futureItems.thenAccept(items -> {
            Platform.runLater(() -> {
                // Loop through the retrieved items.
                for (Object item : items) {
                    if (item instanceof String s) {
                        createInlineButton(s);
                    } else if (item instanceof ImageView iv) {
                        iv.setFitWidth(64);
                        iv.setPreserveRatio(true);
                        inlineButtonsBox.getChildren().add(iv);
                    }
                }
                // Add a "Back" button to allow returning to the main category row.
                Button backButton = new Button("Back");
                backButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #f9f9f9, #e6e6e6); " +
                                "-fx-text-fill: #333; " +
                                "-fx-font-size: 14px; " +
                                "-fx-padding: 6 12; " +
                                "-fx-background-radius: 4; " +
                                "-fx-border-color: #ccc; " +
                                "-fx-border-radius: 4; " +
                                "-fx-border-width: 1;"
                );
                backButton.setOnAction(this::onBackToMainButtonClick);
                inlineButtonsBox.getChildren().add(backButton);

                // Hide the main row and show the inline scroll pane.
                mainButtonsBox.setVisible(false);
                mainButtonsBox.setManaged(false);
                inlineScrollPane.setVisible(true);
                inlineScrollPane.setManaged(true);
            });
        }).exceptionally(ex -> {
            logger.log(Level.SEVERE, "Failed to retrieve category items", ex);
            return null;
        });
    }

    /**
     * Handles the click event on the "Back" button.
     * <p>
     * Clears the inline buttons and toggles the visibility so that the main category row is shown again.
     *
     * @param event the button click event
     */
    @FXML
    private void onBackToMainButtonClick(ActionEvent event) {
        // Save current scroll offsets
        double currentH = inlineScrollPane.getHvalue();
        double currentV = inlineScrollPane.getVvalue();

        // Disable auto-centering temporarily
        autoCenterEnabled = false;

        // Clear inline buttons and hide the inline scroll pane
        inlineButtonsBox.getChildren().clear();
        inlineScrollPane.setVisible(false);
        inlineScrollPane.setManaged(false);

        // Show main category row
        mainButtonsBox.setVisible(true);
        mainButtonsBox.setManaged(true);

        // Restore previous scroll offsets
        Platform.runLater(() -> {
            inlineScrollPane.setHvalue(currentH);
            inlineScrollPane.setVvalue(currentV);
            // Re-enable auto-centering after layout changes are done.
            Platform.runLater(() -> autoCenterEnabled = true);
        });
    }

    /**
     * Dynamically creates an inline button with the given text and applies styling.
     * <p>
     * The created button is also configured to be draggable.
     *
     * @param text the text for the inline button
     */
    private void createInlineButton(String text) {
        Button subButton = new Button(text);

        subButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #f9f9f9, #e6e6e6); " +
                        "-fx-text-fill: #333; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 6 12; " +
                        "-fx-background-radius: 4; " +
                        "-fx-border-color: #ccc; " +
                        "-fx-border-radius: 4; " +
                        "-fx-border-width: 1;"
        );
        setupDragForButton(subButton);
        inlineButtonsBox.getChildren().add(subButton);
    }


    /**
     * Configures a Button to be draggable.
     * <p>
     * When drag is detected, the button's text is placed on the dragboard for potential drop operations.
     *
     * @param btn the Button to be made draggable
     */
    private void setupDragForButton(Button btn) {
        btn.setOnDragDetected(event -> {
            var db = btn.startDragAndDrop(javafx.scene.input.TransferMode.COPY);
            var content = new ClipboardContent();
            content.putString(btn.getText());
            db.setContent(content);
            event.consume();
        });
    }

    /**
     * Calculates the minimum zoom level dynamically based on the viewport size.
     */
    private void calculateMinScale() {
        double viewportWidth  = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        double gridWidth  = GRID_SIZE * TILE_SIZE;
        double gridHeight = GRID_SIZE * TILE_SIZE;

        double scaleX = viewportWidth / gridWidth;
        double scaleY = viewportHeight / gridHeight;
        double containScale = Math.max(scaleX, scaleY);

        minScale = Math.min(containScale, MAX_SCALE);
    }

    /**
     * Enables zooming functionality when the user scrolls while holding CTRL.
     *
     */
    private void addZoomFunctionality() {
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
        if (!autoCenterEnabled) {
            return;
        }

        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        double gridWidth = (GRID_SIZE * TILE_SIZE) * scaleTransform.getX();
        double gridHeight = (GRID_SIZE * TILE_SIZE) * scaleTransform.getY();


        if (gridWidth < viewportWidth && gridHeight < viewportHeight) {
            // Entire grid fits in the viewport – no need to pan
            scrollPane.setHvalue(0.5);
            scrollPane.setVvalue(0.5);
            scrollPane.setPannable(false); // or skip the panning code
        } else {
            scrollPane.setPannable(true);
        }
    }

    /**
     * Enables panning on the container using the middle mouse button.
     * <p>
     * Stores initial mouse and scroll positions on press, and updates scroll values as the mouse is dragged.
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

                double newH = gridManager.clamp(hScrollStart + deltaX, 0, 1);
                double newV = gridManager.clamp(vScrollStart + deltaY, 0, 1);

                scrollPane.setHvalue(newH);
                scrollPane.setVvalue(newV);
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
     * @param file the file containing map data
     */
    private void loadMapFromFile(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            int rows = lines.size();
            int cols = lines.get(0).split(" ").length;

            int[][] levelGrid = new int[rows][cols];
            for (int row = 0; row < rows; row++) {
                String[] values = lines.get(row).split(" ");
                for (int col = 0; col < cols; col++) {
                    levelGrid[row][col] = Integer.parseInt(values[col]);
                }
            }
            setupGrid(levelGrid, cols, rows);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error occurred during reading the lines from file", e);
        }
    }

    /**
     * Sets up the grid using the provided level data.
     * <p>
     * The grid is generated, the scale transformation is applied, and functionalities (zoom, panning, drag-and-drop)
     * are attached. The grid is then added to the container.
     *
     * @param levelGrid the 2D array representing the level
     * @param cols      the number of columns in the grid
     * @param rows      the number of rows in the grid
     */
    private void setupGrid(int[][] levelGrid, int cols, int rows) {
        GenerateGrid gridGen = GenerateGrid.getInstance(levelGrid);
        GridPane gridPane = gridGen.generateGrid();

        double gridWidth = cols * TILE_SIZE;
        double gridHeight = rows * TILE_SIZE;

        GridPane.setHalignment(gridPane, HPos.CENTER);
        gridPane.setMinSize(gridWidth, gridHeight);
        gridPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        gridPane.getTransforms().clear();
        gridPane.getTransforms().add(scaleTransform);

        Group zoomGroup = new Group(gridPane);

        container.getChildren().clear();
        container.getChildren().add(zoomGroup);
        container.setAlignment(Pos.CENTER);
//        container.setPrefSize(gridWidth, gridHeight);

        calculateMinScale();
        centerGrid();
        setupGridFunctionalities();
        scrollPane.setPannable(true);

        logger.info("Map loaded successfully.");
    }

    /**
     * Creates an empty grid and sets it in the GridManager.
     * <p>
     * The created grid is added to the container.
     *
     * @return the newly created GridPane.
     */
    private GridPane createAndSetEmptyGrid() {
        // Create a new empty grid using GridManager.
        GridPane gridPane = gridManager.createEmptyGrid();
        gridPane.getTransforms().clear();
        gridPane.getTransforms().add(scaleTransform);

        // Attach common functionalities to the grid.
//        setupGridFunctionalities();
        container.getChildren().clear();
        container.getChildren().add(gridPane);
        container.setAlignment(Pos.CENTER);
        container.setPrefSize(GRID_SIZE * TILE_SIZE, GRID_SIZE * TILE_SIZE);
        return gridPane;
    }

    /**
     * Sets up common functionalities for the grid including zoom, panning, and drag-and-drop.
     * This feature was disabled because I don't know how to fix it.
     */
    private void setupGridFunctionalities() {
        addZoomFunctionality();
        addPanningFunctionality();
    }


    /**
     * Handles loading a map from the database.
     *
     */
    @FXML
    protected void onLoadMapFromDatabase(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        MapDAO mapDAO = new MapDAO();
//        String currentUsername = getCurrentUsername(); // Implement this method to get the logged-in user
        String currentUsername = "Admin"; // Admin for now

        try {
            List<Map> userMaps = mapDAO.getMapsByUsername(currentUsername);
            if (userMaps.isEmpty()) {
                logger.info("No maps have been created by user: " + currentUsername);
                return;
            }
            List<String> mapNames = userMaps.stream()
                    .map(Map::getMapNameEng)
                    .collect(Collectors.toList());

            SelectorMapDialogBox mapDialog = new SelectorMapDialogBox(mapNames);
            Optional<String> result = mapDialog.showDialog(stage);

            result.ifPresent(selectedItem -> {
                logger.info("Selected map: " + selectedItem);

                Map selectedMap = userMaps.stream()
                        .filter(map -> (map.getMapNameEng()).equals(selectedItem))
                        .findFirst()
                        .orElse(null);

                if (selectedMap != null) {
                    int[][] levelGrid = selectedMap.getMapData();

                    setupGrid(levelGrid, levelGrid[0].length, levelGrid.length);
                    logger.info("Map loaded successfully: " + selectedItem);
                } else {
                    logger.log(Level.SEVERE,"Error", "Selected map not found in user maps.");
                }
            });

        } catch (DataAccessException e) {
            logger.log(Level.SEVERE, "Error loading maps from database", e);
        }
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
//                    lines.forEach(System.out::println);
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
        result.ifPresent(fileName -> {
            try {
                int[][] mapGrid = GenerateGrid.getInstance().getMapGrid();
                if (mapGrid == null || mapGrid.length == 0) {
                    return;
                }

                Map map = new Map(
                        "Admin", // Using Admin for now
                        mapGrid,
                        0,
                        fileName,
                        fileName,
                        1
                );
                mapDAO.insertMap(map);
                logger.info("Map saved to database successfully for username: " + fileName);
            } catch (DataAccessException e) {
                logger.log(Level.SEVERE, "Failed to save map to database", e);
            }
        });
    }

    /**
     * Clear current map by clicking.
     *
     */
    @FXML
    private void onClearButtonClick(ActionEvent event) {
        createAndSetEmptyGrid();
    }
}
