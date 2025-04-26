package com.gnome.gnome.newNewGame;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class NewNewGameController {

    @FXML
    private Button storyModeButton;

    @FXML
    private Button otherModeButton;

    private Popup storyPopup;

    @FXML
    private Button backButton;

    @FXML
    public void initialize() {
        storyModeButton.setOnAction(e -> showStoryPopup());

        otherModeButton.setOnAction(e -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Other mode is under development.");
            alert.showAndWait();
        });

        backButton.setOnAction(e -> goBackToMainMenu());
    }

    private void goBackToMainMenu() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/gnome/gnome/pages/main-menu.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(loader.load());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showStoryPopup() {
        if (storyPopup == null) {
            storyPopup = new Popup();
            VBox storyBox = new VBox(15);
            storyBox.setStyle("-fx-background-color: #333; -fx-padding: 20; -fx-background-radius: 10;");
            storyBox.setAlignment(Pos.CENTER);

            // Simulating 4 maps (first unlocked, others locked)
            for (int i = 1; i <= 4; i++) {
                Button mapButton = new Button("Map " + i);
                mapButton.setPrefWidth(150);

                if (i == 1) {
                    mapButton.setOnAction(e -> {
                        storyPopup.hide();
                        showStartMap(1);
                    });
                } else {
                    mapButton.setDisable(true); // Locked maps
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

    private void showStartMap(int mapNumber) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText("Starting Map " + mapNumber + "...");
        alert.showAndWait();
        // TODO: Here you can load the map scene (once you implement maps)
    }
}
