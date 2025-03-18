package com.gnome.gnome.switcher.switcherPage.component;

import com.gnome.gnome.login.controller.LoginPageController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;


import java.io.IOException;
/**
 * The SceneSwitch class is responsible for switching the content of the current scene (AnchorPane)
 * by loading a new FXML-based layout and setting it as the new content.
 */
public class SceneSwitch {
    public SceneSwitch(AnchorPane curAnchorPane, String fxml) {
        try {
            if (getClass().getResource(fxml) == null) {
                throw new IOException("FXML file not found: " + fxml);
            }
//            FXMLLoader loader=new FXMLLoader(Application.class.getResource(fxml));
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            AnchorPane nextAnchorPane=loader.load();


//        if (nextAnchorPane!=null && nextAnchorPane.getId().equals("loginAnchor")){
//            LoginPageController loginPageController=loader.getController();
//        }

            Component.resize(nextAnchorPane,curAnchorPane.getPrefWidth(), curAnchorPane.getPrefHeight());
            curAnchorPane.getChildren().clear();
            curAnchorPane.getChildren().add(nextAnchorPane);
        }catch (IOException e){
            System.err.println("Error switching to the page: " + fxml);
            System.err.println("Detailed error: " + e.getMessage());
            e.printStackTrace();
        }

    }

}
