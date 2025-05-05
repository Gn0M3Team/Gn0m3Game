package com.gnome.gnome.utils;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

public class CongratsPopUp {

    public static void showSuccess(Stage primaryStage, String message) {
        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox popupContent = new VBox(20);
        popupContent.getStylesheets().add(CongratsPopUp.class.getResource("/com/gnome/gnome/pages/css/dialogs/congrats-popup.css").toExternalForm());
        popupContent.getStyleClass().add("success-popup");
        popupContent.setAlignment(Pos.CENTER);
        popupContent.setMaxWidth(600);

        Label title = new Label("Congratulations!");
        title.getStyleClass().add("popup-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("popup-message");

        Button continueButton = new Button("Continue");
        continueButton.getStyleClass().add("popup-button");
        continueButton.setOnAction(e -> popup.hide());

        popupContent.getChildren().addAll(title, messageLabel, continueButton);
        popup.getContent().add(popupContent);

        Scene scene = primaryStage.getScene();
        if (scene != null) {
            javafx.geometry.Bounds bounds = scene.getRoot().localToScreen(scene.getRoot().getBoundsInLocal());
            double centerX = bounds.getMinX() + bounds.getWidth() / 2;
            double centerY = bounds.getMinY() + bounds.getHeight() / 2;
            double popupWidth = popupContent.getWidth() > 0 ? popupContent.getWidth() : 400;
            double popupHeight = popupContent.getHeight() > 0 ? popupContent.getHeight() : 200;
            popup.setX(centerX - popupWidth / 2);
            popup.setY(centerY - popupHeight / 2);
            popup.show(primaryStage);
        }
    }
}