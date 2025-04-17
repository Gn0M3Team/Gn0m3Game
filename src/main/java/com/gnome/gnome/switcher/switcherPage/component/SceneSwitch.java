package com.gnome.gnome.switcher.switcherPage.component;

import com.gnome.gnome.profile.ProfileController;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import java.io.IOException;
import java.util.Objects;

import javafx.scene.layout.BorderPane;
import javafx.util.Duration;

/**
 * The SceneSwitch class is responsible for switching the content of the current scene (AnchorPane)
 * by loading a new FXML-based layout and setting it as the new content.
 */
public class SceneSwitch {

    private static Object dataToPass;

    public static void setGlobalData(Object data) {
        dataToPass = data;
    }

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
                scene.getStylesheets().clear();
                if (fxml.equals("/com/gnome/gnome/pages/hello-view.fxml")){
                    scene.getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/css/style.css")).toExternalForm()
                    );
                }
                if (fxml.equals("/com/gnome/gnome/pages/continueGame.fxml")){
                    scene.getStylesheets().add(
                            Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/css/continueGame.css")).toExternalForm()
                    );
                }
                if (fxml.equals("/com/gnome/gnome/pages/new-game.fxml")){
                    scene.getStylesheets().add(
                            Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/css/new-game.css")).toExternalForm()
                    );
                }
                if (fxml.equals("/com/gnome/gnome/pages/shop-pop-up.fxml")){
                    scene.getStylesheets().add(
                            Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/css/shop-pop-up.css")).toExternalForm()
                    );
                }
                if (fxml.equals("/com/gnome/gnome/pages/setting.fxml")){
                    scene.getStylesheets().add(
                            Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/css/setting.css")).toExternalForm()
                    );
                }
                if (fxml.equals("/com/gnome/gnome/pages/login-registration.fxml")){
                    scene.getStylesheets().add(
                            Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/css/style.css")).toExternalForm()
                    );
                }



                if (fxml.endsWith("profile-page.fxml")){
                    scene.getStylesheets().add(
                            Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/css/profile.css")).toExternalForm()
                    );
                    ProfileController controller = loader.getController();
                    if (dataToPass instanceof String) {
                        controller.setPlayer((String) dataToPass);
                    }

                }

                if (dataToPass!=null){
                    dataToPass=null;
                }

                applyFadeIn(nextBorderPane);
                scene.setRoot(nextBorderPane);
            } else {
                System.err.println("No scene found for the current BorderPane");
            }


        } catch (IOException e) {
            System.err.println("Error switching to the page: " + fxml);
            System.err.println("Detailed error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    /**
     * Applies a fade-in transition to the new root node.
     */
    private void applyFadeIn(Parent root) {
        FadeTransition fade = new FadeTransition(Duration.millis(500), root);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }
}