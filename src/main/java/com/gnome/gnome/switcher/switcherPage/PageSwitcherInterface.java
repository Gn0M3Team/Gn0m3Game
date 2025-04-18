package com.gnome.gnome.switcher.switcherPage;

import javafx.scene.layout.BorderPane;

public interface PageSwitcherInterface {
    void goLogin(BorderPane anchorPane);
    void goRegistration(BorderPane anchorPane);
    void goAccount(BorderPane anchorPane);
    void goSwitch(BorderPane anchorPane) ;
    void mainMenu(BorderPane anchorPane);
//    void goHello(BorderPane anchorPane);
}
