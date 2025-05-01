package com.gnome.gnome.newGame;

import com.gnome.gnome.dao.*;
import com.gnome.gnome.game.MapLoaderService;
import com.gnome.gnome.game.MapLoaderUIHandler;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class NewGameController {

    @FXML private Button storyModeButton;
    @FXML private Button otherModeButton;
    @FXML private Button backButton;
    @FXML private BorderPane newGameBorderPane;

    private PageSwitcherInterface pageSwitch;

    Stage primaryStage;

    MapLoaderService mapLoaderService;




    @FXML
    public void initialize() {
        pageSwitch = new SwitchPage();
        otherModeButton.setOnAction(e -> showUnderDevelopmentAlert());
        backButton.setOnAction(e -> goBackToMainMenu());
        MonsterDAO monsterDAO = new MonsterDAO();
        ArmorDAO armorDAO = new ArmorDAO();
        WeaponDAO weaponDAO = new WeaponDAO();
        PotionDAO potionDAO = new PotionDAO();
        Platform.runLater(() -> {
            primaryStage = (Stage) storyModeButton.getScene().getWindow();
            mapLoaderService = new MapLoaderService(monsterDAO, armorDAO, weaponDAO, potionDAO);
            new MapLoaderUIHandler(mapLoaderService, primaryStage);
        });
    }

    private void goBackToMainMenu() {
        pageSwitch.goMainMenu(newGameBorderPane);
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
