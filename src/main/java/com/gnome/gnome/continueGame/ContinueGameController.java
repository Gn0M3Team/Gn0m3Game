package com.gnome.gnome.continueGame;

import com.gnome.gnome.camera.Camera;
import com.gnome.gnome.components.PlayerHealthBar;
import com.gnome.gnome.continueGame.component.Coin;
import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.MonsterFactory;
import com.gnome.gnome.monsters.movements.FollowingMovement;
import com.gnome.gnome.monsters.types.Skeleton;
import com.gnome.gnome.monsters.types.missels.Arrow;
import com.gnome.gnome.player.Player;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
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
    @Getter
    private int[][] baseMap;
    /**
     * The active game map including monsters and dynamic elements.
     */
    private int[][] fieldMap;
    /**
     * Camera object for managing viewport rendering.
     */
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
    @Getter
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

    private static ContinueGameController instance; //Singleton Instance

    public static ContinueGameController getContinueGameController(){
        //Get ONLY initialized instance
        if (ContinueGameController.instance == null)
            return null;
        return ContinueGameController.instance;
    }

    private boolean debug_mod_game;

    private double dynamicTileSize;

    /**
     * Called to initialize the controller after its root element has been completely processed.
     * Initializes the game map, camera, viewport rendering, button handlers, and key listeners.
     *
     * @param location  The location used to resolve relative paths for the root object.
     * @param resources The resources used to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupProperties();

        baseMap = initMap(30, 30);
        fieldMap = copyMap(baseMap);
        player = Player.getInstance(15, 15, PLAYER_MAX_HEALTH);
        camera = Camera.getInstance(fieldMap, player.getX(), player.getY(), player);
        updateMapWithMonsters();
        instance = this;

        viewportCanvas = new Canvas();
        viewportCanvas.widthProperty().bind(centerStack.widthProperty());
        viewportCanvas.heightProperty().bind(centerStack.heightProperty());

        gameObjectsPane = new Pane();
        gameObjectsPane.prefWidthProperty().bind(centerStack.widthProperty());
        gameObjectsPane.prefHeightProperty().bind(centerStack.heightProperty());
        gameObjectsPane.setPickOnBounds(false);

        Pane viewportRoot = new Pane(viewportCanvas, gameObjectsPane);
        centerStack.setAlignment(Pos.CENTER);
        centerStack.getChildren().setAll(viewportRoot);

        healthBar = new PlayerHealthBar(200, 20);
        healthBarContainer.getChildren().add(healthBar);
        StackPane.setMargin(healthBar, new Insets(20, 20, 0, 0));

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

    /**
     * Loads properties from the app.properties file to configure the game.
     * Specifically, it loads the debug mode setting (app.skip_login).
     * If the properties file cannot be loaded, an exception is thrown.
     */
    private void setupProperties() {
        Properties properties = new Properties();
        // Load the app.properties file from the classpath
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("app.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Could not find app.properties in classpath");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Could not load properties file", e);
        }

        // Read the debug mode setting (app.skip_login) and convert it to a boolean
        debug_mod_game = Boolean.parseBoolean(properties.getProperty("app.skip_login"));
    }

    /**
     * Initializes a game map of the specified size with random terrain tiles.
     * The map is filled with empty tiles (value 0) for simplicity.
     * Monsters are added to the monsterList (but not directly to the map here).
     *
     * @param rows The number of rows in the map (e.g., 30).
     * @param cols The number of columns in the map (e.g., 30).
     * @return A 2D integer array representing the initialized map.
     */
    private int[][] initMap(int rows, int cols) {
        Random random = new Random();
        int[][] map = new int[rows][cols];

        // Fill the map with random terrain tiles
        // Currently, only EMPTY tiles (value 0) are used for simplicity
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                map[row][col] = 0;
            }
        }

        // Add monsters to the monsterList (commented-out code shows examples of adding skeletons)
        // Currently, only one goblin is added at position (8, 8)
        monsterList.add(MonsterFactory.createMonster(TypeOfObjects.SKELETON, 10, 10));
