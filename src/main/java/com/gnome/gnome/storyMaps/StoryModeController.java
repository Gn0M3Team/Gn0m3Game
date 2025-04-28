package com.gnome.gnome.storyMaps;

import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.userState.UserState;
import com.gnome.gnome.models.Map;
import com.gnome.gnome.game.MapLoader;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;

public class StoryModeController {

    @FXML
    private BorderPane storyMapsBorderPane;

    @FXML
    private VBox mapsContainer;

    private MapLoader mapLoader;

    private PageSwitcherInterface pageSwitch;

    @FXML
    public void initialize() {
        pageSwitch = new SwitchPage();
        MapDAO mapDAO = new MapDAO();
        List<Map> allLevelMaps = mapDAO.getAllLevelMaps();
        int totalMaps = allLevelMaps.size();
        int currentLevel = UserState.getInstance().getMapLevel();

        for (int i = 0; i < totalMaps; i++) {
            Map selectedMap = allLevelMaps.get(i);
            int mapLevel = selectedMap.getLevel();

            // Create map entry HBox
            HBox mapEntry = new HBox(10);
            mapEntry.setPrefWidth(600);
            mapEntry.getStyleClass().add("map-entry");

            // Map preview image
            ImageView mapPreview = new ImageView();
            mapPreview.setFitWidth(100);
            mapPreview.setFitHeight(100);
            mapPreview.setPreserveRatio(true);
            mapPreview.getStyleClass().add("map-preview");

            mapPreview.setImage(new Image(
                    Objects.requireNonNull(
                            getClass().getResourceAsStream("/com/gnome/gnome/images/background.png")
                    )
            ));
//            // TODO: provide the photo of the map
//            mapPreview.setImage(new Image("/images/background.png"));


            // VBox for text and button
            VBox textContainer = new VBox(5);
            textContainer.setPrefWidth(400);

            // Map name
            Label mapName = new Label("Map " + mapLevel + ": " + selectedMap.getMapNameEng());
            mapName.getStyleClass().add("map-name");

            // Map status
            Label mapStatus = new Label();
            mapStatus.getStyleClass().add("map-status");

            // Button to start map
            Button mapButton = new Button("Play Map");
            mapButton.getStyleClass().add("menu-button");

            if (currentLevel > mapLevel) {
                mapEntry.getStyleClass().add("map-entry-completed");
                mapStatus.setText("Completed");
                mapButton.setDisable(false);
            } else if (currentLevel == mapLevel) {
                mapEntry.getStyleClass().add("map-entry-current");
                mapStatus.setText("Current Map");
                mapButton.setDisable(false);
            } else {
                mapEntry.getStyleClass().add("map-entry-locked");
                mapStatus.setText("Locked");
                mapButton.setDisable(true);
            }

            mapButton.setOnAction(e -> {
                if (!mapButton.isDisabled()) {
                    if (mapLoader == null) {
                        Stage stage = (Stage) storyMapsBorderPane.getScene().getWindow();
                        mapLoader = new MapLoader(stage);
                    }
                    mapLoader.showStartMap(selectedMap);
                }
            });

            textContainer.getChildren().addAll(mapName, mapStatus, mapButton);
            mapEntry.getChildren().addAll(mapPreview, textContainer);
            mapsContainer.getChildren().add(mapEntry);
        }
    }

    @FXML
    private void onBackButtonClick(ActionEvent event) {
        pageSwitch.goNewGame(storyMapsBorderPane);
    }

    @FXML
    private void onMusicIconClick() {
        // Implement music toggle logic
    }
}