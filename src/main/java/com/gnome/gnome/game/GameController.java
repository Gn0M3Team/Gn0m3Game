package com.gnome.gnome.game;

import com.gnome.gnome.MainApplication;
import com.gnome.gnome.game.camera.Camera;
import com.gnome.gnome.components.PlayerHealthBar;
import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.game.component.Chest;
import com.gnome.gnome.game.component.Coin;
import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.game.component.CoinUIRenderer;
import com.gnome.gnome.game.component.ItemUIRenderer;
import com.gnome.gnome.game.monsters.GameMonster;
import com.gnome.gnome.models.*;
import com.gnome.gnome.models.Map;
import com.gnome.gnome.game.player.Player;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import com.gnome.gnome.userState.UserState;
import com.gnome.gnome.utils.CustomPopupUtil;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.Math.max;

@Getter
@Setter
public class GameController {

    @FXML private BorderPane rootBorder;
    @FXML private StackPane centerStack;
    @FXML private Button centerMenuButton;
    @FXML private StackPane healthBarContainer;
//    @FXML private VBox rightUIBox;

    private Canvas viewportCanvas;
    private Weapon weapon;
    private Armor armor;
    private Potion potion;
    private int[][] baseMap;
    private int[][] fieldMap;

    private Camera camera;
    private final List<GameMonster> gameMonsterList = new ArrayList<>();
    private final List<Coin> coinsOnMap = new ArrayList<>();
    private final List<Chest> activeChests = new ArrayList<>();
    private final List<com.gnome.gnome.models.Monster> dbMonsters = new ArrayList<>();

    private static final Logger logger = Logger.getLogger(GameController.class.getName());

    private AnimationTimer gameLoop;
    private final long UPDATE_INTERVAL = 16_666_666L;
    private long lastTime = 0;
    private boolean isStop = false;
    private boolean isGameOver = false;

    private Player player;
    private Pane gameObjectsPane;
    private static GameController instance;
    private boolean debugModGame;

    private final PlayerGameService playerGameService = new PlayerGameService(this);
    private GameUIManager uiManager;
    private VBox gameOverOverlay;
    private Popup centerMenuPopup;
    private Pane darkOverlay;
    private PlayerHealthBar healthBar;


    private Map selectedMap;
    private boolean isStoryMode;
    private ItemUIRenderer itemUIRenderer;
    private CoinUIRenderer coinUIRenderer;
    private Pane uiOverlayPane;
    private VBox bottomUIBox;

    private ResourceBundle bundle;

    private boolean isInfoForChestShown = false;
    private boolean isInfoForTableShown = false;

    public static GameController getGameController() {
        return instance == null ? new GameController() : instance;
    }

    public GameController() {}


    public void initializeWithLoadedMap(Map selectedMap,int[][] mapData, List<Monster> monsterList, Armor armor, Weapon weapon, Potion potion) {
        this.baseMap = mapData;
        this.fieldMap = GameInitializer.copyMap(baseMap);
        this.debugModGame = GameInitializer.loadProperties("app.debug_mod_game");
        this.dbMonsters.addAll(monsterList);
        this.armor = armor;
        this.weapon = weapon;
        this.potion = potion;
        this.selectedMap = selectedMap;
        isStoryMode = selectedMap.getLevel() != 0;

        this.bundle = MainApplication.getLangBundle();

        GameInitializer.setupMap(fieldMap, dbMonsters, this.gameMonsterList, this.activeChests, armor, weapon);

        this.player = Player.getInstance();
        this.camera = Camera.getInstance(fieldMap, 0, 0, player, armor, weapon, potion);
        camera.updateCameraCenter();
        instance = this;

        setupUI();
        addGameEntitiesToPane();
        updateCenterStackSize();

        uiManager = new GameUIManager(this);
        healthBar = new PlayerHealthBar(250, 50);
        healthBarContainer.getChildren().add(healthBar);
        updatePlayerHealthBar();

        centerMenuButton.setOnAction(e -> uiManager.showCenterMenuPopup());

        rootBorder.sceneProperty().addListener((obs, oldS, newS) -> {
            if (newS != null) registerKeyHandlers(newS);
        });

        startGameLoop();
        renderGame();
    }

