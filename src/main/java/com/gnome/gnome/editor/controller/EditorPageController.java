package com.gnome.gnome.editor.controller;

import com.gnome.gnome.MainApplication;
import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.editor.javafxobj.TemplateMapDialog;
import com.gnome.gnome.editor.utils.*;
import com.gnome.gnome.dao.userDAO.UserSession;
import com.gnome.gnome.models.Map;
import com.gnome.gnome.editor.javafxobj.SaveMapDialogBox;
import com.gnome.gnome.editor.javafxobj.SelectorMapDialogBox;
import com.gnome.gnome.exceptions.DataAccessException;
import com.gnome.gnome.models.user.PlayerRole;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import com.gnome.gnome.userState.UserState;
import com.gnome.gnome.utils.AlertUtil;
import com.gnome.gnome.utils.CustomPopupUtil;
import com.gnome.gnome.utils.annotation.MyValueInjection;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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

    @FXML
    private BorderPane editorPage;
    private PageSwitcherInterface pageSwitch;

    private final MapDAO mapDAO = new MapDAO();

    private GenerateGrid gridGen;

    private Button prevButtonMonster;
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
    private GridManager gridManager;

    /** Instance of CategoryGenerator to dynamically create inline buttons. */
    private CategoryGenerator categoryGenerator = new CategoryGenerator();

    private boolean autoCenterEnabled = true;

    private final UserState userState = UserState.getInstance();

    private ResourceBundle bundle;
    /**
     * Default constructor, initializes the level grid.
     */
    public EditorPageController() {
        scaleTransform = new Scale(scale, scale);
        gridManager = new GridManager();
        categoryGenerator = MyValueInjection.getInstance().createInstance(CategoryGenerator.class);
        gridGen = GenerateGrid.getInstance();
        prevButtonMonster = null;
    }

    /**
     * Initializes the editor scene, sets up the grid, and enables zooming and panning.
     */
    @FXML
    public void initialize() {
        this.bundle = MainApplication.getLangBundle();

        pageSwitch=new SwitchPage();

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
     * Creates an inline button for a given {@link BotType} and adds it to the {@link HBox} container.
     * <p>
     * This method dynamically generates a button with the name of the bot type as its text. It also sets the
     * background image of the button based on the image path provided by the {@link BotType}. The button is
     * styled with CSS properties, including background image, size, text color, and border radius.
     * Additionally, the button is made draggable using the {@link #setupDragForButton(Button)} method.
     *
     * @param botType the {@link BotType} object containing the name and image path for the button
     * @throws NullPointerException if the {@link BotType} object is null or its image path cannot be found
     * @see BotType
     */
    private void createInlineButton(BotType botType) {
        Button subButton = new Button(botType.getName());

        // Dynamically set background image for each button
        String imagePath = botType.getImagePath();
        URL resource = getClass().getResource(imagePath);

        if (resource != null) {
            String imageUrl = resource.toExternalForm();
            subButton.setStyle("-fx-background-image: url(" + imageUrl + ");"
                    + "-fx-background-repeat: no-repeat;"
                    + "-fx-background-size: cover;"
                    + "-fx-background-position: center;"
                    + "-fx-text-fill: white;"
                    + "-fx-font-size: 10px;"
                    + "-fx-border-radius: 5px;"
                    );

            // Set button size
            subButton.setPrefSize(100, 100);
        }

        subButton.setOnAction(event -> {
            handleButtonClick(botType, subButton);
        });

        setupDragForButton(subButton);
        inlineButtonsBox.getChildren().add(subButton);
    }


    /**
     * Handles the click on a monster‑type button: toggles its selection state and updates the button’s border.
     * <p>
     * If the given BotType is already selected in the GenerateGrid singleton, this method will:
     * <ul>
     *   <li>remove the yellow highlight (set border to transparent),</li>
     *   <li>clear the selected BotType in GenerateGrid.</li>
     * </ul>
     * Otherwise, it will:
     * <ul>
     *   <li>apply a yellow border to the clicked button,</li>
     *   <li>set the new BotType as selected in GenerateGrid.</li>
     * </ul>
     *
     * @param botType    the BotType associated with this button
     * @param subButton  the Button instance that was clicked
     */
    private void handleButtonClick(BotType botType, Button subButton) {
        GenerateGrid grid = GenerateGrid.getInstance();
        if (grid.getSelectedBotType() == botType) {
            subButton.setStyle(subButton.getStyle() + ";-fx-border-color: transparent;");
            grid.setSelectedBotType(null);
            prevButtonMonster = null;
        } else {
            subButton.setStyle(subButton.getStyle() + ";-fx-border-color: yellow;");
            grid.setSelectedBotType(botType);
            if (prevButtonMonster != null)
                prevButtonMonster.setStyle(prevButtonMonster.getStyle() + ";-fx-border-color: transparent;");
            prevButtonMonster = subButton;
        }
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
     * Loads a map from the specified file and sets up the grid accordingly.
     *
     * @param file the file containing map data
     */
    private boolean loadMapFromFile(File file) {
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
            return true;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error occurred during reading the lines from file", e);
            return false;
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
        gridGen = GenerateGrid.getInstance(levelGrid);
        GridPane gridPane = gridGen.generateGrid();

        double gridWidth = cols * TILE_SIZE;
        double gridHeight = rows * TILE_SIZE;

        GridPane.setHalignment(gridPane, HPos.CENTER);
        gridPane.setMinSize(gridWidth, gridHeight);
        gridPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        gridPane.getTransforms().clear();
        gridPane.getTransforms().add(scaleTransform);

        gridManager = new GridManager(gridPane);

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
                    if (item instanceof BotType botType) {
                        createInlineButton(botType);
                    }
                }


                // Add a "Back" button to allow returning to the main category row.
                Button backButton = new Button("Back");

                backButton.setOnAction(ev -> {
                    onBackToMainButtonClick(ev);
                    if (prevButtonMonster != null) {
                        GenerateGrid.getInstance().setSelectedBotType(null);
                    }
                });

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
     * Handles returning to the main menu when the back button is clicked.
     *
     * @param event Button click event.
     */
    @FXML
    protected void onBackButtonClick(ActionEvent event) {
        pageSwitch.goMainMenu(editorPage);
        if (prevButtonMonster != null) {
            GenerateGrid.getInstance().setSelectedBotType(null);
        }
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

        if (selectedFile != null && loadMapFromFile(selectedFile)) {
            CustomPopupUtil.showSuccess(stage, bundle.getString("map.load.success"));;
        } else {
            CustomPopupUtil.showError(stage, bundle.getString("map.load.error"));
        }
    }


    /**
     * Loads maps from the database based on the user's role.
     * <ul>
     *     <li>If the user is an ADMIN, all maps are available.</li>
     *     <li>If the user is a MAP_CREATOR, only maps created by the user are available.</li>
     *     <li>Other roles are not allowed to load maps.</li>
     * </ul>
     *
     * Displays a selection dialog for the user to choose a map to load.
     *
     * @param event the action event triggered by the UI interaction
     */
    @FXML
    protected void onLoadMapFromDatabase(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        MapDAO mapDAO = new MapDAO();
        String currentUsername = userState.getUsername();

        try {
            List<Map> userMaps;

            if (userState.getRole() == PlayerRole.ADMIN) {
                userMaps = mapDAO.getAllMaps();
            } else {
                userMaps = mapDAO.getMapsByUsername(currentUsername);
            }
//            } else if (userState.getRole() == PlayerRole.MAP_CREATOR) {
//                userMaps = mapDAO.getMapsByUsername(currentUsername);
//            }
//            else {
//                logger.warning("User does not have permission to load maps.");
//                return;
//            }

            if (userMaps.isEmpty()) {
                logger.info("No maps available for user: " + currentUsername);
                CustomPopupUtil.showWarning(stage, bundle.getString("error.no.maps.server"));
            }

            List<String> mapNames = userMaps.stream()
                    .map(map -> "ID: " + map.getId() + " - Name: " + map.getName())
                    .collect(Collectors.toList());

            SelectorMapDialogBox mapDialog = new SelectorMapDialogBox(mapNames);
            Optional<String> result = mapDialog.showDialog(stage);

            result.ifPresent(selectedItem -> {
                logger.info("Selected map: " + selectedItem);

                Map selectedMap = userMaps.stream()
                        .filter(map -> ("ID: " + map.getId() + " - Name: " + map.getName()).equals(selectedItem))
                        .findFirst()
                        .orElse(null);

                if (selectedMap != null) {
                    int[][] levelGrid = selectedMap.getMapData();
                    setupGrid(levelGrid, levelGrid[0].length, levelGrid.length);
                    logger.info("Map loaded successfully: " + selectedItem);
                } else {
                    logger.log(Level.SEVERE,"Selected map not found in user maps.");
                    CustomPopupUtil.showWarning(stage, bundle.getString("error.map.not.found.user"));
                }
            });

        } catch (DataAccessException e) {
            logger.log(Level.SEVERE, "Error loading maps from database", e);
            CustomPopupUtil.showError(stage, bundle.getString("map.load.error"));
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

        int[][] mapGrid = GenerateGrid.getInstance().getMapGrid();
        MapValidator.ValidationResult resultValidator = MapValidator.validate(mapGrid);

        if (!resultValidator.isSuccess()) {
            CustomPopupUtil.showWarning(primaryStage, resultValidator.message);
            return;
        }

        result.ifPresent(fileName -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle(bundle.getString("dialog.save.folder.title"));
            File selectedDirectory = directoryChooser.showDialog(primaryStage);

            if (selectedDirectory != null) {
                File fileToSave = new File(selectedDirectory, fileName + ".map");
                try {
                    List<String> lines = Arrays.stream(mapGrid)
                            .map(row -> Arrays.stream(row)
                                    .mapToObj(String::valueOf)
                                    .collect(Collectors.joining(" ")))
                            .collect(Collectors.toList());
                    Files.write(fileToSave.toPath(), lines);
                    logger.info("Map successfully was saved to local device.");
                    CustomPopupUtil.showSuccess(primaryStage, bundle.getString("map.save.local.success"));
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error saving map to local device", e);
                    CustomPopupUtil.showError(primaryStage, bundle.getString("map.save.local.error"));
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

        int[][] mapGrid = GenerateGrid.getInstance().getMapGrid();
        MapValidator.ValidationResult resultValidator = MapValidator.validate(mapGrid);

        if (!resultValidator.isSuccess()) {
            CustomPopupUtil.showWarning(primaryStage, resultValidator.message);
            return;
        }

        result.ifPresent(fileName -> {
            try {
                int level = 0;

                Map map = new Map(
                        userState.getUsername(),
                        mapGrid,
                        100,
                        fileName,
                        fileName,
                        level

                );

                if (mapDialog.isStoryMap()) {
                    List<Map> maps = mapDAO.getMapsOrderedByLevelDesc();
                    if (!maps.isEmpty()) {
                        Map lastMap = maps.getFirst();
                        level = lastMap.getLevel() + 1;
                        map.setLevel(level);
                    }
                    mapDAO.insertMap(map, true);
                    logger.info("Story map saved to database: " + fileName);
                    CustomPopupUtil.showSuccess(primaryStage, bundle.getString("map.save.database.story.success"));

                } else {
                    mapDAO.insertMap(map, false);
                    logger.info("Map saved to database: " + fileName);
                    CustomPopupUtil.showSuccess(primaryStage, bundle.getString("map.save.database.regular.success"));

                }
            } catch (DataAccessException e) {
                logger.log(Level.SEVERE, "Failed to save map to database", e);
                CustomPopupUtil.showError(primaryStage, bundle.getString("map.save.database.error"));
            }
        });
    }

    /**
     * Handles the action of updating an existing map in the database.
     * <p>
     * This method retrieves all maps owned by the current user (or all maps if the user is an admin),
     * validates the current editor map, and allows the user to select a map to update via a dialog.
     * If a map is selected and valid, its data is updated in the database and a success message is shown.
     * </p>
     *
     * @param event the action event triggered by clicking the "Update Map" button
     */
    @FXML
    protected void onUpdateMapFromDatabase(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        MapDAO mapDAO = new MapDAO();
        String currentUsername = userState.getUsername();

        try {
            List<Map> userMaps;

            if (userState.getRole() == PlayerRole.ADMIN) {
                userMaps = mapDAO.getAllMaps();
            } else {
                userMaps = mapDAO.getMapsByUsername(currentUsername);
            }

            if (userMaps.isEmpty()) {
                logger.info("No maps available for update for user: " + currentUsername);
                CustomPopupUtil.showWarning(stage, bundle.getString("warning.no.maps.update"));
                return;
            }

            int[][] mapGrid = GenerateGrid.getInstance().getMapGrid();
            MapValidator.ValidationResult resultValidator = MapValidator.validate(mapGrid);

            if (!resultValidator.isSuccess()) {
                CustomPopupUtil.showWarning(stage, resultValidator.message);
                return;
            }

            List<String> mapNames = userMaps.stream()
                    .map(map -> "ID: " + map.getId() + " - Name: " + map.getName())
                    .collect(Collectors.toList());

            SelectorMapDialogBox mapDialog = new SelectorMapDialogBox(mapNames);
            Optional<String> result = mapDialog.showDialog(stage);

            result.ifPresent(selectedItem -> {
                logger.info("Selected map for update: " + selectedItem);

                Map selectedMap = userMaps.stream()
                        .filter(map -> ("ID: " + map.getId() + " - Name: " + map.getName()).equals(selectedItem))
                        .findFirst()
                        .orElse(null);


                if (selectedMap != null) {

                    selectedMap.setMapData(mapGrid);
                    selectedMap.setScoreVal(selectedMap.getScoreVal() + 100);

                    mapDAO.updateMap(selectedMap);
                    logger.info("Map updated successfully: " + selectedMap.getMapNameEng());
                    CustomPopupUtil.showSuccess(stage, bundle.getString("map.update.success"));

                } else {
                    logger.log(Level.SEVERE, "Selected map not found in user maps.");
                    CustomPopupUtil.showWarning(stage, bundle.getString("error.map.not.found.user"));
                }

            });

        } catch (DataAccessException e) {
            logger.log(Level.SEVERE, "Error updating map in database", e);
            CustomPopupUtil.showError(stage, bundle.getString("map.update.error"));
        }
    }



    /**
     * Clear current map by clicking.
     *
     */
    @FXML
    private void onClearButtonClick(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        createAndSetEmptyGrid();
        CustomPopupUtil.showSuccess(stage, bundle.getString("map.clear.success"));
    }


    /**
     * Handles the event when the user selects a map from the list of available maps in the resources.
     * <p>
     * This method opens a dialog where the user can choose a map. Once the map is selected, the method
     * checks if the map exists in the resources directory (`com/gnome/gnome/maps`). If the map file
     * is found with the specified name and ends with `.map`, it is loaded into the game using the
     * `loadMapFromFile()` method.
     * </p>
     *
     * @param event the {@link ActionEvent} triggered when the user selects a map.
     */
    @FXML
    protected void onLoadMapFromResources(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        TemplateMapDialog mapSelectorDialog = new TemplateMapDialog();

        Optional<String> result = mapSelectorDialog.showDialog(stage);

        result.ifPresent(selectedMapName -> {
            logger.info("Selected map: " + selectedMapName);

            try {
                URL mapsDirUrl = getClass().getClassLoader().getResource("com/gnome/gnome/maps");

                if (mapsDirUrl != null) {
                    File mapsDirectory = new File(mapsDirUrl.toURI());

                    if (mapsDirectory.exists() && mapsDirectory.isDirectory()) {
                        File[] mapFiles = mapsDirectory.listFiles(file -> file.getName().endsWith(".map"));

                        boolean mapFound = false;
                        for (File mapFile : mapFiles) {
                            if (mapFile.getName().equals(selectedMapName)) {
                                logger.info("Map file found: " + mapFile.getPath());
                                loadMapFromFile(mapFile);
                                mapFound = true;
                                break;
                            }
                        }

                        if (!mapFound) {
                            logger.log(Level.SEVERE, "Map file not found with the name: " + selectedMapName);
                        }
                    } else {
                        logger.severe("Maps directory not found or it's not a directory.");
                    }
                } else {
                    logger.log(Level.SEVERE, "Maps directory URL is null.");
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error loading map from resources", e);
            }
        });
    }


    /**
     * Deletes a map from the database based on the user's role.
     * <ul>
     *     <li>If the user is an ADMIN, they can delete any map.</li>
     *     <li>If the user is a MAP_CREATOR, they can only delete their own maps.</li>
     *     <li>Other roles are not allowed to delete maps.</li>
     * </ul>
     *
     * Displays a selection dialog for the user to choose which map to delete.
     *
     * @param event the action event triggered by the UI interaction
     */
    @FXML
    protected void onDeleteMapFromDatabase(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        MapDAO mapDAO = new MapDAO();
        String currentUsername = userState.getUsername();

        try {
            List<Map> userMaps;

            if (userState.getRole() == PlayerRole.ADMIN) {
                userMaps = mapDAO.getAllMaps();
            } else if (userState.getRole() == PlayerRole.MAP_CREATOR) {
                userMaps = mapDAO.getMapsByUsername(currentUsername);
            } else {
                logger.warning("User does not have permission to delete maps.");
                return;
            }

            if (userMaps.isEmpty()) {
                logger.info("No maps available for user: " + currentUsername);
                return;
            }

            List<String> mapDisplayList = userMaps.stream()
                    .map(map -> "ID: " + map.getId() + " - Name: " + map.getName())
                    .collect(Collectors.toList());

            SelectorMapDialogBox mapDialog = new SelectorMapDialogBox(mapDisplayList);
            Optional<String> result = mapDialog.showDialog(stage);

            result.ifPresent(selectedItem -> {
                logger.info("Selected map for deletion: " + selectedItem);

                Map selectedMap = userMaps.stream()
                        .filter(map -> ("ID: " + map.getId() + " - Name: " + map.getName()).equals(selectedItem))
                        .findFirst()
                        .orElse(null);

                if (selectedMap != null) {
                    mapDAO.deleteMapById(selectedMap.getId());
                    logger.info("Map deleted successfully: " + selectedItem);
                    CustomPopupUtil.showSuccess(stage, bundle.getString("map.delete.success"));
                } else {
                    logger.log(Level.SEVERE, "Selected map not found in user maps.");
                    CustomPopupUtil.showWarning(stage, bundle.getString("error.map.not.found.user"));
                }
            });

        } catch (DataAccessException e) {
            logger.log(Level.SEVERE, "Error deleting map from database", e);
            CustomPopupUtil.showError(stage, bundle.getString("map.delete.error"));
        }
    }
}
