package com.gnome.gnome.switcher.controller;

import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

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
        pageSwitch.goHello(switcherPage);
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
