package com.gnome.gnome.switcher.switcherPage;

import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public interface PageSwitcherInterface {
    void goLogin(AnchorPane anchorPane);
    void goRegistration(AnchorPane anchorPane);
    void goAccount(AnchorPane anchorPane);
    void goSwitch(AnchorPane anchorPane) ;
}
