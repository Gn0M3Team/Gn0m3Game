package com.gnome.gnome.setting;

import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;



/**
 * Controller class for the Settings page in the application.
 *
 * This class handles user interactions within the settings view,
 * such as navigating back to the game or to the main menu.
 * It uses a {@link PageSwitcherInterface} implementation to manage page transitions.
 */
public class SettingController {


    @FXML
    private Button backGameButton;
    @FXML
    private Button mainMenuButton;
    @FXML
    private BorderPane settingPage;

    private PageSwitcherInterface pageSwitch;

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     * It initializes the {@link PageSwitcherInterface} implementation.
     */
    @FXML
    public void initialize() {
        pageSwitch = new SwitchPage();
    }

    /**
     * Handles the action of the "Back to Game" button.
     *
     * @param e the action event triggered by the button click
     */
    @FXML
    public void backToGameButtonClick(ActionEvent e) {
        pageSwitch.goNewGame(settingPage);
    }

    /**
     * Handles the action of the "Main Menu" button.
     *
     * @param e the action event triggered by the button click
     */
    @FXML
    public void mainMenuButtonClick(ActionEvent e) {
        pageSwitch.goMainMenu(settingPage);
    }
}
