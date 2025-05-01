package com.gnome.gnome.game;

import com.gnome.gnome.components.PlayerHealthBar;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class GameUIManager {
    private final GameController controller;
    private final Logger logger = Logger.getLogger(GameUIManager.class.getName());

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

        VBox overlay = new VBox(20);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        overlay.prefWidthProperty().bind(controller.getCenterStack().widthProperty());
        overlay.prefHeightProperty().bind(controller.getCenterStack().heightProperty());

        Label gameOverLabel = new Label("GAME OVER");
        gameOverLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: red;");

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> Platform.exit());

        Button restartButton = new Button("Restart");
        restartButton.setOnAction(e -> controller.restartGame());

        overlay.getChildren().addAll(gameOverLabel, restartButton, exitButton);
        controller.getCenterStack().getChildren().add(overlay);
        controller.setGameOverOverlay(overlay);
    }

    public void showCenterMenuPopup() {
        if (controller.getCenterMenuPopup() != null) return;

        Popup popup = new Popup();
        controller.getGameLoop().stop();
        controller.setStop(true);
        popup.setAutoHide(true);

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
        });

        Button goBackButton = new Button("Go Back");
        goBackButton.setOnAction(e -> {
            try {
                controller.onSceneExit(false);
                URL fxmlUrl = GameUIManager.class.getResource("/com/gnome/gnome/pages/main-menu.fxml");
                if (fxmlUrl == null) {
                    logger.severe("main-menu.fxml not found!");
                    return;
                }
                Parent mainRoot = FXMLLoader.load(fxmlUrl);
                Stage stage = (Stage) controller.getCenterMenuButton().getScene().getWindow();
                stage.getScene().setRoot(mainRoot);
            } catch (IOException ex) {
                logger.severe("Failed to load main page: " + ex.getMessage());
            }
            popup.hide();
        });

        menuBox.getChildren().addAll(title, resumeButton, goBackButton);
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

    public void showTablePopup(String titleText) {
        Popup popup = new Popup(); // Оголошення popup перед використанням

        VBox popupContent = new VBox(10);
        popupContent.setAlignment(Pos.CENTER);
        popupContent.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-border-color: black; -fx-border-width: 2;");

        Label title = new Label(titleText);
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> popup.hide()); // Тепер працює, бо popup вже оголошено

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

}
