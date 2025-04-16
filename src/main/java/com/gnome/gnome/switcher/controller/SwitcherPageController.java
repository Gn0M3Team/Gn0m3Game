package com.gnome.gnome.switcher.controller;

import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class SwitcherPageController {

    private PageSwitcherInterface pageSwitch;
    @FXML
    public BorderPane switcherPage;

    @FXML
    public void initialize() {
        pageSwitch = new SwitchPage();
    }
    @FXML
    protected void onHelloViewButtonClick(ActionEvent event) {
        pageSwitch.mainMenu(switcherPage);
    }


    @FXML
    public void onLoginButtonClick(ActionEvent event) {
        pageSwitch.goLogin(switcherPage);
    }
    @FXML
    public void onRegistrationButtonClick(ActionEvent event) {
        pageSwitch.goRegistration(switcherPage);
    }
    @FXML
    public void onAccountButtonClick(ActionEvent event) {
        pageSwitch.goAccount(switcherPage);
    }

}
