package com.gnome.gnome.continueGame;

import com.gnome.gnome.camera.Camera;
import com.gnome.gnome.components.PlayerHealthBar;
import com.gnome.gnome.continueGame.component.Coin;
import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.MonsterFactory;
import com.gnome.gnome.monsters.MonsterFactory.MonsterType;
import com.gnome.gnome.monsters.types.Skeleton;
import com.gnome.gnome.monsters.types.missels.Arrow;
import com.gnome.gnome.player.Player;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.Random;

import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;

/**
 * Controller class for the "Continue Game" scene.
 * Manages player movement, the camera viewport, and an in-game popup menu.
 */
public class ContinueGameController implements Initializable {

    /** Root layout of the scene. */
    @FXML private BorderPane rootBorder;

    /** StackPane container for the camera viewport. */
    @FXML private StackPane centerStack;

    /** Button that opens the center menu popup. */
    @FXML private Button centerMenuButton;
    @FXML private StackPane healthBarContainer;

    @FXML private Label coinCountLabel;

    // Game map
    private int[][] baseMap;
    private int[][] fieldMap;
    private PageSwitcherInterface pageSwitch;

    /** The full 30x30 map grid representing the game world. */
    private int[][] field30x30;

    // Camera to view part of the map
    private Camera camera;

    // Popup for center menu
    private Popup centerMenuPopup;

    // List of monsters on the map
    private final List<Monster> monsterList = new ArrayList<>();
    // List of coins on the map
    private final List<Coin> coinsOnMap = new ArrayList<>();

    private static final Logger logger = Logger.getLogger(ContinueGameController.class.getName());

    // Game timer for monster movement
    private AnimationTimer monsterMovementTimer;
    private final long MONSTER_UPDATE_INTERVAL_NANOS = 1_000_000_000L;
    private long lastMonsterUpdateTime = 0;

    // Skeleton attack interval
    private final long SKELETON_ATTACK_INTERVAL_NANOS = 3_000_000_000L;
    private long lastSkeletonAttackTime = 0;

    // Player instance
    private Player player;
    private static final int PLAYER_MAX_HEALTH = 100;

    private PlayerHealthBar healthBar;
    private Pane gameObjectsPane;
    private VBox gameOverOverlay;
    /**
     * Called to initialize the controller after its root element has been completely processed.
     * Initializes the game map, camera, viewport rendering, button handlers, and key listeners.
     *
     * @param location  The location used to resolve relative paths for the root object.
     * @param resources The resources used to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Create and copy map
        baseMap = initMap(30, 30);
        fieldMap = copyMap(baseMap);
        pageSwitch=new SwitchPage();

        // Create player
        player = new Player(15, 15, PLAYER_MAX_HEALTH);

        // Initialize camera
        camera = new Camera(fieldMap, 15, 15, player);

        // Pane for dynamic objects (e.g. arrows)
        gameObjectsPane = new Pane();
        gameObjectsPane.setLayoutX(0);
        gameObjectsPane.setLayoutY(0);
        gameObjectsPane.setTranslateX(0);
        gameObjectsPane.setTranslateY(0);
        gameObjectsPane.setPrefSize(15 * TILE_SIZE, 15 * TILE_SIZE);
        gameObjectsPane.setPickOnBounds(false);

        updateMapWithMonsters();

        // Health bar setup
        healthBar = new PlayerHealthBar(200, 20);
        healthBarContainer.getChildren().add(healthBar);
        updatePlayerHealthBar();

        centerStack.setAlignment(Pos.CENTER);
        updateCameraViewport();

        centerMenuButton.setOnAction(e -> showCenterMenu());

        // Register keyboard listeners
        rootBorder.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                registerKeyHandlers(newScene);
            }
        });

        startMonsterMovement();

        Coin coin = new Coin(9, 9, 100);
        coin.updateScreenPosition(camera.getStartCol(), camera.getStartRow());
        coinsOnMap.add(coin);
        gameObjectsPane.getChildren().add(coin.getImageView());
    }


     /* Initializes a map of given size filled with random values and surrounded by a border of mountains.
     *
     * @param rows number of rows in the map
     * @param cols number of columns in the map
     * @return a 2D integer array representing the initialized map
     */
    private int[][] initMap(int rows, int cols) {
        Random random = new Random();
        int[][] map = new int[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int[] possibleValues = {0, 1, 2, 3, 4, 5}; // EMPTY, MOUNTAIN, TREE, ROCK, RIVER, VILLAGER
                map[row][col] = possibleValues[random.nextInt(possibleValues.length)];
            }
        }

