package com.gnome.gnome.editor.javafxobj;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A dialog for selecting a map from a list of available maps stored in the resources.
 * <p>
 * This class is responsible for loading the map names from the resources directory and
 * presenting a dialog window where the user can select one of the maps.
 * </p>
 */
public class TemplateMapDialog {

    private static final Logger logger = Logger.getLogger(TemplateMapDialog.class.getName());
    private final List<String> mapNames;

    /**
     * Constructs a TemplateMapDialog instance and initializes it by loading the available map names from resources.
     * The maps are expected to be files with the ".map" extension located in the "com/gnome/gnome/maps" directory.
     */
    public TemplateMapDialog() {
        // Load maps from resources
        this.mapNames = loadMapsFromResources();
        logger.info("Initialized with map names: " + this.mapNames);
    }

    /**
     * Loads the list of map names from the resources directory. Only files with the ".map" extension are considered valid maps.
     * <p>
     * The method checks the directory "com/gnome/gnome/maps" inside the resources and returns a list of map file names.
     * </p>
     *
     * @return a list of map names (file names) that end with ".map"
     */
    private List<String> loadMapsFromResources() {
        try {
            // Load the "maps" directory from the resources
            URL mapsDirUrl = getClass().getClassLoader().getResource("com/gnome/gnome/maps");
            if (mapsDirUrl != null) {
                File mapsDirectory = new File(mapsDirUrl.toURI());
                if (mapsDirectory.exists() && mapsDirectory.isDirectory()) {
                    // Filter the files to get only the ones with a ".map" extension
                    return Stream.of(mapsDirectory.listFiles())
                            .filter(file -> file.getName().endsWith(".map"))
                            .map(File::getName)  // Extract file names
                            .collect(Collectors.toList());
                }
            } else {
                logger.severe("Maps directory not found in resources.");
            }
        } catch (Exception e) {
            logger.severe("Failed to load maps from resources: " + e.getMessage());
        }
        return List.of();  // Return an empty list if loading maps fails
    }

    /**
     * Displays the dialog for selecting a map from the list of available maps.
     * The dialog shows a list view with all the available map names loaded from the resources.
     * <p>
     * Once a map is selected, its name is returned as an {@link Optional}.
     * If no map is selected (or the dialog is cancelled), an empty {@link Optional} is returned.
     * </p>
     *
     * @param stage the owner {@link Stage} of the dialog
     * @return an {@link Optional<String>} containing the name of the selected map, or an empty {@link Optional} if no map is selected
     */
    public Optional<String> showDialog(Stage stage) {
        Dialog<String> dialog = new Dialog<>();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Select Map");
        dialog.setHeaderText("Select a map to load:");

        // Add select and cancel buttons to the dialog
        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        // Create a ListView to display the map names
        ObservableList<String> observableMapNames = FXCollections.observableArrayList(mapNames);
        ListView<String> listView = new ListView<>(observableMapNames);

        // Set the content of the dialog to contain the ListView with map names
        dialog.getDialogPane().setContent(new VBox(10, listView));

        // Set the result converter for the dialog, returning the selected map name if "Select" is pressed
        dialog.setResultConverter(dialogButton ->
                dialogButton == selectButtonType ? listView.getSelectionModel().getSelectedItem() : null
        );

        // Apply custom styles for the dialog (CSS)
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/com/gnome/gnome/pages/css/dialogs/dialog_load_db.css").toExternalForm());

        // Show the dialog and wait for the user to choose a map or cancel
        return dialog.showAndWait();
    }
}
