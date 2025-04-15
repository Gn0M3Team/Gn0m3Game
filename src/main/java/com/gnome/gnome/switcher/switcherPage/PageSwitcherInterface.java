package com.gnome.gnome.switcher.switcherPage;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public interface PageSwitcherInterface {
    void goLogin(BorderPane anchorPane);
    void goRegistration(BorderPane anchorPane);
    void goAccount(BorderPane anchorPane);
    void goSwitch(BorderPane anchorPane) ;
    void goHello(BorderPane anchorPane);
//    void goHello(BorderPane anchorPane);
}
