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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    /**
     * Container for the player's health bar.
     */
    @FXML private StackPane healthBarContainer;

    /**
     * The canvas used for rendering the visible portion of the map.
     */
    private Canvas viewportCanvas;

    /**
     * Label showing the number of coins collected by the player.
     */
    @FXML private Label coinCountLabel;

    /**
     * The initial game map (static terrain).
     */
    private int[][] baseMap;
    /**
     * The active game map including monsters and dynamic elements.
     */
    private int[][] fieldMap;
    private PageSwitcherInterface pageSwitch;

    /** The full 30x30 map grid representing the game world. */
    private int[][] field30x30;
    private Camera camera;
    /**
     * Popup menu shown when the center menu button is clicked.
     */
    private Popup centerMenuPopup;
    /**
     * List of all active monsters in the game.
     */
    private final List<Monster> monsterList = new ArrayList<>();
    /**
     * List of coins currently present on the map.
     */
    private final List<Coin> coinsOnMap = new ArrayList<>();
    /**
     * List of arrows currently active in the game.
     */
    private final List<Arrow> activeArrows = new ArrayList<>();

    private static final Logger logger = Logger.getLogger(ContinueGameController.class.getName());

    /**
     * Timer for updating monster behavior periodically.
     */
    private AnimationTimer monsterMovementTimer;
    /**
     * Time interval (in nanoseconds) for updating monsters.
     */
    private final long MONSTER_UPDATE_INTERVAL_NANOS = 1_000_000_000L;
    /**
     * Last time monsters were updated.
     */
    private long lastMonsterUpdateTime = 0;

    // Player instance
    private Player player;
    private static final int PLAYER_MAX_HEALTH = 100;

    /**
     * The player's health bar UI component.
     */
    private PlayerHealthBar healthBar;
    /**
     * Pane containing game objects such as coins.
     */
    private Pane gameObjectsPane;
    /**
     * Overlay shown when the game is over.
     */
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
        baseMap     = initMap(30, 30);
        fieldMap    = copyMap(baseMap);
        player      = new Player(15, 15, PLAYER_MAX_HEALTH);
        camera      = new Camera(fieldMap, player.getX(), player.getY(), player);
        updateMapWithMonsters();
        pageSwitch=new SwitchPage();


        double vw = 15 * TILE_SIZE;
        double vh = 15 * TILE_SIZE;

        viewportCanvas = new Canvas(vw, vh);

        gameObjectsPane = new Pane();
        gameObjectsPane.setPrefSize(vw, vh);
        gameObjectsPane.setPickOnBounds(false);

        Pane viewportRoot = new Pane(viewportCanvas, gameObjectsPane);
        viewportRoot.setPrefSize(vw, vh);

        centerStack.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
        centerStack.setAlignment(Pos.CENTER);
        centerStack.getChildren().setAll(viewportRoot);
        StackPane.setAlignment(viewportRoot, Pos.CENTER);

        centerStack.widthProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("centerStack width: " + newVal);
        });
        centerStack.heightProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("centerStack height: " + newVal);
        });

