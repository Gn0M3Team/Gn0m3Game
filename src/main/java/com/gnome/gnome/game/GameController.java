package com.gnome.gnome.game;

import com.gnome.gnome.camera.Camera;
import com.gnome.gnome.components.PlayerHealthBar;
import com.gnome.gnome.game.component.Chest;
import com.gnome.gnome.game.component.Coin;
import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.models.Armor;
import com.gnome.gnome.models.Potion;
import com.gnome.gnome.models.Weapon;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.types.Skeleton;
import com.gnome.gnome.monsters.types.missels.Arrow;
import com.gnome.gnome.player.Player;
import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

@Getter
@Setter
public class GameController {

    @FXML private BorderPane rootBorder;
    @FXML private StackPane centerStack;
    @FXML private Button centerMenuButton;
    @FXML private StackPane healthBarContainer;

    private Canvas viewportCanvas;
    private Weapon weapon;
    private Armor armor;
    private Potion potion;
    private int[][] baseMap;
    private int[][] fieldMap;

    private Camera camera;
    private final List<Monster> monsterList = new ArrayList<>();
    private final List<Coin> coinsOnMap = new ArrayList<>();
    private final List<Arrow> activeArrows = new ArrayList<>();
    private final List<Chest> activeChests = new ArrayList<>();
    private final List<com.gnome.gnome.models.Monster> dbMonsters = new ArrayList<>();

    private static final Logger logger = Logger.getLogger(GameController.class.getName());

    private AnimationTimer gameLoop;
    private final long UPDATE_INTERVAL = 16_666_666L;
    private long lastTime = 0;
    private boolean isStop = false;

    private Player player;
    private Pane gameObjectsPane;
    private static GameController instance;
    private boolean debugModGame;

    private final MovementService movementService = new MovementService(this);
    private GameUIManager uiManager;
    private VBox gameOverOverlay;
    private Popup centerMenuPopup;
    private PlayerHealthBar healthBar;

    public static GameController getGameController() {
        return instance == null ? new GameController() : instance;
    }

    public GameController() {}


