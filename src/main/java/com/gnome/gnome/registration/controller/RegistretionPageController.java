package com.gnome.gnome.registration.controller;

import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class RegistretionPageController {

    @FXML
    private AnchorPane registrationPage;
    private PageSwitcherInterface pageSwitch;

    @FXML
    public void initialize(){
        pageSwitch=new SwitchPage();
    }

    @FXML
    private void onLoginButtonClick(ActionEvent event) throws IOException {
        pageSwitch.goLogin(registrationPage);
    }
    @FXML
    private void onGoSwitchButtonClick(ActionEvent event) throws IOException {
        pageSwitch.goSwitch(registrationPage);
    }


}
