package com.gnome.gnome.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Window;

public class AlertUtil {

    public static void showInfo(Window owner, String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, owner, title, content);
    }

    public static void showWarning(Window owner, String title, String content) {
        showAlert(Alert.AlertType.WARNING, owner, title, content);
    }

    public static void showError(Window owner, String title, String content) {
        showAlert(Alert.AlertType.ERROR, owner, title, content);
    }

    public static void showSuccess(Window owner, String content) {
        showAlert(Alert.AlertType.INFORMATION, owner, "Success", content);
    }

    private static void showAlert(Alert.AlertType type, Window owner, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(owner);

        // Apply the custom stylesheet to the dialog pane
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(AlertUtil.class.getResource("/com/gnome/gnome/pages/css/alert/alert.css").toExternalForm());

        alert.showAndWait();
    }
}