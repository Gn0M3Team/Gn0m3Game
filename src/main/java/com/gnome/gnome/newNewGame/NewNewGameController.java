package com.gnome.gnome.newNewGame;

import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.game.GameController;
import com.gnome.gnome.game.MapLoader;
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

import java.util.Arrays;
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

    Stage primaryStage;

    MapLoader mapLoader;


    @FXML
    public void initialize() {
        storyModeButton.setOnAction(e -> showStoryPopup());
        otherModeButton.setOnAction(e -> showUnderDevelopmentAlert());
        backButton.setOnAction(e -> goBackToMainMenu());
        Platform.runLater(() -> {
            primaryStage = (Stage) storyModeButton.getScene().getWindow();
            mapLoader = new MapLoader(primaryStage);
        });
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

            MapDAO mapDAO = new MapDAO();
            List<Map> allLevelMaps = mapDAO.getAllLevelMaps();
            int totalMaps = allLevelMaps.size();


            for (int i = 0; i < totalMaps; i++) {
                Button mapButton = new Button("Map Level" + (i+1));
                mapButton.setPrefWidth(150);
                int mapId = allLevelMaps.get(i).getId();
                mapButton.setOnAction(e -> {
                    storyPopup.hide();
                    mapLoader.showStartMap(mapId);
                });

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
}
