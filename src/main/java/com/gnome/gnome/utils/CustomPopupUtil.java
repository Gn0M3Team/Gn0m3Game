package com.gnome.gnome.utils;

import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

public class CustomPopupUtil {

    private static Popup currentPopup = null; // Track the currently active popup

    public static void showSuccess(Stage stage, String message) {
        showPopup(stage, message, "success");
    }

    public static void showError(Stage stage, String message) {
        showPopup(stage, message, "error");
    }

    public static void showWarning(Stage stage, String message) {
        showPopup(stage, message, "warning");
    }

    public static void showInfo(Stage stage, String message) {
        // Check if a popup is currently showing
        if (currentPopup != null && currentPopup.isShowing()) {
            return; // Don't show a new popup if one is already active
        }
        showPopup(stage, message, "info");
    }

    private static void showPopup(Stage stage, String message, String type) {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        Label label = new Label(message);
        label.getStyleClass().add("popup-message");

        VBox box = new VBox(label);
        box.getStylesheets().add(CustomPopupUtil.class.getResource("/com/gnome/gnome/pages/css/dialogs/custom-popup-alert.css").toExternalForm());
        box.getStyleClass().addAll("popup-box", type);
        box.setAlignment(Pos.CENTER);

        popup.getContent().add(box);

        Scene scene = stage.getScene();
        if (scene != null) {
            currentPopup = popup; // Set the current popup
            popup.show(stage);

            // Отложенный вызов layout для получения ширины
            box.applyCss();
            box.layout();

            double centerX = stage.getX() + (scene.getWidth() - box.getWidth()) / 2;
            double centerY = stage.getY() + (scene.getHeight() - box.getHeight()) / 2;

            popup.setX(centerX);
            popup.setY(centerY);

            FadeTransition fade = new FadeTransition(Duration.seconds(2), box);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setDelay(Duration.seconds(1.5));
            fade.setOnFinished(e -> {
                popup.hide();
                currentPopup = null; // Clear the reference when the popup disappears
            });
            fade.play();
        }
    }
}