        // Add monsters to the list
        monsterList.add(MonsterFactory.createMonster(MonsterType.GOBLIN, 10, 10));
        monsterList.add(MonsterFactory.createMonster(MonsterType.DEMON, 5, 15));
        monsterList.add(MonsterFactory.createMonster(MonsterType.SKELETON, 2, 15));

        return map;
    }

    /**
     * Makes a deep copy of the 2D map array.
     */
    private int[][] copyMap(int[][] src) {
        return Arrays.stream(src).map(int[]::clone).toArray(int[][]::new);
    }


    /**
     * Updates fieldMap by copying the base map and then placing monsters (their values)
     * into the corresponding cells.
     */
    private void updateMapWithMonsters() {
        fieldMap = copyMap(baseMap);
        for (Monster monster : monsterList) {
            int x = monster.getX();
            int y = monster.getY();
            if (y >= 0 && y < fieldMap.length && x >= 0 && x < fieldMap[0].length) {
                fieldMap[y][x] = monster.getMonsterValue();
            }
        }
        camera.setMapGrid(fieldMap);
    }

    /**
     * Registers key event handlers on the Scene for player movement.
     */
    private void registerKeyHandlers(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (centerMenuPopup != null && centerMenuPopup.isShowing()) return;

            KeyCode code = event.getCode();
            switch (code) {
                case LEFT, A -> player.moveLeft();
                case RIGHT, D -> player.moveRight();
                case UP, W -> player.moveUp();
                case DOWN, S -> player.moveDown();
                case SPACE -> {
                    List<Monster> eliminated = player.attack(monsterList, 1, 20);
                    removeMonsters(eliminated);
                }
                default -> {}
            }
            checkCoinPickup();
            camera.updateCameraCenter();
            updateCameraViewport();
        });
        scene.getRoot().requestFocus();
    }

    private void checkCoinPickup() {
        int px = player.getX();
        int py = player.getY();

        List<Coin> collected = new ArrayList<>();

        for (Coin coin : coinsOnMap) {
            if (coin.getGridX() == px && coin.getGridY() == py) {
                player.addCoin(coin.getValue());
                gameObjectsPane.getChildren().remove(coin.getImageView());
                collected.add(coin);
            }
        }

        coinsOnMap.removeAll(collected);
        updateCoinLabel();
    }

    private void updateCoinLabel() {
        if (coinCountLabel != null) {
            coinCountLabel.setText("Coins: " + player.getPlayerCoins());
        }
    }

    private void removeMonsters(List<Monster> eliminated) {
        for (Monster monster : eliminated) {
            int x = monster.getX();
            int y = monster.getY();

            baseMap[y][x] = TypeOfObjects.FLOOR.getValue();

            Coin coin = new Coin(x, y, monster.getCost());
            coin.updateScreenPosition(camera.getStartCol(), camera.getStartRow());
            coinsOnMap.add(coin);
            gameObjectsPane.getChildren().add(coin.getImageView());
        }

        monsterList.removeAll(eliminated);
    }


    /**
     * Updates the camera's viewport display.
     */
    private void updateCameraViewport() {
        GridPane cameraViewport = camera.getViewport();

        int startCol = camera.getStartCol();
        int startRow = camera.getStartRow();

        gameObjectsPane.getChildren().clear();

        for (Coin coin : coinsOnMap) {
            if (coin.getGridX() >= startCol && coin.getGridX() < startCol + 15 &&
                    coin.getGridY() >= startRow && coin.getGridY() < startRow + 15) {

                coin.updateScreenPosition(startCol, startRow);
                gameObjectsPane.getChildren().add(coin.getImageView());
            }
        }

        if (centerStack.getChildren().isEmpty()) {
            centerStack.getChildren().addAll(cameraViewport, gameObjectsPane);
        } else {
            centerStack.getChildren().set(0, cameraViewport);
            if (!centerStack.getChildren().contains(gameObjectsPane)) {
                centerStack.getChildren().add(gameObjectsPane);
            }
        }
    }

    /**
     * Game loop: moves monsters, triggers skeleton attacks, checks collisions.
     */
    private void startMonsterMovement() {
        monsterMovementTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastMonsterUpdateTime == 0) {
                    lastMonsterUpdateTime = now;
                    lastSkeletonAttackTime = now;
                    return;
                }

                if (now - lastMonsterUpdateTime >= MONSTER_UPDATE_INTERVAL_NANOS) {
                    handleMonsterMovement();
                    lastMonsterUpdateTime = now;
                }

                if (now - lastSkeletonAttackTime >= SKELETON_ATTACK_INTERVAL_NANOS) {
                    handleSkeletonAttacks();
                    lastSkeletonAttackTime = now;
                }

                checkArrowCollisionsWithPlayer();
            }
        };
        monsterMovementTimer.start();
    }

    /**
     * Moves all monsters on the map and refreshes the view.
     */
    private void handleMonsterMovement() {
        int rows = fieldMap.length;
        int cols = fieldMap[0].length;

        fieldMap = copyMap(baseMap);
        for (Monster monster : monsterList) {
            int oldX = monster.getX();
            int oldY = monster.getY();
            monster.move();

            if (monster.getX() < 0 || monster.getX() >= cols ||
                    monster.getY() < 0 || monster.getY() >= rows) {
                monster.setPosition(oldX, oldY);
            }
        }

        updateMapWithMonsters();
        updateCameraViewport();
    }

    /**
     * Skeleton attack logic (should be moved to Skeleton class).
     */
    private void handleSkeletonAttacks() {
        // tile_92.png  arrow
        for (Monster monster : monsterList) {
            // TODO: My shit code is below, don't touch it, I'll fix it. I can't do without it now, so help me God
            if (monster instanceof Skeleton skeleton) {
                int sx = skeleton.getX();
                int sy = skeleton.getY();

                int startCol = camera.getStartCol();
                int startRow = camera.getStartRow();

                double screenSkeletonX = (sx - startCol) * TILE_SIZE;
                double screenSkeletonY = (sy - startRow) * TILE_SIZE;
                double arrowWidth = 10, arrowHeight = 3;
                double offsetX = (TILE_SIZE - arrowWidth) / 2.0;
                double offsetY = (TILE_SIZE - arrowHeight) / 2.0;

                Arrow arrow = new Arrow(
                        screenSkeletonX + offsetX,
                        screenSkeletonY + offsetY,
                        screenSkeletonX + offsetX + 5 * TILE_SIZE,
                        screenSkeletonY + offsetY,
                        5
                );

                arrow.launch();
                gameObjectsPane.getChildren().add(arrow.getNode());
            }
        }
    }

    /**
     * Checks collisions between arrows and the player.
     */
    private void checkArrowCollisionsWithPlayer() {
        int globalPlayerX = player.getX();
        int globalPlayerY = player.getY();
        int startCol = camera.getStartCol();
        int startRow = camera.getStartRow();

        double playerScreenX = (globalPlayerX - startCol) * TILE_SIZE;
        double playerScreenY = (globalPlayerY - startRow) * TILE_SIZE;

        List<Node> arrowsToRemove = new ArrayList<>();
        for (Node arrowNode : gameObjectsPane.getChildren()) {
            if (arrowNode.getBoundsInParent().intersects(playerScreenX, playerScreenY, TILE_SIZE, TILE_SIZE)) {
                player.takeDamage(10);
                updatePlayerHealthBar();
                arrowsToRemove.add(arrowNode);
            }
        }

        gameObjectsPane.getChildren().removeAll(arrowsToRemove);
    }


    /**
     * Shows the center menu with sample buttons.
     * Shows the center menu popup at the center of the scene.
     * The popup displays a title and two buttons: "Option 1" and "Go Back".
     * "Go Back" loads the main menu scene.
     */
    private void showCenterMenu() {
        if (centerMenuPopup == null) {
            centerMenuPopup = new Popup();
            centerMenuPopup.setAutoHide(true);

            VBox menuBox = new VBox(20);
            menuBox.getStylesheets().add(getClass().getResource("/com/gnome/gnome/pages/css/continueGame.css").toExternalForm());
            menuBox.setAlignment(Pos.CENTER);
            menuBox.setStyle("-fx-background-color: #C0C0C0; -fx-padding: 20; -fx-background-radius: 20;");
            menuBox.getStyleClass().add("menu-popup");

            Label title = new Label("MENU");
            title.getStyleClass().add("menu-title");

            Button option1 = new Button("Option 1");
            Button goBackButton = new Button("Go Back");
            option1.getStyleClass().add("menu-button");
            goBackButton.getStyleClass().add("menu-button");

            option1.setOnAction(e -> centerMenuPopup.hide());

            goBackButton.setOnAction(e -> {
                logger.info("Go Back clicked. Redirecting to main page.");
                centerMenuPopup.hide();
                pageSwitch.goMainMenu(rootBorder);
            });

            menuBox.getChildren().addAll(title, option1, goBackButton);
            centerMenuPopup.getContent().add(menuBox);
        }

        Scene scene = centerMenuButton.getScene();
        if (scene != null) {
            Bounds bounds = scene.getRoot().localToScreen(scene.getRoot().getBoundsInLocal());
            centerMenuPopup.show(scene.getWindow(),
                    bounds.getMinX() + bounds.getWidth() / 2 - 100,
                    bounds.getMinY() + bounds.getHeight() / 2 - 75);
        }
    }

    /**
     * Updates the health bar and shows Game Over if health reaches 0.
     */
    private void updatePlayerHealthBar() {
        double fraction = (double) player.getCurrentHealth() / player.getMaxHealth();
        healthBar.setHealthFraction(fraction);

        if (player.getCurrentHealth() <= 0) {
            showGameOverOverlay();
        }
    }


    /**
     * Displays Game Over screen and stops the game loop.
     */
    private void showGameOverOverlay() {
        if (gameOverOverlay != null) return;

        monsterMovementTimer.stop();

        gameOverOverlay = new VBox(20);
        gameOverOverlay.setAlignment(Pos.CENTER);
        gameOverOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        gameOverOverlay.prefWidthProperty().bind(centerStack.widthProperty());
        gameOverOverlay.prefHeightProperty().bind(centerStack.heightProperty());

        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: red;");

        Button exitButton = new Button("Exit");
        Button restartButton = new Button("Restart");

        exitButton.setOnAction(e -> Platform.exit());
        restartButton.setOnAction(e -> restartGame());

        gameOverOverlay.getChildren().addAll(gameOverLabel, restartButton, exitButton);
        centerStack.getChildren().add(gameOverOverlay);
    }

    /**
     * Reloads the FXML view to restart the game.
     */
    private void restartGame() {
        try {
            URL fxmlUrl = getClass().getResource("/com/gnome/gnome/pages/continue-game.fxml");
            Parent newRoot = FXMLLoader.load(Objects.requireNonNull(fxmlUrl));
            Stage stage = (Stage) rootBorder.getScene().getWindow();
            stage.getScene().setRoot(newRoot);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
