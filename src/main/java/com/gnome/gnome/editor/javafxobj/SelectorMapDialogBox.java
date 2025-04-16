package com.gnome.gnome.editor.javafxobj;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class SelectorMapDialogBox {
    private static final Logger logger = Logger.getLogger(SelectorMapDialogBox.class.getName());
    private final List<String> mapNames;

    public SelectorMapDialogBox(List<String> mapNames) {
        this.mapNames = mapNames != null ? mapNames : List.of(); // Default to empty list if null
        logger.info("Initialized with map names: " + this.mapNames);
    }

    public Optional<String> showDialog(Stage stage) {
        Dialog<String> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Select Map");
        dialog.setHeaderText("Enter search term to filter the list");

        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        TextField searchField = new TextField();
        searchField.setPromptText("Search...");

        FilteredList<String> items = new FilteredList<>(FXCollections.observableArrayList(mapNames), s -> true);
        logger.info("List details: " + items);
        items.forEach((item) -> logger.info("Item: " + item));

        ListView<String> listView = new ListView<>(items);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            items.setPredicate(item -> item.toLowerCase().contains(newValue.toLowerCase()));
        });

        dialog.getDialogPane().setContent(new VBox(10, searchField, listView));

        dialog.setResultConverter(dialogButton ->
                dialogButton == selectButtonType ? listView.getSelectionModel().getSelectedItem() : null
        );

        return dialog.showAndWait();
    }
}
