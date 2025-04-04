package com.gnome.gnome.account.controller;

import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class AccountController {

    @FXML
    protected AnchorPane accountPage;

    private PageSwitcherInterface pageSwitch;

    @FXML
    protected void initialize(){
        pageSwitch=new SwitchPage();
    }

    @FXML
    protected void onSwitchButtonClick()throws IOException {
        pageSwitch.goSwitch(accountPage);
    }

}
