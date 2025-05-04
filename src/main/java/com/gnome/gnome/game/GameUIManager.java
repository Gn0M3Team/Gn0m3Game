package com.gnome.gnome.game;

import com.gnome.gnome.MainApplication;
import com.gnome.gnome.components.PlayerHealthBar;
import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.dao.UserStatisticsDAO;
import com.gnome.gnome.dao.userDAO.UserGameStateDAO;
import com.gnome.gnome.models.Map;
import com.gnome.gnome.models.UserStatistics;
import com.gnome.gnome.models.user.UserGameState;
import com.gnome.gnome.player.Player;
import com.gnome.gnome.shop.controllers.ShopController;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import com.gnome.gnome.userState.UserState;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class GameUIManager {
    private final GameController controller;
    private final Logger logger = Logger.getLogger(GameUIManager.class.getName());

    private PageSwitcherInterface pageSwitch;

    @Getter
    private Stage currentPopup;
    private Popup infoPopup; // Track the info popup specifically
    private ResourceBundle bundle;


    public GameUIManager(GameController controller) {
        if (MainApplication.lang == 'S'){
            this.bundle = ResourceBundle.getBundle("slovak");
        }
        else {
            this.bundle = ResourceBundle.getBundle("english");
        }

        this.controller = controller;
        pageSwitch = new SwitchPage();
    }

    public void updateHealthBar(PlayerHealthBar healthBar) {
        Platform.runLater(() -> {
            healthBar.setHealthFraction((double) controller.getPlayer().getCurrentHealth() / controller.getPlayer().getMaxHealth());
            if (controller.getPlayer().getCurrentHealth() <= 0) showGameOverOverlay();
        });
    }

    public void showGameOverOverlay() {
        if (controller.getGameOverOverlay() != null || controller.isGameOver()) return;

        controller.getGameLoop().stop();
        controller.setStop(true);
        controller.setGameOver(true);

        Player player = controller.getPlayer();

        updateMapAfterDeath(controller.getSelectedMap());
        updatePlayerAfterDeath(player);
        updatePlayerStatisticsAfterDeath(player);

        VBox overlay = new VBox(20);
        overlay.setAlignment(Pos.CENTER);
        overlay.getStyleClass().add("game-over-overlay");
        overlay.prefWidthProperty().bind(controller.getCenterStack().widthProperty());
        overlay.prefHeightProperty().bind(controller.getCenterStack().heightProperty());

        Label gameOverLabel = new Label(bundle.getString("game.over"));
        gameOverLabel.getStyleClass().add("game-over-label");

        Label coinsLabel = new Label(String.format(bundle.getString("stats.coins"), (int) player.getPlayerCoins()));
        Label scoreLabel = new Label( String.format(bundle.getString("stats.score"), player.getScore()));
        Label chestsLabel = new Label(String.format(bundle.getString("stats.chests"), player.getCountOfOpenedChest()));
        Label killedLabel = new Label(String.format(bundle.getString("stats.monsters"), player.getCountOfKilledMonsters()));

        for (Label statLabel : List.of(coinsLabel, scoreLabel, chestsLabel, killedLabel)) {
            statLabel.getStyleClass().add("stat-label");
        }

        Button restartButton = new Button(bundle.getString("button.restart"));
        restartButton.getStyleClass().add("game-button");
        restartButton.setOnAction(e -> controller.restartGame());

        Button exitButton = new Button(bundle.getString("exit.button"));
        exitButton.getStyleClass().add("game-button");
        exitButton.setOnAction(e -> {
            pageSwitch.goMainMenu(controller.getRootBorder());
        });

        overlay.getChildren().addAll(
                gameOverLabel,
                coinsLabel,
                scoreLabel,
                chestsLabel,
                killedLabel,
                restartButton,
                exitButton
        );

        controller.getCenterStack().getChildren().add(overlay);
        controller.setGameOverOverlay(overlay);
    }

    public void showCenterMenuPopup() {
        if (controller.getCenterStack().getChildren().stream().anyMatch(n -> n.getStyleClass().contains("menu-popup")))
            return;

        controller.getGameLoop().stop();
        controller.setStop(true);

        Pane darkOverlay = new Pane();
        darkOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        darkOverlay.prefWidthProperty().bind(controller.getCenterStack().widthProperty());
        darkOverlay.prefHeightProperty().bind(controller.getCenterStack().heightProperty());

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/css/game-menu.css")).toExternalForm());
        menuBox.getStyleClass().add("menu-popup");

        Label title = new Label("MENU");
        title.getStyleClass().add("menu-title");

        Button resumeButton = new Button(bundle.getString("menu.button.resume"));
        resumeButton.getStyleClass().add("menu-button");
        resumeButton.setOnAction(e -> {
            controller.getCenterStack().getChildren().removeAll(menuBox, darkOverlay);
            controller.getGameLoop().start();
            controller.setStop(false);

            Scene scene = controller.getCenterMenuButton().getScene();
            if (scene != null) {
                scene.setOnKeyPressed(controller.getPlayerGameService()::handleKeyPress);
                scene.getRoot().requestFocus();
            }

            controller.setCenterMenuPopup(null);
        });

        Button settingsButton = new Button(bundle.getString("button.settings"));
        settingsButton.getStyleClass().add("menu-button");
        settingsButton.setOnAction(e -> {
            controller.getCenterStack().getChildren().removeAll(menuBox, darkOverlay);
            controller.onSceneExit(false);
            pageSwitch.goSetting(controller.getRootBorder());
        });

        Button goBackButton = new Button(bundle.getString("newgame.button.back"));
        goBackButton.getStyleClass().add("menu-button");
        goBackButton.setOnAction(e -> {
            controller.getCenterStack().getChildren().removeAll(menuBox, darkOverlay);
            controller.onSceneExit(false);
            pageSwitch.goMainMenu(controller.getRootBorder());
        });

        menuBox.getChildren().addAll(title, resumeButton, settingsButton, goBackButton);

        controller.getCenterStack().getChildren().addAll(darkOverlay, menuBox);
        StackPane.setAlignment(menuBox, Pos.CENTER);
    }


    public void updatePlayerAfterDeath(Player player) {
        UserGameStateDAO userGameStateDAO = new UserGameStateDAO();
        UserGameState userGameState = userGameStateDAO.getUserGameStateByUsername(UserState.getInstance().getUsername());
        if (userGameState != null) {
            userGameState.setScore(userGameState.getScore() + player.getScore());
            userGameState.setBalance((float) (userGameState.getBalance() + player.getPlayerCoins()));
            userGameStateDAO.updateUserGameState(userGameState);
        }
    }

    public void updateMapAfterDeath(Map map) {
        MapDAO mapDAO = new MapDAO();
        map.setTimesPlayed(map.getTimesPlayed() + 1);
        mapDAO.updateMap(map);
    }

    public void updatePlayerStatisticsAfterDeath(Player player) {
        UserStatisticsDAO userStatisticsDAO = new UserStatisticsDAO();
        UserStatistics userStatistics = userStatisticsDAO.getUserStatisticsByUsername(UserState.getInstance().getUsername());
        if (userStatistics != null) {
            userStatistics.setTotalMapsPlayed(userStatistics.getTotalMapsPlayed() + 1);
            userStatistics.setTotalDeaths(userStatistics.getTotalDeaths() + 1);
            userStatistics.setTotalMonstersKilled(userStatistics.getTotalMonstersKilled() + player.getCountOfKilledMonsters());
            userStatistics.setTotalChestsOpened(userStatistics.getTotalChestsOpened() + player.getCountOfOpenedChest());
            userStatisticsDAO.updateUserStatistics(userStatistics);
        }
    }

    public void shakeCamera() {
        if (controller.isGameOver()) return;

        double originalX = controller.getCenterStack().getTranslateX();
        double originalY = controller.getCenterStack().getTranslateY();
        Random random = new Random();

        int durationMs = 500;
        int intervalMs = 20;
        int shakeTimes = durationMs / intervalMs;

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            int count = 0;

            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (controller.isGameOver()) {
                        controller.getCenterStack().setTranslateX(0);
                        controller.getCenterStack().setTranslateY(0);
                        timer.cancel();
                        return;
                    }

                    if (count < shakeTimes) {
                        double offsetX = (random.nextDouble() - 0.5) * 10;
                        double offsetY = (random.nextDouble() - 0.5) * 10;
                        controller.getCenterStack().setTranslateX(originalX + offsetX);
                        controller.getCenterStack().setTranslateY(originalY + offsetY);
                        count++;
                    } else {
                        controller.getCenterStack().setTranslateX(originalX);
                        controller.getCenterStack().setTranslateY(originalY);
                        timer.cancel();
                    }
                });
            }
        };
        timer.scheduleAtFixedRate(task, 0, intervalMs);
    }

    public void showShopPopup(boolean isStoryMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gnome/gnome/pages/shop.fxml"));
            if (MainApplication.lang == 'S'){
                loader.setResources(ResourceBundle.getBundle("slovak"));
            }
            else{
                loader.setResources(ResourceBundle.getBundle("english"));
            }

            Parent shopRoot = loader.load();

            ShopController shopController = loader.getController();
            shopController.setGameController(controller);

            showDarkOverlay();

            controller.getCenterStack().getChildren().add(shopRoot);
            StackPane.setAlignment(shopRoot, Pos.CENTER);

            shopController.setOnExit(() -> {
                controller.getCenterStack().getChildren().remove(shopRoot);
                hideDarkOverlay();
                controller.onSceneExit(false);
                pageSwitch.goMainMenu(controller.getRootBorder());
            });

            shopController.setOnContinue(() -> {
                controller.getCenterStack().getChildren().remove(shopRoot);
                hideDarkOverlay();

                if (isStoryMode) {
                    controller.closeShopAndStartNewGame();
                }
                // else do nothing — just close the popup
            });

        } catch (IOException e) {
            logger.severe("Failed to load shop popup: " + e.getMessage());
        }
    }



    private void showDarkOverlay() {
        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        overlay.prefWidthProperty().bind(controller.getCenterStack().widthProperty());
        overlay.prefHeightProperty().bind(controller.getCenterStack().heightProperty());
        controller.getCenterStack().getChildren().add(overlay);
    }

    private void hideDarkOverlay() {
        controller.getCenterStack().getChildren().removeIf(node -> node instanceof Pane && "-fx-background-color: rgba(0, 0, 0, 0.6);".equals(node.getStyle()));
    }

    public void showTablePopup(String titleText) {
        Popup popup = new Popup();

        VBox popupContent = new VBox(10);
        popupContent.setAlignment(Pos.CENTER);
        popupContent.getStylesheets().add(getClass().getResource("/com/gnome/gnome/pages/css/game-ui.css").toExternalForm());
        popupContent.getStyleClass().add("table-popup");

        Label title = new Label(titleText);
        title.getStyleClass().add("table-title");

        Button closeButton = new Button(bundle.getString("button.close"));
        closeButton.getStyleClass().add("table-button");
        closeButton.setOnAction(e -> popup.hide());

        popupContent.getChildren().addAll(title, closeButton);

        popup.getContent().add(popupContent);
        popup.setAutoHide(true);

        Scene scene = controller.getCenterStack().getScene();
        if (scene != null) {
            Bounds bounds = scene.getRoot().localToScreen(scene.getRoot().getBoundsInLocal());
            popup.show(scene.getWindow(),
                    bounds.getMinX() + bounds.getWidth() / 2 - 100,
                    bounds.getMinY() + bounds.getHeight() / 2 - 75);
        }
    }

    public void showStatisticsPopup(boolean isStoryMode, Runnable afterCloseAction) {
        Player player = controller.getPlayer();

        VBox popupContent = new VBox(10);
        popupContent.setAlignment(Pos.CENTER);
        popupContent.getStylesheets().add(getClass().getResource("/com/gnome/gnome/pages/css/game-ui.css").toExternalForm());
        popupContent.getStyleClass().add("stats-popup");

        Label title = new Label(bundle.getString("label.level.statistics"));
        title.getStyleClass().add("stats-title");

        Label coinsLabel = new Label(String.format(bundle.getString("stats.coins"), (int) player.getPlayerCoins()));
        coinsLabel.getStyleClass().add("stats-label");
        Label scoreLabel = new Label( String.format(bundle.getString("stats.score"), player.getScore()));
        scoreLabel.getStyleClass().add("stats-label");
        Label chestsLabel = new Label(String.format(bundle.getString("stats.chests"), player.getCountOfOpenedChest()));
        chestsLabel.getStyleClass().add("stats-label");
        Label killedLabel = new Label(String.format(bundle.getString("stats.monsters"), player.getCountOfKilledMonsters()));
        killedLabel.getStyleClass().add("stats-label");

        Button continueButton = new Button(bundle.getString("button.continue"));
        continueButton.getStyleClass().add("stats-button");
        continueButton.setOnAction(e -> {
            controller.getCenterStack().getChildren().remove(popupContent);
            controller.getCenterStack().getChildren().removeIf(node -> node.getStyle() != null && node.getStyle().contains("rgba(0, 0, 0, 0.6)"));
            if (afterCloseAction != null) afterCloseAction.run();
        });

        popupContent.getChildren().addAll(title, coinsLabel, scoreLabel, chestsLabel, killedLabel, continueButton);

        showDarkOverlay();
        controller.getCenterStack().getChildren().add(popupContent);
        StackPane.setAlignment(popupContent, Pos.CENTER);
    }

    public void showInfo(Stage stage, String message) {
        // Check if an info popup is already visible
        if (infoPopup != null && infoPopup.isShowing()) {
            logger.info("Info popup is already visible. Skipping new popup with message: " + message);
            return;
        }

        // Create a new popup
        infoPopup = new Popup();
        infoPopup.setAutoHide(true);

        // Create the popup content
        Label label = new Label(message);
        label.getStyleClass().add("info-message");

        VBox box = new VBox(label);
        box.getStylesheets().add(getClass().getResource("/com/gnome/gnome/pages/css/game-ui.css").toExternalForm());
        box.getStyleClass().add("info-box");
        box.setAlignment(Pos.CENTER);

        infoPopup.getContent().add(box);

        // Show the popup
        Scene scene = stage.getScene();
        if (scene != null) {
            infoPopup.show(stage);

            // Calculate position after layout
            box.applyCss();
            box.layout();

            double centerX = stage.getX() + (scene.getWidth() - box.getWidth()) / 2;
            double centerY = stage.getY() + (scene.getHeight() - box.getHeight()) / 2;

            infoPopup.setX(centerX);
            infoPopup.setY(centerY);

            // Add fade animation
            FadeTransition fade = new FadeTransition(Duration.seconds(2), box);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setDelay(Duration.seconds(1.5));
            fade.setOnFinished(e -> {
                infoPopup.hide();
                infoPopup = null; // Clear the reference when the popup hides
            });
            fade.play();
        }
    }
}