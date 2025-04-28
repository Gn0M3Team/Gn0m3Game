package com.gnome.gnome.game;

import com.gnome.gnome.dao.MapDAO;

import com.gnome.gnome.models.Map;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapLoader {

    private ExecutorService executor;

    private Popup loadingPopup;
    private Stage primaryStage;

    public MapLoader() {
    }


    public MapLoader(Stage primaryStage) {
        this.executor = Executors.newSingleThreadExecutor();
        this.primaryStage = primaryStage;
    }



    public void showStartMap(int mapId) {
        showLoadingPopup();

        executor.submit(() -> {
            try {
                MapDAO levelMapDAO = new MapDAO();
                Map selectedMap = levelMapDAO.getMapById(mapId);

                if (selectedMap != null) {
                    Platform.runLater(() -> loadGamePage(selectedMap));
                } else {
                    Platform.runLater(() -> {
                        hideLoadingPopup();
                        showError("No maps found in database!");
                    });
                }

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    hideLoadingPopup();
                    showError("Failed to load map: " + ex.getMessage());
                });
            }
        });
    }

    private void showLoadingPopup() {
        if (loadingPopup == null) {
            loadingPopup = new Popup();
            Label loadingLabel = new Label("Loading... Please wait");
            loadingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-background-color: #222; -fx-padding: 20px; -fx-background-radius: 10;");
            VBox box = new VBox(loadingLabel);
            box.setAlignment(Pos.CENTER);
            box.setStyle("-fx-background-color: transparent;");
            loadingPopup.getContent().add(box);
            loadingPopup.setAutoHide(false);
        }

        if (primaryStage != null) {
            loadingPopup.show(primaryStage);
            loadingPopup.setX(primaryStage.getX() + primaryStage.getWidth() / 2 - 100);
            loadingPopup.setY(primaryStage.getY() + primaryStage.getHeight() / 2 - 50);
        }
    }

    private void hideLoadingPopup() {
        if (loadingPopup != null) {
            loadingPopup.hide();
        }
    }

    private void loadGamePage(Map selectedMap) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gnome/gnome/pages/game.fxml"));
            Parent root = loader.load();
            GameController controller = loader.getController();
            controller.initializeWithLoadedMap(selectedMap.getMapData());

            primaryStage.getScene().setRoot(root);

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Failed to load Continue Game page.");
        } finally {
            hideLoadingPopup();
        }
    }


    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
