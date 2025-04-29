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

        service.loadMapAsync(
                (monsters, armor, weapon, potion) -> Platform.runLater(() -> {
                    hideLoadingPopup();
                    loadGamePage(map, monsters, armor, weapon, potion);
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
            loadingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-background-color: #333; -fx-padding: 20px;");
            VBox box = new VBox(loadingLabel);
            box.setAlignment(Pos.CENTER);
            loadingPopup = new Popup();
            loadingPopup.getContent().add(box);
        }

        loadingPopup.show(primaryStage);
        loadingPopup.setX(primaryStage.getX() + primaryStage.getWidth() / 2 - 100);
        loadingPopup.setY(primaryStage.getY() + primaryStage.getHeight() / 2 - 50);
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
            controller.initializeWithLoadedMap(selectedMap.getMapData(), monsters, armor, weapon, potion);
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