    private void setupUI() {
        viewportCanvas = new Canvas();

        BorderPane.setAlignment(centerStack, Pos.CENTER);
        rootBorder.setCenter(centerStack);

        rootBorder.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((o, oldW, newW) -> updateCenterStackSize());
                newScene.heightProperty().addListener((o, oldH, newH) -> updateCenterStackSize());
                updateCenterStackSize();
            }
        });

        gameObjectsPane = new Pane();
        uiOverlayPane = new Pane();
        VBox bottomUIBox = new VBox();

        gameObjectsPane.setPickOnBounds(false);
        uiOverlayPane.setPickOnBounds(false);

        bottomUIBox.setAlignment(Pos.BOTTOM_CENTER);
        bottomUIBox.setPrefHeight(150);
        bottomUIBox.setMouseTransparent(true);
        this.bottomUIBox = bottomUIBox;

        uiOverlayPane.getChildren().add(bottomUIBox);

        VBox itemBoxContent = new VBox(20);
        itemBoxContent.setAlignment(Pos.CENTER);
        itemBoxContent.setPickOnBounds(false);
        itemBoxContent.setMouseTransparent(true);

        itemUIRenderer = new ItemUIRenderer(itemBoxContent);
        itemUIRenderer.render();

        coinUIRenderer = new CoinUIRenderer(itemBoxContent, player);
        coinUIRenderer.render();

        StackPane itemBox = new StackPane(itemBoxContent);
        itemBox.setAlignment(Pos.CENTER);
        itemBox.setPrefWidth(200);
        itemBox.setStyle("-fx-padding: 0 20 0 0;");

        StackPane viewportRoot = new StackPane(viewportCanvas, gameObjectsPane, uiOverlayPane);
        viewportRoot.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        HBox.setHgrow(viewportRoot, Priority.ALWAYS);

        HBox gameAndUIBox = new HBox(viewportRoot, itemBox);
        centerStack.getChildren().setAll(gameAndUIBox);

        camera.drawViewport(viewportCanvas, coinsOnMap);
        updateCenterStackSize();
    }

    private void updateCenterStackSize() {
        if (rootBorder.getScene() == null) return;

        double fullWidth = rootBorder.getScene().getWidth();
        double availableHeight = rootBorder.getScene().getHeight() - 100;

        double itemBoxWidth = 160;
        double availableWidth = fullWidth - itemBoxWidth - 100;

        double size = Math.min(availableWidth, availableHeight);

        centerStack.setPrefWidth(size + itemBoxWidth);
        centerStack.setPrefHeight(size);

        viewportCanvas.setWidth(size);
        viewportCanvas.setHeight(size);

        if (gameObjectsPane != null) {
            gameObjectsPane.setPrefSize(size, size);
            gameObjectsPane.setMinSize(size, size);
            gameObjectsPane.setMaxSize(size, size);
        }

        if (uiOverlayPane != null) {
            uiOverlayPane.setPrefSize(size, size);
            uiOverlayPane.setMinSize(size, size);
            uiOverlayPane.setMaxSize(size, size);
        }

        if (bottomUIBox != null) {
            bottomUIBox.setPrefSize(size, 150);
        }

        renderGame();
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

        for (GameMonster gameMonster : gameMonsterList) {
            ImageView monsterView = gameMonster.getRepresentation();
            monsterView.getProperties().put("gridX", gameMonster.getX());
            monsterView.getProperties().put("gridY", gameMonster.getY());
            gameMonster.updateVisual(camera);
            System.out.println("Adding monster view: " + gameMonster.getNameEng() +
                    ", visible=" + gameMonster.getRepresentation().isVisible() +
                    ", parent=" + gameMonster.getRepresentation().getParent());
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
        gameMonsterList.clear();
        coinsOnMap.clear();
        gameObjectsPane.getChildren().clear();
        isStop = false;
    }

    private void registerKeyHandlers(Scene scene) {
        scene.setOnKeyPressed(playerGameService::handleKeyPress);
        scene.getRoot().requestFocus();
    }

    public boolean isBlocked(int x, int y, GameMonster self) {
        return gameMonsterList.stream().anyMatch(m -> m != self && m.getX() == x && m.getY() == y) ||
                activeChests.stream().anyMatch(ch -> ch.getGridX() == x && ch.getGridY() == y);
    }


    void movePlayer(int oldX, int oldY, int newX, int newY, TypeOfObjects tileType) {
        if (newX < oldX) player.moveLeft();
        else if (newX > oldX) player.moveRight();
        else if (newY < oldY) player.moveUp();
        else if (newY > oldY) player.moveDown();

        if (tileType == TypeOfObjects.FINISH_POINT) onHatchStepped();
        else if (tileType == TypeOfObjects.RIVER) onRiverStepped();

        renderGame();
    }

    boolean isNearChest(int x, int y) {
        for (Chest chest : activeChests) {
            int cx = chest.getGridX();
            int cy = chest.getGridY();
            if (((Math.abs(cx - x) == 1 && cy == y) || (Math.abs(cy - y) == 1 && cx == x)) && !chest.isOpened()) {
                return true;
            }
        }
        return false;
    }

    void openNearbyChest() {
        for (Chest chest : activeChests) {
            int cx = chest.getGridX();
            int cy = chest.getGridY();

            boolean isAdjacent = (Math.abs(cx - player.getX()) == 1 && cy == player.getY()) ||
                    (Math.abs(cy - player.getY()) == 1 && cx == player.getX());

            if (isAdjacent && !chest.isOpened()) {
                chest.setOpened(true);
                chest.animate();
                player.addCoin(Math.round(chest.getValue()));
                coinUIRenderer.update();
                player.addCountOfOpenedChest();
                player.addScore(50);
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
        uiManager.showTablePopup(bundle.getString("crafting.table"));
    }

    private void onHatchStepped() {
        if (!gameMonsterList.isEmpty()) {
            if (rootBorder.getScene() != null && rootBorder.getScene().getWindow() != null) {
                Stage stage = (Stage) rootBorder.getScene().getWindow();
                if (debugModGame) System.out.println("You must kill all monsters before using the hatch!");
                CustomPopupUtil.showWarning(stage, bundle.getString("warning.monsters.before.hatch"));
            }
            return;
        }

        isGameOver = true;
        if (gameLoop != null) gameLoop.stop();

        onLevelCompleted();

        isStop = true;
        uiManager.showStatisticsPopup(isStoryMode,() -> uiManager.showShopPopup(isStoryMode));
    }

    public void onLevelCompleted() {
        updateMapAfterLevelCompletion(selectedMap);
        updatePlayerAfterLevelCompletion(player);
        updatePlayerStatisticsAfterLevelCompletion(player);

        if (isStoryMode && selectedMap.getLevel() == UserState.getInstance().getMapLevel()) {
            updatePlayerLevelAfterStoryLevelCompletion();
        }
    }

    public void updatePlayerStatisticsAfterLevelCompletion(Player player) {
        UserState userState = UserState.getInstance();
        if (userState != null) {
            userState.setUpdateStats(player.getCountOfKilledMonsters(), true, player.getCountOfOpenedChest());
        }
    }

    public void updatePlayerAfterLevelCompletion(Player player) {
        UserState userState = UserState.getInstance();
        if (userState != null) {
            userState.setUpdatePlayerState(player.getScore(), player.getPlayerCoins());

        }
    }

    public void updateMapAfterLevelCompletion(Map map) {
        MapDAO mapDAO = new MapDAO();
        map.setTimesPlayed(map.getTimesPlayed() + 1);
        map.setTimesCompleted(map.getTimesCompleted() + 1);
        mapDAO.updateMap(map);
    }



    public void updatePlayerLevelAfterStoryLevelCompletion() {
        UserState userState = UserState.getInstance();

        if (userState != null) {
            userState.setMapLevel(selectedMap.getLevel() + 1);
        }
    }

    public void closeShopAndStartNewGame() {
        if (uiManager.getCurrentPopup() != null) uiManager.getCurrentPopup().close();
        onSceneExit(false);
        new SwitchPage().goNewGame(rootBorder);
    }

    /**
     * Handles the player stepping on a river tile.
     * The player takes damage equal to their current health, effectively killing them.
     */
    private void onRiverStepped() {
        if (debugModGame) System.out.println("Player stepped on river at (" + player.getX() + ", " + player.getY() + ")");
        double damage = player.getMaxHealth() * 0.1;
        player.takeDamage(damage);
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
        coinUIRenderer.update();
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
    void removeMonsters(List<GameMonster> eliminated) {
        eliminated.forEach(monster -> {
            int x = monster.getX(), y = monster.getY();
            coinsOnMap.add(new Coin(x, y, monster.getCost()));
            player.addScore(monster.getScore());
            player.addCountOfKilledMonsters();
            coinUIRenderer.update();
            gameObjectsPane.getChildren().remove(monster.getRepresentation());
        });

        gameMonsterList.removeAll(eliminated);
        renderGame();
    }


    /**
     * Updates the camera's viewport to render the current state of the game.
     * This includes drawing the map, updating the player's position, repositioning effects, and updating UI elements.
     */
    private void renderGame() {
        GraphicsContext gc = viewportCanvas.getGraphicsContext2D();
        camera.updateCameraCenter();

        camera.drawViewport(viewportCanvas, coinsOnMap);

        if (rootBorder.getScene() != null && rootBorder.getScene().getWindow() != null) {
            Stage stage = (Stage) rootBorder.getScene().getWindow();
            if (this.isNearTable(player.getX(), player.getY()) && !isInfoForTableShown) {
                isInfoForTableShown = true;
                CustomPopupUtil.showInfo(stage, bundle.getString("interaction.table"));
            }
            if (this.isNearChest(player.getX(), player.getY()) && !isInfoForChestShown) {
                isInfoForChestShown = true;
                CustomPopupUtil.showInfo(stage, bundle.getString("interaction.chest"));
            }
        }
        drawAttackRange(gc, 1);

        gameMonsterList.forEach(monster -> {
            monster.updateVisual(camera);
            ImageView view = monster.getRepresentation();

            int gridX = monster.getX();
            int gridY = monster.getY();

            if (camera.isInView(gridX, gridY)) {
                if (!gameObjectsPane.getChildren().contains(view)) {
                    gameObjectsPane.getChildren().add(view);
                }
            } else {
                gameObjectsPane.getChildren().remove(view);
            }
        });
        activeChests.forEach(c -> {
            int x = c.getGridX(), y = c.getGridY();
            c.updatePositionWithCamera(camera.getStartCol(), camera.getStartRow(), camera.getTileWidth(), camera.getTileHeight());

            if (camera.isInView(x, y)) {
                if (!gameObjectsPane.getChildren().contains(c.getImageView())) {
                    gameObjectsPane.getChildren().add(c.getImageView());
                }
            } else {
                gameObjectsPane.getChildren().remove(c.getImageView());
            }
        });
        player.setDynamicTileSize(Math.min(camera.getTileWidth(), camera.getTileHeight()));
        player.updatePositionWithCamera(camera.getStartCol(), camera.getStartRow(), camera.getTileWidth(), camera.getTileHeight(), this::checkCoinPickup);

        if (!gameObjectsPane.getChildren().contains(player.getRepresentation()))
            gameObjectsPane.getChildren().add(player.getRepresentation());

        updatePlayerHealthBar();
        coinUIRenderer.update();
    }


    public boolean isLineOfSightClear(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;

        int steps = max(Math.abs(dx), Math.abs(dy));
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
        if (isGameOver) return;
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
                    renderGame();
                    lastTime = now;
                }
            }
        };
        gameLoop.start();
    }

    private void updateMonsters(double delta) {
        if (isGameOver || isStop) return;

        List<GameMonster> toRemove = new ArrayList<>();

        for (GameMonster gameMonster : gameMonsterList) {
            if (gameMonster.getHealth() <= 0) {
                toRemove.add(gameMonster);
                continue;
            }
            if (gameMonster.getRepresentation().getParent() == null) {
                gameObjectsPane.getChildren().add(gameMonster.getRepresentation());
            }
            gameMonster.updateLogic(player, delta, fieldMap, baseMap);
            gameMonster.meleeAttack(player, gameObjectsPane, System.nanoTime());
        }

        if (!toRemove.isEmpty()) {
            removeMonsters(toRemove);
        }
    }

    public void shakeCamera() {
        if (isGameOver) return;
        uiManager.shakeCamera();
    }

    public void updatePlayerHealthBar() {
        uiManager.updateHealthBar(healthBar);
    }

    void restartGame() {
        try {
            onSceneExit(true);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gnome/gnome/pages/game.fxml"));

            if (MainApplication.getLang() == 'S'){
                loader.setResources(ResourceBundle.getBundle("slovak"));
            }
            else{
                loader.setResources(ResourceBundle.getBundle("english"));
            }

            Parent newRoot = loader.load();
            GameController ctrl = loader.getController();
            ctrl.initializeWithLoadedMap(selectedMap,baseMap, dbMonsters, armor, weapon, potion);
            Stage stage = (Stage) rootBorder.getScene().getWindow();
            stage.getScene().setRoot(newRoot);
        } catch (IOException ex) {
            logger.severe("Failed to restart game: " + ex.getMessage());
        }
    }



}
