package com.gnome.gnome.newGame;

import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;



/**
 * Controller class for the "New Game" page.
 * <p>
 * Handles user interaction on the New Game scene, including displaying
 * a center popup and switching back to the main page.
 */
public class NewGameController {

    @FXML
    private Button popupButton;
    @FXML
    private BorderPane newGamePage;

    private Popup centerMenuPopup;
    private PageSwitcherInterface pageSwitch;

    /**
     * Initializes the controller.
     * Sets up event handlers and prepares necessary objects.
     */
    @FXML
    public void initialize() {
        pageSwitch=new SwitchPage();
        popupButton.setOnAction(e -> openPopup());
    }

    /**
     * Event handler for the "Show-Pop-Up" button.
     * Navigates back to the Hello (main) page.
     *
     * @param e the action event triggered by button click
     */
    @FXML
    public void onShopPopUpButtonClick(ActionEvent e){
        pageSwitch.goShopPopUp(newGamePage);
    }
    @FXML
    public void onMeinMenuButtonClick(ActionEvent e){
        pageSwitch.goHello(newGamePage);
    }



    /**
     * Opens a popup window centered on the screen.
     * The popup contains two buttons: one to close it and another to navigate back to the main page.
     * If the popup has already been created, it simply repositions and displays it.
     */
    private void openPopup() {
        if (centerMenuPopup == null) {
            centerMenuPopup = new Popup();
            centerMenuPopup.setAutoHide(true);

            VBox menuBox = new VBox(20);
            menuBox.setAlignment(Pos.CENTER);
            menuBox.getStyleClass().add("menu-popup");
            menuBox.setStyle("-fx-background-color: #C0C0C0; -fx-padding: 20; -fx-background-radius: 20;");

            Label title = new Label("Pop up");
            title.getStyleClass().add("menu-title");

            Button option1 = new Button("Close");
            option1.getStyleClass().add("menu-button");
            Button goBackButton = new Button("Go to setting");
            goBackButton.getStyleClass().add("menu-button");

            option1.setOnAction(e -> {
                centerMenuPopup.hide();
            });

            goBackButton.setOnAction(e -> {
                centerMenuPopup.hide();
                pageSwitch.goSetting(newGamePage);
            });

            menuBox.getChildren().addAll(title, goBackButton,option1);
            centerMenuPopup.getContent().add(menuBox);
        }

        Scene scene = popupButton.getScene();
        if (scene != null) {
            Bounds bounds = scene.getRoot().localToScreen(scene.getRoot().getBoundsInLocal());
            double centerX = bounds.getMinX() + bounds.getWidth() / 2;
            double centerY = bounds.getMinY() + bounds.getHeight() / 2;
            centerMenuPopup.show(scene.getWindow(), centerX - 100, centerY - 75);
        }
    }

}
