package com.gnome.gnome.shopPopUp;

import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;

/**
 * Controller class for the Shop Pop-Up view in the application.
 *
 * This class manages user interaction within the shop pop-up,
 * such as returning to the game. It utilizes a {@link PageSwitcherInterface}
 * to handle page transitions.
 */
public class ShopPopUpController {

    @FXML
    private Button ReturnGameButton;
    @FXML
    private BorderPane shopPopUpPage;

    private PageSwitcherInterface pageSwitch;
    /**
     * Initializes the controller.
     * This method is called automatically after the FXML file is loaded.
     * It sets up the page switcher implementation.
     */
    @FXML
    public void initialize() {
        pageSwitch = new SwitchPage();
    }

    /**
     * Handles the action triggered by the "Return Game" button.
     * Switches the scene back to the game.
     *
     * @param e the action event triggered by the button click
     */
    @FXML
    public void newGameButtonClick(ActionEvent e) {
        pageSwitch.goNewGame(shopPopUpPage);
    }





}