//        monsterList.add(MonsterFactory.createMonster(MonsterType.SKELETON, 5, 15));
//        monsterList.add(MonsterFactory.createMonster(MonsterType.SKELETON, 8, 8));
        monsterList.add(MonsterFactory.createMonster(TypeOfObjects.GOBLIN, 8, 8));
        return map;
    }

    /**
     * Creates a deep copy of a 2D map array.
     * This is used to create the fieldMap, which can be modified without affecting the baseMap.
     *
     * @param src The source 2D array to copy.
     * @return A new 2D array that is a deep copy of the source.
     */
    private int[][] copyMap(int[][] src) {
        int rows = src.length;
        int[][] dest = new int[rows][];
        // Copy each row of the source array to the destination array
        for (int i = 0; i < rows; i++) {
            dest[i] = new int[src[i].length];
            System.arraycopy(src[i], 0, dest[i], 0, src[i].length);
        }
        return dest;
    }


    /**
     * Updates the fieldMap by copying the baseMap and adding the current positions of all monsters.
     * Each monster's position is marked on the fieldMap with its monster value (e.g., -1 for a goblin).
     */
    private void updateMapWithMonsters() {
        // Create a fresh copy of the baseMap
        fieldMap = copyMap(baseMap);

        // Iterate through all monsters and place them on the fieldMap
        for (Monster monster : monsterList) {
            int x = monster.getX(); // Monster's X position
            int y = monster.getY(); // Monster's Y position
            // Check if the monster's position is within the map bounds
            if (y >= 0 && y < fieldMap.length && x >= 0 && x < fieldMap[0].length) {
                // Set the map cell to the monster's value
                fieldMap[y][x] = monster.getMonsterValue();
            }
        }

        // Update the camera with the new fieldMap so it can render the updated map
        camera.setMapGrid(fieldMap);
    }

    /**
     * Registers key event handlers on the Scene to handle player input (e.g., movement, attacking).
     * The player can move using W, A, S, D (or arrow keys) and attack using the spacebar.
     *
     * @param scene The Scene object to register the key handlers on.
     */
    private void registerKeyHandlers(Scene scene) {
        // Add a key press handler to the scene
        scene.setOnKeyPressed(event -> {
            // If the center menu popup is showing, ignore key presses (player cannot move while the menu is open)
            if (centerMenuPopup != null && centerMenuPopup.isShowing()) return;

            // Get the dimensions of the map to ensure the player stays within bounds
            int maxRow = baseMap.length; // Number of rows in the map
            int maxCol = baseMap[0].length; // Number of columns in the map

            // Store the player's current position
            int oldX = player.getX();
            int oldY = player.getY();
            // Initialize the new position as the current position (will be updated based on input)
            int newX = oldX;
            int newY = oldY;

            // Handle key presses to determine the new position or perform an action
            switch (event.getCode()) {
                case LEFT, A -> {
                    if (oldX > 0) newX = oldX - 1; // Only move if not at the left edge of the map
                }
                case RIGHT, D -> {
                    if (oldX < maxCol - 1) newX = oldX + 1; // Only move if not at the right edge
                }
                case UP, W -> {
                    if (oldY > 0) newY = oldY - 1; // Only move if not at the top edge
                }
                case DOWN, S -> {
                    if (oldY < maxRow - 1) newY = oldY + 1; // Only move if not at the bottom edge
                }
                case SPACE -> {
                    // Check if the gameObjectsPane is initialized (needed for attack effects)
                    if (gameObjectsPane == null) {
                        System.err.println("Error: gameObjectsPane is null when trying to attack");
                        return;
                    }
                    // Show a visual indicator of the player's attack range (1 tile in all directions)
                    showAttackRange(1, camera.getStartCol(), camera.getStartRow());
                    // Perform the attack, dealing 20 damage to monsters within 1 tile
                    // The callback removes any monsters that are killed by the attack
                    player.attack(monsterList, 1, 20, gameObjectsPane, camera.getStartCol(), camera.getStartRow(), monster -> {
                        removeMonsters(Collections.singletonList(monster));
                    });
                }
                default -> { /* no actions */ }
            }

            // If the player's position has changed (i.e., they moved), process the movement
            if (newX != oldX || newY != oldY) {
                // Check if the new position is occupied by a monster
                boolean isOccupiedByMonster = false;
                for (Monster monster : monsterList) {
                    if (monster.getX() == newX && monster.getY() == newY) {
                        isOccupiedByMonster = true;
                        break;
                    }
                }

                // If the new position has a monster, the player cannot move there
                if (isOccupiedByMonster) {
                    if (debug_mod_game) {
                        System.out.println("Player cannot move to (" + newX + ", " + newY + ") - position occupied by a monster");
                    }
                    return;
                }

                // Get the tile type at the new position
                int tileValue = fieldMap[newY][newX];
                // If the tile value is negative (indicating a monster), get the underlying base map value
                if (tileValue < 0) {
                    tileValue = baseMap[newY][newX];
                }
                TypeOfObjects tileType = TypeOfObjects.fromValue(tileValue);

                // Check if the tile is walkable (FLOOR, HATCH, RIVER, or EMPTY)
                if (tileType == TypeOfObjects.FLOOR || tileType == TypeOfObjects.FINISH_POINT || tileType == TypeOfObjects.RIVER || tileType == TypeOfObjects.EMPTY) {
                    // Update the player's position based on the direction of movement
                    if (newX < oldX) player.moveLeft();
                    else if (newX > oldX) player.moveRight();
                    else if (newY < oldY) player.moveUp();
                    else if (newY > oldY) player.moveDown();

                    // Handle special tile interactions
                    if (tileType == TypeOfObjects.FINISH_POINT) {
                        onHatchStepped(); // Handle stepping on a hatch (currently a placeholder)
                    } else if (tileType == TypeOfObjects.RIVER) {
                        onRiverStepped(); // Handle stepping on a river (deals damage to the player)
                    }

                    // Check if the player has picked up a coin at the new position
                    checkCoinPickup();

                    // Update the camera to follow the player and redraw the viewport
                    camera.updateCameraCenter();
                    updateCameraViewport();
                } else {
                    if (debug_mod_game)
                        System.out.println("Player cannot move to (" + newX + ", " + newY + ") - tile type: " + tileType);
                }
            }
        });

        // Request focus on the scene's root to ensure it receives key events
        scene.getRoot().requestFocus();
    }

    // TODO: Here we need to implement shop
    private void onHatchStepped() {

    }

    /**
     * Handles the player stepping on a river tile.
     * The player takes damage equal to their current health, effectively killing them.
     */
    private void onRiverStepped() {
        if (debug_mod_game)
            System.out.println("Player stepped on RIVER at (" + player.getX() + ", " + player.getY() + ")");
        // Deal damage to the player equal to their current health (instant death)
        player.takeDamage(player.getCurrentHealth());
        // Update the health bar to reflect the damage (will trigger game over if health reaches 0)
        updatePlayerHealthBar();
    }

    /**
     * Checks whether the player is standing on a coin and collects it if found.
     */
    private void checkCoinPickup() {
        int px = player.getX(); // Player's X position
        int py = player.getY(); // Player's Y position
        List<Coin> collected = new ArrayList<>(); // List to store coins that the player collects

        // Iterate through all coins on the map
        for (Coin coin : coinsOnMap) {
            // Check if the coin is at the player's position
            if (coin.getGridX() == px && coin.getGridY() == py) {
                player.addCoin(coin.getValue()); // Add the coin's value to the player's total
                gameObjectsPane.getChildren().remove(coin.getImageView()); // Remove the coin's visual representation from the scene
                collected.add(coin); // Mark the coin as collected
            }
        }

        // Remove all collected coins from the map
        coinsOnMap.removeAll(collected);
        // Update the coin counter label to reflect the new total
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
     * Removes the specified monsters from the game and replaces them with coins.
     * The monsters are removed from the monsterList, and their positions on the fieldMap are updated to FLOOR tiles.
     *
     * @param eliminated A list of monsters to remove (e.g., monsters killed by the player's attack).
     */
    private void removeMonsters(List<Monster> eliminated) {
        // Iterate through all monsters to be removed
        for (Monster monster : eliminated) {
            int x = monster.getX(); // Monster's X position
            int y = monster.getY(); // Monster's Y position
            //  Set the monster's position on the fieldMap to a FLOOR tile (removing the monster)
            fieldMap[y][x] = TypeOfObjects.FLOOR.getValue();
            // Create a new coin at the monster's position with the monster's coin value (cost)
            Coin coin = new Coin(x, y, monster.getCost());
            coinsOnMap.add(coin); // Add the coin to the map
            monsterList.remove(monster); // Remove the monster from the list
        }

        // Update the fieldMap with the remaining monsters
        updateMapWithMonsters();
        // Redraw the viewport to reflect the changes (e.g., monster removed, coin added)
        updateCameraViewport();
    }


    /**
     * Updates the camera's viewport to render the current state of the game.
     * This includes drawing the map, updating the player's position, repositioning effects, and updating UI elements.
     */
    private void updateCameraViewport() {
        // Update camera center
        camera.updateCameraCenter();

        // Draw map and coins
        camera.drawViewport(viewportCanvas, coinsOnMap);

        // Update player's dynamic tile size
        player.setDynamicTileSize(camera.getDynamicTileSize());

        double dynamicTileSize = camera.getDynamicTileSize();
        double playerSize = dynamicTileSize * 0.6;
        double playerOffset = (dynamicTileSize - playerSize) / 2.0;

        // Calculate player centered on canvas
        double centerOffsetX = viewportCanvas.getWidth() / 2 - playerSize / 2;
        double centerOffsetY = viewportCanvas.getHeight() / 2 - playerSize / 2;

        // Set player position
        player.getRepresentation().setTranslateX(centerOffsetX);
        player.getRepresentation().setTranslateY(centerOffsetY);

        // Update player rectangle size
        if (player.getRepresentation() instanceof Rectangle rect) {
            rect.setWidth(playerSize);
            rect.setHeight(playerSize);
        }

        // Add player to the scene if not yet added
        if (!gameObjectsPane.getChildren().contains(player.getRepresentation())) {
            gameObjectsPane.getChildren().add(player.getRepresentation());
        }

        // Update active visual effects (e.g., attacks, hit animations)
        for (var node : gameObjectsPane.getChildren()) {
            if (node instanceof ImageView effectView) {
                Object absoluteX = effectView.getProperties().get("absoluteX");
                Object absoluteY = effectView.getProperties().get("absoluteY");
                if (absoluteX instanceof Double absX && absoluteY instanceof Double absY) {
                    double offsetX = (absX - camera.getStartCol()) * dynamicTileSize + playerOffset;
                    double offsetY = (absY - camera.getStartRow()) * dynamicTileSize + playerOffset;
                    effectView.setTranslateX(offsetX);
                    effectView.setTranslateY(offsetY);
                }
            }
        }

        // Update all active arrows
        for (Arrow arrow : activeArrows) {
            arrow.updateCameraOffset(camera.getStartCol(), camera.getStartRow());
        }

        updateCoinLabel();
        updatePlayerHealthBar();
    }



    /**
     * Starts the game loop using an AnimationTimer.
     * The game loop updates the viewport, moves monsters, triggers skeleton attacks, and manages active arrows.
     */
    private void startMonsterMovement() {
        // Create a new AnimationTimer for the game loop
        monsterMovementTimer = new AnimationTimer() {
            private long lastTime = 0; // The timestamp of the last frame, used to calculate the time delta
            @Override
            public void handle(long now) {
                // On the first frame, initialize the lastTime and exit
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                // Update the viewport to reflect the current game state
                updateCameraViewport();

                // Check if it's time to update monster behavior (every 1 second)
                if (now - lastMonsterUpdateTime >= MONSTER_UPDATE_INTERVAL_NANOS) {
                    handleMonsterMovement(now); // Move monsters and handle their attacks
                    lastMonsterUpdateTime = now; // Update the last update timestamp
                }

                // Handle skeleton attacks (skeletons shoot arrows)
                for (Monster m : monsterList) {
                    if (m instanceof Skeleton s) {
                        // Attempt to shoot an arrow at the player
                        Arrow arrow = s.attack(camera.getStartCol(), camera.getStartRow(), player.getX(), player.getY());
                        if (arrow != null) {
                            // If an arrow was shot, set the skeleton as its owner and add it to the active arrows list
                            arrow.setSkeleton(s);
                            arrow.setDynamicTileSize(camera.getDynamicTileSize());
                            activeArrows.add(arrow);
                            arrow.shoot(gameObjectsPane, player);
                        }
                    }
                }

                // Remove any arrows that are no longer in the scene (e.g., after hitting the player or reaching their target)
                activeArrows.removeIf(arrow -> !gameObjectsPane.getChildren().contains(arrow.getView()));

                // Update the last frame timestamp
                lastTime = now;
            }
        };
        // Start the game loop
        monsterMovementTimer.start();
    }

    /**
     * Displays a visual indicator of the player's attack range (1 tile in all directions).
     * The range is shown as semi-transparent red squares for 0.5 seconds.
     *
     * @param range The attack range (in tiles, currently hardcoded to 1).
     * @param cameraStartCol The starting column of the camera's viewport.
     * @param cameraStartRow The starting row of the camera's viewport.
     */
    private void showAttackRange(int range, int cameraStartCol, int cameraStartRow) {
        List<Rectangle> rangeIndicators = new ArrayList<>();

        double dynamicTileSize = camera.getDynamicTileSize();
        double offset = dynamicTileSize * 0.2;

        double centerOffsetX = viewportCanvas.getWidth() / 2 - dynamicTileSize / 2;
        double centerOffsetY = viewportCanvas.getHeight() / 2 - dynamicTileSize / 2;

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                int attackX = player.getX() + dx;
                int attackY = player.getY() + dy;

                if (attackX >= 0 && attackX < baseMap[0].length && attackY >= 0 && attackY < baseMap.length) {
                    double pixelX = centerOffsetX + dx * dynamicTileSize + offset;
                    double pixelY = centerOffsetY + dy * dynamicTileSize + offset;

                    Rectangle rangeIndicator = new Rectangle(dynamicTileSize * 0.6, dynamicTileSize * 0.6);
                    rangeIndicator.setTranslateX(pixelX);
                    rangeIndicator.setTranslateY(pixelY);
                    rangeIndicator.setFill(Color.RED);
                    rangeIndicator.setOpacity(0.3);

                    rangeIndicators.add(rangeIndicator);
                    gameObjectsPane.getChildren().add(rangeIndicator);
                }
            }
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(event -> {
            gameObjectsPane.getChildren().removeAll(rangeIndicators);
        });
        pause.play();
    }





    /**
     * Handles monster movement and attacks as part of the game loop.
     * This method is called every 1 second (MONSTER_UPDATE_INTERVAL_NANOS).
     * Monsters move according to their movement strategy, perform melee attacks if applicable, and may die if they step on a river.
     *
     * @param currentTime The current timestamp (in nanoseconds), used for melee attack cooldowns.
     */
    private void handleMonsterMovement(long currentTime) {
        int rows = fieldMap.length; // Number of rows in the map
        int cols = fieldMap[0].length; // Number of columns in the map

        // Create a fresh copy of the baseMap to reset dynamic elements
        fieldMap = copyMap(baseMap);
        List<Monster> toRemove = new ArrayList<>(); // List of monsters to remove (e.g., if they die)

        // Iterate through all monsters to update their positions and behavior
        for (Monster monster : monsterList) {
            int dx = Math.abs(player.getX() - monster.getX());
            int dy = Math.abs(player.getY() - monster.getY());

            if (dx <= monster.getAttackRange() && dy <= monster.getAttackRange()) {
                if (!(monster.getMovementStrategy() instanceof FollowingMovement)) {
                    monster.setMovementStrategy(new FollowingMovement());
                    if (debug_mod_game) {
                        System.out.println(monster.getNameEng() + " switches to FollowingMovement!");
                    }
                }
            }

            // If the monster is not a skeleton, attempt a melee attack on the player
            if (!(monster instanceof Skeleton)) {
                monster.meleeAttack(player, gameObjectsPane, camera.getStartCol(), camera.getStartRow(), currentTime);
            }

            // Check if the monster is currently playing a hit effect or melee attack animation
            // If so, it cannot move
            if (monster.isHitEffectPlaying() || monster.isMeleeAttacking()) {
                if (debug_mod_game)
                    System.out.println("Monster at (" + monster.getX() + ", " + monster.getY() + ") cannot move due to " + (monster.isHitEffectPlaying() ? "hit effect" : "melee attack"));
                continue;
            }

            if (dx <= monster.getAttackRange() && dy <= monster.getAttackRange()) {
                if (debug_mod_game) {
                    System.out.println("Monster at (" + monster.getX() + ", " + monster.getY() + ") will not move - player is within attack range (" + dx + ", " + dy + ")");
                }
                continue; // Гравець у зоні досяжності, монстр не рухається
            }

            // Store the monster's current position
            int oldX = monster.getX();
            int oldY = monster.getY();
            int newX = oldX;
            int newY = oldY;

            // Move the monster according to its movement strategy (e.g., random movement for goblins)
            monster.move();

            newX = monster.getX(); // Get the new position after moving
            newY = monster.getY();

            // Check if the new position is outside the map bounds
            // If so, revert the monster to its old position
            if (newX < 0 || newX >= cols || newY < 0 || newY >= rows) {
                monster.setPosition(oldX, oldY);
                continue;
            }

            // Check if the new position is occupied by the player
            // If so, revert the monster to its old position (monsters cannot move onto the player's tile)
            if (newX == player.getX() && newY == player.getY()) {
                if (debug_mod_game) {
                    System.out.println("Monster at (" + oldX + ", " + oldY + ") cannot move to (" + newX + ", " + newY + ") - position occupied by player");
                }
                monster.setPosition(oldX, oldY);
                continue;
            }

            // Check if the new position is occupied by another monster
            boolean isOccupiedByAnotherMonster = false;
            for (Monster otherMonster : monsterList) {
                if (otherMonster != monster && otherMonster.getX() == newX && otherMonster.getY() == newY) {
                    isOccupiedByAnotherMonster = true;
                    break;
                }
            }

            // If the new position has another monster, revert the monster to its old position
            if (isOccupiedByAnotherMonster) {
                if (debug_mod_game) {
                    System.out.println("Monster at (" + oldX + ", " + oldY + ") cannot move to (" + newX + ", " + newY + ") - position occupied by another monster");
                }
                monster.setPosition(oldX, oldY);
                continue;
            }

            // Get the tile type at the new position
            int tileValue = fieldMap[newY][newX];
            if (tileValue < 0) {
                tileValue = baseMap[newY][newX]; // If a monster is present, get the underlying base map value
            }
            TypeOfObjects tileType = TypeOfObjects.fromValue(tileValue);

            // Check if the tile is walkable (FLOOR, HATCH, RIVER, or EMPTY)
            if (tileType == TypeOfObjects.FLOOR || tileType == TypeOfObjects.FINISH_POINT || tileType == TypeOfObjects.RIVER || tileType == TypeOfObjects.EMPTY) {
                // If the tile is a river, the monster dies
                if (tileType == TypeOfObjects.RIVER) {
                    if (debug_mod_game)
                        System.out.println("Monster at (" + oldX + ", " + oldY + ") stepped on RIVER at (" + newX + ", " + newY + ") - Monster dies");
                    toRemove.add(monster); // Mark the monster for removal
                }
            } else {
                // If the tile is not walkable (e.g., MOUNTAIN, TREE), revert the monster to its old position
                if (debug_mod_game)
                    System.out.println("Monster cannot move to (" + newX + ", " + newY + ") - tile type: " + tileType);
                monster.setPosition(oldX, oldY);
            }
        }

        // Remove any monsters marked for removal (e.g., those that stepped on a river)
        removeMonsters(toRemove);

        // Update the fieldMap with the remaining monsters
        updateMapWithMonsters();

        // Redraw the viewport to reflect the updated game state
        updateCameraViewport();
    }

    /**
     * Shows the center menu popup at the center of the scene.
     * The popup displays a title and two buttons: "Option 1" and "Go Back".
     * "Go Back" loads the main menu scene.
     */
    private void showCenterMenu() {
        // Create the popup if it doesn't already exist
        if (centerMenuPopup == null) {
            centerMenuPopup = new Popup();
            centerMenuPopup.setAutoHide(true); // The popup hides when clicked outside of it

            // Create a VBox to hold the popup's content
            VBox menuBox = new VBox(20); // 20 pixels spacing between children
            menuBox.setAlignment(Pos.CENTER);
            menuBox.setStyle("-fx-background-color: #C0C0C0; -fx-padding: 20; -fx-background-radius: 20;");
            menuBox.getStyleClass().add("menu-popup");

            // Create a title label for the popup
            Label title = new Label("MENU");
            title.getStyleClass().add("menu-title");

            // Create buttons for the popup
            Button option1 = new Button("Option 1");
            Button goBackButton = new Button("Go Back");

            // Define the action for "Option 1" (simply closes the popup)
            option1.setOnAction(e -> centerMenuPopup.hide());

            // Define the action for "Go Back" (loads the main menu scene)
            goBackButton.setOnAction(e -> {
                try {
                    URL fxmlUrl = getClass().getResource("/com/gnome/gnome/pages/main-menu.fxml");
                    Parent mainRoot = FXMLLoader.load(Objects.requireNonNull(fxmlUrl));
                    Stage stage = (Stage) centerMenuButton.getScene().getWindow();
                    stage.getScene().setRoot(mainRoot);
                } catch (IOException ex) {
                    logger.severe("Failed to load main page: " + ex.getMessage());
                    ex.printStackTrace();
                }
                centerMenuPopup.hide();
            });

            // Add the title and buttons to the popup's content
            menuBox.getChildren().addAll(title, option1, goBackButton);
            centerMenuPopup.getContent().add(menuBox);
        }

        // Show the popup at the center of the window
        Scene scene = centerMenuButton.getScene();
        if (scene != null) {
            // Calculate the position to center the popup
            Bounds bounds = scene.getRoot().localToScreen(scene.getRoot().getBoundsInLocal());
            centerMenuPopup.show(scene.getWindow(),
                    bounds.getMinX() + bounds.getWidth() / 2 - 100,
                    bounds.getMinY() + bounds.getHeight() / 2 - 75);
        }
    }

    /**
     * Updates the player's health bar to reflect their current health.
     * If the player's health reaches 0, the game over overlay is shown.
     */
    private void updatePlayerHealthBar() {
        // Run the update on the JavaFX Application Thread (required for UI updates)
        Platform.runLater(() -> {
            // Calculate the fraction of health remaining (currentHealth / maxHealth)
            double fraction = (double) player.getCurrentHealth() / player.getMaxHealth();
            //  Update the health bar to show the new fraction
            healthBar.setHealthFraction(fraction);

            // If the player's health reaches 0, show the game over screen
            if (player.getCurrentHealth() <= 0) {
                showGameOverOverlay();
            }
        });
    }


    /**
     * Displays the "Game Over" overlay when the player's health reaches 0.
     * The overlay shows a "Game Over" message and provides buttons to restart the game or exit.
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