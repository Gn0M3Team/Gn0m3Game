package com.gnome.gnome.game;

import com.gnome.gnome.models.*;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class MapLoaderUIHandler {

    private final MapLoaderService service;
    private final Stage primaryStage;
    private Popup loadingPopup;

    public MapLoaderUIHandler(MapLoaderService service, Stage stage) {
        this.service = service;
        this.primaryStage = stage;
    }

    public void showStartMap(Map map) {
        showLoadingPopup();

        long startTime = System.currentTimeMillis();

        service.loadMapAsync(
                (monsters, armor, weapon, potion) -> Platform.runLater(() -> {
                    long elapsed = System.currentTimeMillis() - startTime;
                    long remaining = 7_000 - elapsed;

                    if (remaining > 0) {
                        new Thread(() -> {
                            try {
                                Thread.sleep(remaining);
                            } catch (InterruptedException ignored) {}
                            Platform.runLater(() -> {
                                hideLoadingPopup();
                                loadGamePage(map, monsters, armor, weapon, potion);
                            });
                        }).start();
                    } else {
                        hideLoadingPopup();
                        loadGamePage(map, monsters, armor, weapon, potion);
                    }
                }),
                ex -> Platform.runLater(() -> {
                    hideLoadingPopup();
                    showError("Map loading failed: " + ex.getMessage());
                })
        );
    }

    private void showLoadingPopup() {
        if (loadingPopup == null) {
            // Loading Title
            Label loadingLabel = new Label("Loading...");
            loadingLabel.getStyleClass().add("loading-label");

            // Loading Indicator
            ProgressIndicator loadingIndicator = new ProgressIndicator(-1); // Indeterminate progress
            loadingIndicator.getStyleClass().add("loading-indicator");

            // Separator
            Separator separator = new Separator();
            separator.getStyleClass().add("loading-separator");

            // Tip Label
            Label tipLabel = new Label(getRandomTip());
            tipLabel.setWrapText(true);
            tipLabel.setMaxWidth(450);
            tipLabel.getStyleClass().add("tip-label");

            // Tip Container (to give the tip its own background and padding)
            VBox tipContainer = new VBox(tipLabel);
            tipContainer.getStyleClass().add("tip-container");

            // Main Container
            VBox box = new VBox(15, loadingLabel, loadingIndicator, separator, tipContainer);
            box.setPrefSize(600, 350);
            box.getStyleClass().add("loading-box");
            box.getStylesheets().add(getClass().getResource("/com/gnome/gnome/pages/css/dialogs/map-loader.css").toExternalForm());
            box.setAlignment(Pos.CENTER);

            loadingPopup = new Popup();
            loadingPopup.getContent().add(box);

            // Add fade-in animation for a gentle effect
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), box);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }

        loadingPopup.show(primaryStage);
        loadingPopup.setX(primaryStage.getX() + primaryStage.getWidth() / 2 - 300);
        loadingPopup.setY(primaryStage.getY() + primaryStage.getHeight() / 2 - 175);
    }


    private String getRandomTip() {
        String[] tips = {
                "Tip: Press 'E' to open nearby chests.",
                "Tip: Use potions to quickly recover health.",
                "Tip: Some monsters have patterns — study their movement.",
                "Tip: Find better armor to survive longer.",
                "Tip: Coins can be used to buy upgrades after a level."
        };
        int index = (int) (Math.random() * tips.length);
        return tips[index];
    }

    private void hideLoadingPopup() {
        if (loadingPopup != null) {
            loadingPopup.hide();
        }
    }

    private void loadGamePage(Map selectedMap, List<Monster> monsters, Armor armor, Weapon weapon, Potion potion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gnome/gnome/pages/game.fxml"));
            Parent root = loader.load();
            GameController controller = loader.getController();
            controller.initializeWithLoadedMap(selectedMap,selectedMap.getMapData(), monsters, armor, weapon, potion);
            primaryStage.getScene().setRoot(root);
        } catch (Exception e) {
            showError("Failed to load game view: " + e.getMessage());
        }
    }


    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Loading Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
