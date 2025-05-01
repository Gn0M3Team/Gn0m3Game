package com.gnome.gnome.game;

import com.gnome.gnome.models.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

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
            Label loadingLabel = new Label("Loading...");
            loadingLabel.setStyle("""
                -fx-font-family: 'Press Start 2P';
                -fx-font-size: 22px;
                -fx-text-fill: white;
                -fx-effect: dropshadow(gaussian, black, 2, 0.5, 1, 1);
        """);

            Label tipLabel = new Label(getRandomTip());
            tipLabel.setWrapText(true);
            tipLabel.setMaxWidth(500);
            tipLabel.setStyle("""
                -fx-font-family: 'Press Start 2P';
                -fx-font-size: 12px;
                -fx-text-fill: lightgray;
                -fx-padding: 15px 0 0 0;
        """);

            VBox box = new VBox(loadingLabel, tipLabel);
            box.setPrefSize(600, 300);
            box.setStyle("""
                -fx-background-color: rgba(0, 0, 0, 0.8);
                -fx-border-color: white;
                -fx-border-width: 2px;
                -fx-background-radius: 15px;
                -fx-border-radius: 15px;
                -fx-padding: 30px;
            """);
            box.setSpacing(30);
            box.setAlignment(Pos.CENTER);

            loadingPopup = new Popup();
            loadingPopup.getContent().add(box);
        }

        loadingPopup.show(primaryStage);
        loadingPopup.setX(primaryStage.getX() + primaryStage.getWidth() / 2 - 300);
        loadingPopup.setY(primaryStage.getY() + primaryStage.getHeight() / 2 - 150);
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
