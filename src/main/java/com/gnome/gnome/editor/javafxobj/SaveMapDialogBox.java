package com.gnome.gnome.editor.javafxobj;

import com.gnome.gnome.models.user.PlayerRole;
import com.gnome.gnome.userState.UserState;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class SaveMapDialogBox {
    private static final Logger logger = Logger.getLogger(SaveMapDialogBox.class.getName());

    private final PlayerRole userRole = UserState.getInstance().getRole();

    private boolean storyMapSelected = false; // ⚡ правильное поле, которое фиксирует реальный выбор галочки

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

        CheckBox storyMapCheckBox = new CheckBox("Mark as story map");

        VBox contentBox;
        if (userRole == PlayerRole.ADMIN || userRole == PlayerRole.MAP_CREATOR) {
            contentBox = new VBox(10, mapNameField, storyMapCheckBox);
        } else {
            contentBox = new VBox(10, mapNameField);
        }

        dialog.getDialogPane().setContent(contentBox);

        Node processButton = dialog.getDialogPane().lookupButton(processButtonType);
        processButton.setDisable(true);

        mapNameField.textProperty().addListener((observable, oldValue, newValue) ->
                processButton.setDisable(newValue.trim().isEmpty())
        );

        dialog.setResultConverter(button -> {
            if (button == processButtonType) {
                storyMapSelected = storyMapCheckBox.isSelected();
                String name = mapNameField.getText().trim();
                logger.info("Map name: " + name + ", story map selected: " + storyMapSelected);
                return name;
            }
            return null;
        });

        dialog.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/css/dialogs/dialog_save_map.css"))
                        .toExternalForm()
        );

        return dialog.showAndWait();
    }

    // Правильный геттер
    public boolean isStoryMap() {
        return storyMapSelected;
    }
}
