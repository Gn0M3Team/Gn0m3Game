package com.gnome.gnome.game;

import com.gnome.gnome.components.PlayerHealthBar;
import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.dao.UserStatisticsDAO;
import com.gnome.gnome.dao.userDAO.UserGameStateDAO;
import com.gnome.gnome.models.Map;
import com.gnome.gnome.models.UserStatistics;
import com.gnome.gnome.models.user.UserGameState;
import com.gnome.gnome.player.Player;
import com.gnome.gnome.shop.controllers.ShopController;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import com.gnome.gnome.userState.UserState;
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
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class GameUIManager {
    private final GameController controller;
    private final Logger logger = Logger.getLogger(GameUIManager.class.getName());
    @Getter
    private Stage currentPopup;

    public GameUIManager(GameController controller) {
        this.controller = controller;
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
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        overlay.prefWidthProperty().bind(controller.getCenterStack().widthProperty());
        overlay.prefHeightProperty().bind(controller.getCenterStack().heightProperty());

        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: red;");

        Label coinsLabel = new Label("Coins collected: " + (int) player.getPlayerCoins());
        Label scoreLabel = new Label("Score: " + player.getScore());
        Label chestsLabel = new Label("Opened chests: " + player.getCountOfOpenedChest());
        Label killedLabel = new Label("Monsters killed: " + player.getCountOfKilledMonsters());

        for (Label statLabel : List.of(coinsLabel, scoreLabel, chestsLabel, killedLabel)) {
            statLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px;");
        }

        Button restartButton = new Button("Restart");
        restartButton.setOnAction(e -> controller.restartGame());

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> Platform.exit());

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
        if (controller.getCenterMenuPopup() != null) return;

        Popup popup = new Popup();
        controller.getGameLoop().stop();
        controller.setStop(true);
        popup.setAutoHide(true);

        Pane darkOverlay = new Pane();
        darkOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        darkOverlay.prefWidthProperty().bind(controller.getCenterStack().widthProperty());
        darkOverlay.prefHeightProperty().bind(controller.getCenterStack().heightProperty());
        controller.getCenterStack().getChildren().add(darkOverlay);

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setStyle("-fx-background-color: #C0C0C0; -fx-padding: 20; -fx-background-radius: 20;");
        menuBox.getStyleClass().add("menu-popup");

        Label title = new Label("MENU");
        title.getStyleClass().add("menu-title");

        Button resumeButton = new Button("Resume");
        resumeButton.setOnAction(e -> {
            popup.hide();
            controller.getGameLoop().start();
            controller.setStop(false);
            controller.getCenterStack().getChildren().remove(darkOverlay);
        });

        Button settingsButton = new Button("Settings");
        settingsButton.setOnAction(e -> {
            new SwitchPage().goSetting(controller.getRootBorder());
            popup.hide();
            controller.getCenterStack().getChildren().remove(darkOverlay);
        });

        Button goBackButton = new Button("Go Back");
        goBackButton.setOnAction(e -> {
            new SwitchPage().goMainMenu(controller.getRootBorder());
            popup.hide();
            controller.getCenterStack().getChildren().remove(darkOverlay);
        });

        menuBox.getChildren().addAll(title, resumeButton, settingsButton, goBackButton);
        popup.getContent().add(menuBox);

        Scene scene = controller.getCenterMenuButton().getScene();
        if (scene != null) {
            Bounds bounds = scene.getRoot().localToScreen(scene.getRoot().getBoundsInLocal());
            popup.show(scene.getWindow(),
                    bounds.getMinX() + bounds.getWidth() / 2 - 100,
                    bounds.getMinY() + bounds.getHeight() / 2 - 75);
        }

        controller.setCenterMenuPopup(popup);
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
            Parent shopRoot = loader.load();
            Scene shopScene = new Scene(shopRoot);
            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initOwner(controller.getCenterMenuButton().getScene().getWindow());
            popup.setTitle("Shop");
            popup.setScene(shopScene);
            popup.setResizable(false);

            showDarkOverlay();
            popup.setOnHidden(e -> {
                hideDarkOverlay();
                if (isStoryMode) controller.closeShopAndStartNewGame();
                else controller.restartGame();
            });

            ShopController shopController = loader.getController();
            shopController.setGameController(controller);

            popup.showAndWait();
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
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: black; -fx-border-width: 2;");

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button closeButton = new Button("Close");
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
        popupContent.setStyle("-fx-background-color: #ffffff; -fx-padding: 20; -fx-border-color: black; -fx-border-width: 2;");

        Label title = new Label("Level Statistics");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        Label coinsLabel = new Label("Coins collected: " + (int) player.getPlayerCoins());
        Label scoreLabel = new Label("Score: " + player.getScore());
        Label chestsLabel = new Label("Opened chests: " + player.getCountOfOpenedChest());
        Label killedLabel = new Label("Monsters killed: " + player.getCountOfKilledMonsters());

        for (Label statLabel : List.of(coinsLabel, scoreLabel, chestsLabel, killedLabel)) {
            statLabel.setStyle("-fx-font-size: 16px;");
        }

        Button continueButton = new Button("Continue");
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


}
