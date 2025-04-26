package com.gnome.gnome.newNewGame;

import com.gnome.gnome.continueGame.ContinueGameController;
import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.game.GameController;
import com.gnome.gnome.models.Map;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class NewNewGameController {

    @FXML private Button storyModeButton;
    @FXML private Button otherModeButton;
    @FXML private Button backButton;

    private Popup storyPopup;
    private Popup loadingPopup;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(); // background thread for loading

    @FXML
    public void initialize() {
        storyModeButton.setOnAction(e -> showStoryPopup());
        otherModeButton.setOnAction(e -> showUnderDevelopmentAlert());
        backButton.setOnAction(e -> goBackToMainMenu());
    }

    private void goBackToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gnome/gnome/pages/main-menu.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showUnderDevelopmentAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("Other mode is under development.");
        alert.showAndWait();
    }

    private void showStoryPopup() {
        if (storyPopup == null) {
            storyPopup = new Popup();
            VBox storyBox = new VBox(15);
            storyBox.setStyle("-fx-background-color: #333; -fx-padding: 20; -fx-background-radius: 10;");
            storyBox.setAlignment(Pos.CENTER);

            for (int i = 1; i <= 4; i++) {
                Button mapButton = new Button("Map " + i);
                mapButton.setPrefWidth(150);

                if (i == 1) {
                    mapButton.setOnAction(e -> {
                        storyPopup.hide();
                        showStartMap(1);
                    });
                } else {
                    mapButton.setDisable(true);
                }

                storyBox.getChildren().add(mapButton);
            }

            storyPopup.getContent().add(storyBox);
            storyPopup.setAutoHide(true);
        }

        Scene scene = storyModeButton.getScene();
        if (scene != null) {
            Stage stage = (Stage) scene.getWindow();
            storyPopup.show(stage);
            storyPopup.setX(stage.getX() + stage.getWidth() / 2 - 100);
            storyPopup.setY(stage.getY() + stage.getHeight() / 2 - 100);
        }
    }

    // This one I should put in solo independent class.
    private void showStartMap(int mapNumber) {
        showLoadingPopup();

        executor.submit(() -> {
            try {
                // Load maps from database
                MapDAO mapDAO = new MapDAO();
                List<Map> allMaps = mapDAO.getAllMaps();

                // Simulate map selection (e.g., based on map number, for now take first map)
                Map selectedMap = allMaps.isEmpty() ? null : allMaps.getFirst();

                if (selectedMap != null) {
                    Platform.runLater(() -> {
                        loadContinueGamePage(selectedMap);
                    });
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

        Scene scene = storyModeButton.getScene();
        if (scene != null) {
            Stage stage = (Stage) scene.getWindow();
            loadingPopup.show(stage);
            loadingPopup.setX(stage.getX() + stage.getWidth() / 2 - 100);
            loadingPopup.setY(stage.getY() + stage.getHeight() / 2 - 50);
        }
    }

    private void hideLoadingPopup() {
        if (loadingPopup != null) {
            loadingPopup.hide();
        }
    }

    private void loadContinueGamePage(Map selectedMap) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gnome/gnome/pages/continue-game.fxml"));
            Parent root = loader.load();
            GameController controller = loader.getController();
            controller.initializeWithLoadedMap(selectedMap.getMapData());

            Stage stage = (Stage) storyModeButton.getScene().getWindow();
            stage.getScene().setRoot(root);

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
