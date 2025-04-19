package com.gnome.gnome.editor.javafxobj;

import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.logging.Logger;

public class SaveMapDialogBox {
    private static final Logger logger = Logger.getLogger(SaveMapDialogBox.class.getName());

    /**
     * Shows the Save Map dialog.
     *
     * @param stage the owner stage for the dialog
     * @return an Optional containing the entered map name if the user confirms, or an empty Optional if cancelled
     */
    public Optional<String> showDialog(Stage stage) {
        Dialog<String> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Save Map");
        dialog.setHeaderText("Enter the name of the map");

        ButtonType processButtonType = new ButtonType("Process", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(processButtonType, ButtonType.CANCEL);


        TextField mapNameField = new TextField();
        mapNameField.setPromptText("Enter name...");
        mapNameField.requestFocus();

        dialog.getDialogPane().setContent(new VBox(10, mapNameField));

        Node processButton = dialog.getDialogPane().lookupButton(processButtonType);
        processButton.setDisable(true);
        mapNameField.textProperty().addListener((observable, oldValue, newValue) ->
                processButton.setDisable(newValue.trim().isEmpty())
        );

        dialog.setResultConverter(button ->
                button == processButtonType ? mapNameField.getText().trim() : null
        );

        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/gnome/gnome/pages/css/dialogs/dialog_save_map.css").toExternalForm());

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> logger.info("Map name entered: " + name));

        return result;
    }
}
