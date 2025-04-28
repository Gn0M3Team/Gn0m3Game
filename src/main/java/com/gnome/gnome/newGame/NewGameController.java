package com.gnome.gnome.newGame;

import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.dto.UserState;
import com.gnome.gnome.game.MapLoader;
import com.gnome.gnome.models.Map;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.geometry.Pos;

import java.util.List;

public class NewGameController {

    @FXML private Button storyModeButton;
    @FXML private Button otherModeButton;
    @FXML private Button backButton;
    @FXML private BorderPane newGameBorderPane;

    private PageSwitcherInterface pageSwitch;

    private Popup storyPopup;
    private Popup loadingPopup;

    Stage primaryStage;

    MapLoader mapLoader;




    @FXML
    public void initialize() {
        pageSwitch = new SwitchPage();
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

    @FXML
    private void showStoryMaps(ActionEvent event) {
        pageSwitch.goStoryMaps(newGameBorderPane);
    }
}
