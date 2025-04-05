package com.gnome.gnome.login.controller;

import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class LoginPageController {

    @FXML
    private BorderPane loginPage;
    private PageSwitcherInterface pageSwitch;

    @FXML
    private void initialize(){
        pageSwitch=new SwitchPage();
    }

    @FXML
    private void onSwitcherButtonClick()throws IOException  {
        pageSwitch.goSwitch(loginPage);
    }
    @FXML
    private void onAccountButtonClick()throws IOException  {
        pageSwitch.goAccount(loginPage);
    }
}