//        viewportRoot.layoutXProperty().bind(
//                centerStack.widthProperty().subtract(vw).divide(2)
//        );
//        viewportRoot.layoutYProperty().bind(
//                centerStack.heightProperty().subtract(vh).divide(2)
//        );

        healthBar = new PlayerHealthBar(200, 20);
        healthBarContainer.getChildren().add(healthBar);
        updatePlayerHealthBar();

        coinsOnMap.add(new Coin(9, 9, 100));
        updateCoinLabel();

        centerMenuButton.setOnAction(e -> showCenterMenu());
        rootBorder.sceneProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) registerKeyHandlers(newS);
        });

        startMonsterMovement();
        updateCameraViewport();
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
                int[] possibleValues = {0, 1, 2, 3, 4}; // EMPTY, MOUNTAIN, TREE, ROCK, RIVER
                map[row][col] = possibleValues[random.nextInt(possibleValues.length)];
            }
        }

        // Add monsters to the list
        monsterList.add(MonsterFactory.createMonster(MonsterType.SKELETON, 10, 10));
        monsterList.add(MonsterFactory.createMonster(MonsterType.SKELETON, 5, 15));
        monsterList.add(MonsterFactory.createMonster(MonsterType.SKELETON, 8, 8));

        return map;
    }

    /**
     * Makes a deep copy of the 2D map array.
     */
    private int[][] copyMap(int[][] src) {
        int rows = src.length;
        int[][] dest = new int[rows][];
        for (int i = 0; i < rows; i++) {
            dest[i] = new int[src[i].length];
            System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
        }
        return dest;
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

            int maxRow = baseMap.length;
            int maxCol = baseMap[0].length;

            int oldX = player.getX();
            int oldY = player.getY();
            int newX = oldX;
            int newY = oldY;

            switch (event.getCode()) {
                case LEFT, A -> {
                    if (oldX > 0) newX = oldX - 1;
                }
                case RIGHT, D -> {
                    if (oldX < maxCol - 1) newX = oldX + 1;
                }
                case UP, W -> {
                    if (oldY > 0) newY = oldY - 1;
                }
                case DOWN, S -> {
                    if (oldY < maxRow - 1) newY = oldY + 1;
                }
                case SPACE -> {
                    List<Monster> eliminated = player.attack(monsterList, 1, 20);
                    removeMonsters(eliminated);
                }
                default -> { /* no movement */ }
            }

            if (newX != oldX || newY != oldY) {
                int tileValue = fieldMap[newY][newX];
                if (tileValue < 0) {
                    tileValue = baseMap[newY][newX];
                }
                TypeOfObjects tileType = TypeOfObjects.fromValue(tileValue);

                if (tileType == TypeOfObjects.FLOOR || tileType == TypeOfObjects.HATCH || tileType == TypeOfObjects.RIVER) {
                    if (newX < oldX) player.moveLeft();
                    else if (newX > oldX) player.moveRight();
                    else if (newY < oldY) player.moveUp();
                    else if (newY > oldY) player.moveDown();

                    if (tileType == TypeOfObjects.HATCH) {
                        onHatchStepped();
                    } else if (tileType == TypeOfObjects.RIVER) {
                        onRiverStepped();
                    }


                    checkCoinPickup();
                    camera.updateCameraCenter();
                    updateCameraViewport();
                } else {
                    System.out.println("Player cannot move to (" + newX + ", " + newY + ") - tile type: " + tileType);
                }
            }
        });
        scene.getRoot().requestFocus();
    }

    // TODO: Here we need to implement shop
    private void onHatchStepped() {

    }

    /**
     * Called when the player steps on a river tile.
     * Deals damage to the player.
     */
    private void onRiverStepped() {
        System.out.println("Player stepped on RIVER at (" + player.getX() + ", " + player.getY() + ") - Taking 5 damage");
        player.takeDamage(player.getCurrentHealth());
        updatePlayerHealthBar();
    }

    /**
     * Checks whether the player is standing on a coin and collects it if found.
     */
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

    /**
     * Updates the coin counter label to reflect collected coins.
     */
    private void updateCoinLabel() {
        if (coinCountLabel != null) {
            coinCountLabel.setText("Coins: " + player.getPlayerCoins());
        }
    }

    /**
     * Removes the specified monsters and replaces them with coins.
     *
     * @param eliminated a list of monsters to remove
     */
    private void removeMonsters(List<Monster> eliminated) {
        for (Monster monster : eliminated) {
            int x = monster.getX();
            int y = monster.getY();
            fieldMap[y][x] = TypeOfObjects.FLOOR.getValue();
            Coin coin = new Coin(x, y, monster.getCost());
            coinsOnMap.add(coin);
        }

        monsterList.removeAll(eliminated);
        updateCameraViewport();
    }


    /**
     * Updates the camera's viewport display.
     */
    private void updateCameraViewport() {
        camera.updateCameraCenter();
        camera.drawViewport(viewportCanvas, coinsOnMap);

        if (!gameObjectsPane.getChildren().contains(player.getRepresentation())) {
            gameObjectsPane.getChildren().add(player.getRepresentation());
        }

        int playerGridX = player.getX() - camera.getStartCol(); //  X position relative to the start of the viewport
        int playerGridY = player.getY() - camera.getStartRow(); // Y position relative to the start of the viewport
        double pixelX = playerGridX * TILE_SIZE; // Convert to pixels
        double pixelY = playerGridY * TILE_SIZE;

        player.getRepresentation().setTranslateX(pixelX);
        player.getRepresentation().setTranslateY(pixelY);

        for (Arrow arrow : activeArrows) {
            arrow.updateCameraOffset(camera.getStartCol(), camera.getStartRow());
        }

        updateCoinLabel();
        updatePlayerHealthBar();
    }


    /**
     * Game loop: moves monsters, triggers skeleton attacks, checks collisions.
     */
    private void startMonsterMovement() {
        monsterMovementTimer = new AnimationTimer() {
            private long lastTime = 0;
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                updateCameraViewport();

                if (now - lastMonsterUpdateTime >= MONSTER_UPDATE_INTERVAL_NANOS) {
                    handleMonsterMovement();
                    lastMonsterUpdateTime = now;
                }

                for (Monster m : monsterList) {
                    if (m instanceof Skeleton s) {
                        Arrow arrow = s.attack(camera.getStartCol(), camera.getStartRow(), player.getX(), player.getY());
                        if (arrow != null) {
                            arrow.setSkeleton(s);
                            activeArrows.add(arrow);
                            arrow.shoot(gameObjectsPane, player);
                        }
                    }
                }

                activeArrows.removeIf(arrow -> !gameObjectsPane.getChildren().contains(arrow.getView()));

                lastTime = now;
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
        List<Monster> toRemove = new ArrayList<>();

        for (Monster monster : monsterList) {
            int oldX = monster.getX();
            int oldY = monster.getY();
            int newX = oldX;
            int newY = oldY;

            monster.move();
            newX = monster.getX();
            newY = monster.getY();

            if (newX < 0 || newX >= cols || newY < 0 || newY >= rows) {
                monster.setPosition(oldX, oldY);
                continue;
            }

            int tileValue = fieldMap[newY][newX];
            if (tileValue < 0) {
                tileValue = baseMap[newY][newX];
            }
            TypeOfObjects tileType = TypeOfObjects.fromValue(tileValue);

            if (tileType == TypeOfObjects.FLOOR || tileType == TypeOfObjects.HATCH || tileType == TypeOfObjects.RIVER) {
                if (tileType == TypeOfObjects.RIVER) {
                    System.out.println("Monster at (" + oldX + ", " + oldY + ") stepped on RIVER at (" + newX + ", " + newY + ") - Monster dies");
                    toRemove.add(monster);
                }
            } else {
                System.out.println("Monster cannot move to (" + newX + ", " + newY + ") - tile type: " + tileType);
                monster.setPosition(oldX, oldY);
            }
        }

        removeMonsters(toRemove);

        updateMapWithMonsters();
        updateCameraViewport();
    }

    /**
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
        Platform.runLater(() -> {
            double fraction = (double) player.getCurrentHealth() / player.getMaxHealth();
            healthBar.setHealthFraction(fraction);

            if (player.getCurrentHealth() <= 0) {
                showGameOverOverlay();
            }
        });
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
