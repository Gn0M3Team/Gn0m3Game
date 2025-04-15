package com.gnome.gnome.switcher.switcherPage.component;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.io.IOException;
import java.util.Objects;

import javafx.scene.layout.BorderPane;
import java.util.Objects;

/**
 * The SceneSwitch class is responsible for switching the content of the current scene (AnchorPane)
 * by loading a new FXML-based layout and setting it as the new content.
 */
public class SceneSwitch {

    public SceneSwitch(BorderPane curBorderPane, String fxml) {
        try {
            if (getClass().getResource(fxml) == null) {
                throw new IOException("FXML file not found: " + fxml);
            }
//            System.out.println(fxml);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            BorderPane nextBorderPane = loader.load();

            Scene scene = curBorderPane.getScene();
            if (scene != null) {

                if (fxml=="/com/gnome/gnome/pages/hello-view.fxml"){
                    scene.getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/css/style.css")).toExternalForm()
                    );
                }
                scene.setRoot(nextBorderPane);
//                System.out.println("Scene root updated to: " + fxml);
            } else {
//                System.err.println("No scene found for the current BorderPane");
            }


        } catch (IOException e) {
            System.err.println("Error switching to the page: " + fxml);
            System.err.println("Detailed error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}