    public void initializeWithLoadedMap(int[][] mapData, List<com.gnome.gnome.models.Monster> monsterList, Armor armor, Weapon weapon, Potion potion) {
        this.baseMap = mapData;
        this.fieldMap = GameInitializer.copyMap(baseMap);
        this.debugModGame = GameInitializer.loadProperties("app.debug_mod_game");
        this.dbMonsters.addAll(monsterList);
        this.armor = armor;
        this.weapon = weapon;
        this.potion = potion;


        GameInitializer.setupMap(fieldMap, dbMonsters, this.monsterList, this.activeChests, armor, weapon);

        this.player = Player.getInstance();
        this.camera = Camera.getInstance(fieldMap, 0, 0, player, armor, weapon, potion);


        camera.updateCameraCenter();
        instance = this;

        setupUI();
        addGameEntitiesToPane();

        uiManager = new GameUIManager(this);
        healthBar = new PlayerHealthBar(250, 100);
        healthBarContainer.getChildren().add(healthBar);
        updatePlayerHealthBar();

        centerMenuButton.setOnAction(e -> uiManager.showCenterMenuPopup());

        rootBorder.sceneProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) registerKeyHandlers(newS);
        });

        startGameLoop();
        renderGame(false);
    }

    private void setupUI() {
        viewportCanvas = new Canvas();
        BorderPane.setAlignment(centerStack, Pos.CENTER);
        centerStack.prefWidthProperty().bind(rootBorder.widthProperty());
        centerStack.prefHeightProperty().bind(rootBorder.heightProperty());
        centerStack.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        rootBorder.setCenter(centerStack);

        viewportCanvas.widthProperty().bind(centerStack.widthProperty());
        viewportCanvas.heightProperty().bind(centerStack.heightProperty());
        viewportCanvas.setFocusTraversable(true);

        gameObjectsPane = new Pane();
        gameObjectsPane.prefWidthProperty().bind(centerStack.widthProperty());
        gameObjectsPane.prefHeightProperty().bind(centerStack.heightProperty());
        gameObjectsPane.setPickOnBounds(false);

        Pane viewportRoot = new Pane(viewportCanvas, gameObjectsPane);
        centerStack.getChildren().setAll(viewportRoot);
        StackPane.setAlignment(viewportRoot, Pos.CENTER);
    }

    private void loadMainMenu() {
        try {
            URL fxmlUrl = getClass().getResource("/com/gnome/gnome/pages/main-menu.fxml");
            Parent mainRoot = FXMLLoader.load(Objects.requireNonNull(fxmlUrl));
            Stage stage = (Stage) centerMenuButton.getScene().getWindow();
            stage.getScene().setRoot(mainRoot);
        } catch (IOException ex) {
            logger.severe("Failed to load main page: " + ex.getMessage());
        }
    }

    private void addGameEntitiesToPane() {
        camera.updateCameraCenter();
        double tileSize = camera.getDynamicTileSize();

        for (Chest chest : activeChests) {
            chest.updatePositionWithCamera(camera.getStartCol(), camera.getStartRow(), tileSize, tileSize);
            if (!gameObjectsPane.getChildren().contains(chest.getImageView())) {
                gameObjectsPane.getChildren().add(chest.getImageView());
            }
        }

        for (Monster monster : monsterList) {
            ImageView monsterView = monster.getRepresentation();
            monsterView.getProperties().put("gridX", monster.getX());
            monsterView.getProperties().put("gridY", monster.getY());
            monster.updateVisual(camera);
            if (!gameObjectsPane.getChildren().contains(monsterView)) {
                gameObjectsPane.getChildren().add(monsterView);
            }
        }
    }

    void onSceneExit(boolean isRestart) {
        if (gameLoop != null) gameLoop.stop();
        gameLoop = null;

        Camera.resetInstance();
        Player.resetInstance();
        if (!isRestart)
            GameController.instance = null;

        viewportCanvas = null;
        monsterList.clear();
        coinsOnMap.clear();
        activeArrows.clear();
        gameObjectsPane.getChildren().clear();
        isStop = false;
    }

    private void registerKeyHandlers(Scene scene) {
        scene.setOnKeyPressed(movementService::handleKeyPress);
        scene.getRoot().requestFocus();
    }

    public boolean isBlocked(int x, int y) {
        return monsterList.stream().anyMatch(m -> m.getX() == x && m.getY() == y) ||
                activeChests.stream().anyMatch(ch -> ch.getGridX() == x && ch.getGridY() == y);
    }

    void movePlayer(int oldX, int oldY, int newX, int newY, TypeOfObjects tileType) {
        if (newX < oldX) player.moveLeft();
        else if (newX > oldX) player.moveRight();
        else if (newY < oldY) player.moveUp();
        else if (newY > oldY) player.moveDown();

        if (tileType == TypeOfObjects.FINISH_POINT) onHatchStepped();
        else if (tileType == TypeOfObjects.RIVER) onRiverStepped();

        checkCoinPickup();
        renderGame(true);
        monsterList.forEach(m -> m.cancelMeleeAttackIfPlayerOutOfRange(player));
    }

    boolean isNearChest(int x, int y) {
        for (Chest chest : activeChests) {
            int cx = chest.getGridX();
            int cy = chest.getGridY();
            if ((Math.abs(cx - x) == 1 && cy == y) || (Math.abs(cy - y) == 1 && cx == x)) {
                return true;
            }
        }
        return false;
    }

    void openNearbyChest() {
        Iterator<Chest> iter = activeChests.iterator();
        while (iter.hasNext()) {
            Chest chest = iter.next();
            int cx = chest.getGridX();
            int cy = chest.getGridY();

            boolean isAdjacent = (Math.abs(cx - player.getX()) == 1 && cy == player.getY()) ||
                    (Math.abs(cy - player.getY()) == 1 && cx == player.getX());

            if (isAdjacent) {
                chest.animate();
                player.addCoin(Math.round(chest.getValue()));

                PauseTransition delay = new PauseTransition(javafx.util.Duration.seconds(1));
                delay.setOnFinished(event -> {
                    activeChests.remove(chest);
                    renderGame(true);
                });
                delay.play();

                break;
            }
        }
    }


    boolean isNearTable(int x, int y) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (nx >= 0 && ny >= 0 && ny < baseMap.length && nx < baseMap[0].length) {
                if (TypeOfObjects.fromValue(baseMap[ny][nx]) == TypeOfObjects.TABLE) {
                    return true;
                }
            }
        }
        return false;
    }

    void showTablePopup() {
        uiManager.showTablePopup("Crafting Table");
    }

    // TODO: Here we need to implement shop
    private void onHatchStepped() {

    }

    /**
     * Handles the player stepping on a river tile.
     * The player takes damage equal to their current health, effectively killing them.
     */
    private void onRiverStepped() {
        if (debugModGame) System.out.println("Player stepped on river at (" + player.getX() + ", " + player.getY() + ")");
        player.takeDamage(player.getCurrentHealth());
        updatePlayerHealthBar();
    }

    /**
     * Checks whether the player is standing on a coin and collects it if found.
     */
    private void checkCoinPickup() {
        int px = player.getX();
        int py = player.getY();

        List<Coin> collected = coinsOnMap.stream()
                .filter(coin -> coin.getGridX() == px && coin.getGridY() == py)
                .toList();

        collected.forEach(coin -> {
            player.addCoin(coin.getValue());
            gameObjectsPane.getChildren().remove(coin.getImageView());
        });

        coinsOnMap.removeAll(collected);
    }

    private void drawAttackRange(GraphicsContext gc, int range) {
        gc.setFill(Color.color(1, 0, 0, 0.3));
        double tw = camera.getTileWidth(), th = camera.getTileHeight();
        int baseX = player.getX() - camera.getStartCol();
        int baseY = player.getY() - camera.getStartRow();

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                gc.fillRect((baseX + dx) * tw, (baseY + dy) * th, tw, th);
            }
        }
    }




    /**
     * Removes the specified monsters from the game and replaces them with coins.
     * The monsters are removed from the monsterList, and their positions on the fieldMap are updated to FLOOR tiles.
     *
     * @param eliminated A list of monsters to remove (e.g., monsters killed by the player's attack).
     */
    void removeMonsters(List<Monster> eliminated) {
        eliminated.forEach(monster -> {
            int x = monster.getX(), y = monster.getY();
            coinsOnMap.add(new Coin(x, y, monster.getCost()));
            gameObjectsPane.getChildren().remove(monster.getRepresentation());
        });

        monsterList.removeAll(eliminated);
        renderGame(true);
    }


    /**
     * Updates the camera's viewport to render the current state of the game.
     * This includes drawing the map, updating the player's position, repositioning effects, and updating UI elements.
     */
    private void renderGame(boolean updateCamera) {
        GraphicsContext gc = viewportCanvas.getGraphicsContext2D();
        if (updateCamera) camera.updateCameraCenter();

        camera.drawViewport(viewportCanvas, coinsOnMap);
        camera.drawPressEHints(gc, baseMap, player.getX(), player.getY(), activeChests);
        drawAttackRange(gc, 1);

        monsterList.forEach(m -> m.updateVisual(camera));
        activeChests.forEach(c -> c.updatePositionWithCamera(camera.getStartCol(), camera.getStartRow(), camera.getTileWidth(), camera.getTileHeight()));
        activeArrows.forEach(a -> a.updateCameraOffset(camera.getStartCol(), camera.getStartRow()));

        player.setDynamicTileSize(Math.min(camera.getTileWidth(), camera.getTileHeight()));
        player.updatePositionWithCamera(camera.getStartCol(), camera.getStartRow(), camera.getTileWidth(), camera.getTileHeight());

        if (!gameObjectsPane.getChildren().contains(player.getRepresentation()))
            gameObjectsPane.getChildren().add(player.getRepresentation());

        updatePlayerHealthBar();

    }


    public boolean isLineOfSightClear(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;

        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        double stepX = dx / (double) steps;
        double stepY = dy / (double) steps;

        for (int i = 1; i < steps; i++) {
            int ix = (int) Math.round(x1 + i * stepX);
            int iy = (int) Math.round(y1 + i * stepY);

            if (ix < 0 || iy < 0 || iy >= fieldMap.length || ix >= fieldMap[0].length) return true;

            if (!TypeOfObjects.fromValue(baseMap[iy][ix]).isTransparent()) return true;
        }
        return false;
    }


    /**
     * Starts the game loop using an AnimationTimer.
     * The game loop updates the viewport, moves monsters, triggers skeleton attacks, and manages active arrows.
     */
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                if (now - lastTime >= UPDATE_INTERVAL) {
                    double delta = (now - lastTime) / 1_000_000_000.0;
                    updateMonsters(delta);
                    updateProjectiles(delta);
                    renderGame(true);
                    lastTime = now;
                }
            }
        };
        gameLoop.start();
    }

    private void updateMonsters(double delta) {
        for (Monster monster : monsterList) {
            monster.updateLogic(player, delta, fieldMap, baseMap);
            if (monster instanceof Skeleton skeleton) {
                if (skeleton.canShootArrow() && !skeleton.hasActiveArrow() &&
                        !isLineOfSightClear(skeleton.getX(), skeleton.getY(), player.getX(), player.getY())) {
                    Arrow arrow = skeleton.shootArrowTowards(player);
                    if (arrow != null) {
                        arrow.setDynamicTileSize(camera.getDynamicTileSize());
                        arrow.updateCameraOffset(camera.getStartCol(), camera.getStartRow());
                        activeArrows.add(arrow);
                        arrow.shoot(gameObjectsPane, player);
                    }
                }
            } else {
                monster.meleeAttack(player, gameObjectsPane, System.nanoTime());
                monster.cancelMeleeAttackIfPlayerOutOfRange(player);
            }
        }
    }

    private void updateProjectiles(double delta) {
        activeArrows.removeIf(arrow -> {
            arrow.update(delta);
            if (arrow.hasHitTarget()) {
                gameObjectsPane.getChildren().remove(arrow.getView());
                return true;
            }
            return false;
        });
    }


    public void shakeCamera() {
        uiManager.shakeCamera();
    }

    public void updatePlayerHealthBar() {
        uiManager.updateHealthBar(healthBar);
    }

    void restartGame() {
        try {
            onSceneExit(true);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gnome/gnome/pages/game.fxml"));
            Parent newRoot = loader.load();
            GameController ctrl = loader.getController();
            ctrl.initializeWithLoadedMap(baseMap, dbMonsters, armor, weapon, potion);
            Stage stage = (Stage) rootBorder.getScene().getWindow();
            stage.getScene().setRoot(newRoot);
        } catch (IOException ex) {
            logger.severe("Failed to restart game: " + ex.getMessage());
        }
    }



}
