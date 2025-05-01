package com.gnome.gnome.game;

import com.gnome.gnome.camera.Camera;
import com.gnome.gnome.components.PlayerHealthBar;
import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.dao.UserStatisticsDAO;
import com.gnome.gnome.dao.userDAO.UserGameStateDAO;
import com.gnome.gnome.game.component.Chest;
import com.gnome.gnome.game.component.Coin;
import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.models.*;
import com.gnome.gnome.models.Map;
import com.gnome.gnome.models.user.UserGameState;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.types.Skeleton;
import com.gnome.gnome.monsters.types.missels.Arrow;
import com.gnome.gnome.player.Player;
import com.gnome.gnome.shop.controllers.ShopController;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import com.gnome.gnome.userState.UserState;
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
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.URL;
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
    private boolean isGameOver = false;

    private Player player;
    private Pane gameObjectsPane;
    private static GameController instance;
    private boolean debugModGame;

    private final MovementService movementService = new MovementService(this);
    private GameUIManager uiManager;
    private VBox gameOverOverlay;
    private Popup centerMenuPopup;
    private Pane darkOverlay;
    private PlayerHealthBar healthBar;
    private Stage currentPopup;


    private Map selectedMap;


    public static GameController getGameController() {
        return instance == null ? new GameController() : instance;
    }

    public GameController() {}


    public void initializeWithLoadedMap(Map selectedMap,int[][] mapData, List<com.gnome.gnome.models.Monster> monsterList, Armor armor, Weapon weapon, Potion potion) {
        this.baseMap = mapData;
        this.fieldMap = GameInitializer.copyMap(baseMap);
        this.debugModGame = GameInitializer.loadProperties("app.debug_mod_game");
        this.dbMonsters.addAll(monsterList);
        this.armor = armor;
        this.weapon = weapon;
        this.potion = potion;
        this.selectedMap = selectedMap;


        GameInitializer.setupMap(fieldMap, dbMonsters, this.monsterList, this.activeChests, armor, weapon);

        this.player = Player.getInstance();
        this.camera = Camera.getInstance(fieldMap, 0, 0, player, armor, weapon, potion);
        camera.updateCameraCenter();
        instance = this;

        setupUI();
        addGameEntitiesToPane();

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
        centerStack.prefWidthProperty().bind(rootBorder.widthProperty());
        centerStack.prefHeightProperty().bind(rootBorder.heightProperty());
        centerStack.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        rootBorder.setCenter(centerStack);

        viewportCanvas.widthProperty().bind(centerStack.widthProperty());
        viewportCanvas.heightProperty().bind(centerStack.heightProperty());
        viewportCanvas.setFocusTraversable(true);
        viewportCanvas.requestFocus();

        gameObjectsPane = new Pane();
        gameObjectsPane.setPickOnBounds(false);
        gameObjectsPane.prefWidthProperty().bind(centerStack.widthProperty());
        gameObjectsPane.prefHeightProperty().bind(centerStack.heightProperty());

        Pane viewportRoot = new Pane(viewportCanvas, gameObjectsPane);
        centerStack.getChildren().setAll(viewportRoot);
        StackPane.setAlignment(viewportRoot, Pos.CENTER);

        camera.drawViewport(viewportCanvas, coinsOnMap);
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
            System.out.println("Adding monster view: " + monster.getNameEng() +
                    ", visible=" + monster.getRepresentation().isVisible() +
                    ", parent=" + monster.getRepresentation().getParent());
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

    public boolean isBlocked(int x, int y, Monster self) {
        return monsterList.stream().anyMatch(m -> m != self && m.getX() == x && m.getY() == y) ||
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
        renderGame();
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
        for (Chest chest : activeChests) {
            int cx = chest.getGridX();
            int cy = chest.getGridY();

            boolean isAdjacent = (Math.abs(cx - player.getX()) == 1 && cy == player.getY()) ||
                    (Math.abs(cy - player.getY()) == 1 && cx == player.getX());

            if (isAdjacent && !chest.isOpened()) {
                chest.setOpened(true);
                chest.animate();
                player.addCoin(Math.round(chest.getValue()));
                player.addCountOfOpenedChest();
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

    private void onHatchStepped() {
        if (!monsterList.isEmpty()) {
            if (debugModGame) System.out.println("You must kill all monsters before using the hatch!");
            return;
        }

        isGameOver = true;
        if (gameLoop != null) gameLoop.stop();

        onLevelCompleted();
        showShopPopup();
    }

    public void onLevelCompleted() {
        updateMapAfterLevelCompletion(selectedMap);

        updatePlayerAfterLevelCompletion(player);

        updatePlayerStatisticsAfterLevelCompletion(player);

        if (selectedMap.getLevel() == UserState.getInstance().getMapLevel()) {
            updatePlayerLevelAfterStoryLevelCompletion();
        }
    }

    public void updatePlayerStatisticsAfterLevelCompletion(Player player) {
        UserStatisticsDAO userStatisticsDAO = new UserStatisticsDAO();
        UserStatistics userStatistics = userStatisticsDAO.getUserStatisticsByUsername(UserState.getInstance().getUsername());
        if (userStatistics != null) {
            userStatistics.setTotalMapsPlayed(userStatistics.getTotalMapsPlayed() + 1);
            userStatistics.setTotalWins(userStatistics.getTotalWins() + 1);
            userStatistics.setTotalMonstersKilled(userStatistics.getTotalMonstersKilled() + player.getCountOfKilledMonsters());
            userStatistics.setTotalChestsOpened(userStatistics.getTotalChestsOpened() + player.getCountOfOpenedChest());
            userStatisticsDAO.updateUserStatistics(userStatistics);
        }
    }

    public void updatePlayerAfterLevelCompletion(Player player) {
        UserGameStateDAO userGameStateDAO = new UserGameStateDAO();
        UserGameState userGameState = userGameStateDAO.getUserGameStateByUsername(UserState.getInstance().getUsername());
        if (userGameState != null) {
            userGameState.setScore(userGameState.getScore() + player.getScore());
            userGameState.setBalance((float) (userGameState.getBalance() + player.getPlayerCoins()));
            userGameStateDAO.updateUserGameState(userGameState);
        }
    }

    public void updateMapAfterLevelCompletion(Map map) {
        MapDAO mapDAO = new MapDAO();
        map.setTimesPlayed(map.getTimesPlayed() + 1);
        map.setTimesCompleted(map.getTimesCompleted() + 1);
        mapDAO.updateMap(map);
    }



    public void updatePlayerLevelAfterStoryLevelCompletion() {
        UserGameStateDAO userGameStateDAO = new UserGameStateDAO();
        UserGameState userGameState = userGameStateDAO.getUserGameStateByUsername(UserState.getInstance().getUsername());
        if (userGameState != null) {
            userGameState.setMapLevel(userGameState.getMapLevel() + 1); // Increment the player's level
            userGameStateDAO.updateUserGameState(userGameState);
        }
    }

    private void showDarkOverlay() {
        if (darkOverlay == null) {
            darkOverlay = new Pane();
            darkOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
            darkOverlay.prefWidthProperty().bind(centerStack.widthProperty());
            darkOverlay.prefHeightProperty().bind(centerStack.heightProperty());
        }

        if (!centerStack.getChildren().contains(darkOverlay)) {
            centerStack.getChildren().add(darkOverlay);
        }
    }

    private void hideDarkOverlay() {
        centerStack.getChildren().remove(darkOverlay);
    }

    private void showShopPopup() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gnome/gnome/pages/shop.fxml"));
            Parent shopRoot = loader.load();

            Scene shopScene = new Scene(shopRoot);
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initOwner(centerMenuButton.getScene().getWindow());
            popup.setTitle("Shop");

            popup.setScene(shopScene);
            popup.setResizable(false);

            showDarkOverlay();
            popup.setOnHidden(e -> {
                hideDarkOverlay();
                currentPopup = null;
            });

            ShopController controller = loader.getController();
            controller.setGameController(this);

            currentPopup = popup;
            popup.showAndWait();

        } catch (IOException e) {
            logger.severe("Failed to load shop popup: " + e.getMessage());
        }
    }

    public void closeShopAndGoToMainMenu() {
        if (currentPopup != null) currentPopup.close();
        onSceneExit(false);
        new SwitchPage().goMainMenu(rootBorder);
    }

    public void closeShopAndStartNewGame() {
        if (currentPopup != null) currentPopup.close();
        onSceneExit(false);
        new SwitchPage().goNewGame(rootBorder);
    }

    /**
     * Handles the player stepping on a river tile.
     * The player takes damage equal to their current health, effectively killing them.
     */
    private void onRiverStepped() {
        if (debugModGame) System.out.println("Player stepped on river at (" + player.getX() + ", " + player.getY() + ")");
        double damage = max(player.getCurrentHealth() * 0.1, 1);
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
            player.addScore(monster.getValue());
            player.addCountOfKilledMonsters();
            gameObjectsPane.getChildren().remove(monster.getRepresentation());
        });

        monsterList.removeAll(eliminated);
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
        camera.drawPressEHints(gc, baseMap, player.getX(), player.getY(), activeChests);
        drawAttackRange(gc, 1);

        monsterList.forEach(m -> {
            m.updateVisual(camera);
        });
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
                    updateProjectiles(delta);
                    renderGame();
                    lastTime = now;
                }
            }
        };
        gameLoop.start();
    }

    private void updateMonsters(double delta) {
        if (isGameOver || isStop) return;

        List<Monster> toRemove = new ArrayList<>();

        for (Monster monster : monsterList) {
            if (monster.getHealth() <= 0) {
                toRemove.add(monster);
                continue;
            }
            if (monster.getRepresentation().getParent() == null) {
                gameObjectsPane.getChildren().add(monster.getRepresentation());
            }
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
            }
        }

        if (!toRemove.isEmpty()) {
            removeMonsters(toRemove);
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
