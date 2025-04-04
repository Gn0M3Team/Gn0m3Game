package com.gnome.gnome.components;

import com.gnome.gnome.profile.ProfileController;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;


public class LeaderBoardView extends VBox {

    private Label titleLabel;
    private TextField searchField;
    private RadioButton allRadioButton;
    private RadioButton onlyMyRadioButton;
    private ToggleGroup toggleGroup;
    private ListView<String> listView;

    private int currentPage = 1;
    private final int pageSize = 17;
    private boolean loading = false;

    public LeaderBoardView(Runnable onCloseAction) {
        this.getStyleClass().add("leaderboard-view");
        this.getStylesheets().add(
                getClass().getResource("/com/gnome/gnome/pages/css/leaderboard.css").toExternalForm()
        );

        titleLabel = new Label("RESULT");
        titleLabel.getStyleClass().add("title-label");

        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.getStyleClass().add("search-field");

        allRadioButton = new RadioButton("all");
        onlyMyRadioButton = new RadioButton("only my");
        toggleGroup = new ToggleGroup();
        allRadioButton.setToggleGroup(toggleGroup);
        onlyMyRadioButton.setToggleGroup(toggleGroup);
        allRadioButton.setSelected(true);
        allRadioButton.getStyleClass().add("radio-button");
        onlyMyRadioButton.getStyleClass().add("radio-button");

        listView = new ListView<>();
        listView.getStyleClass().add("list-view");

        loadMoreItems();

        listView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) listView.lookup(".scroll-bar:vertical");
                if (scrollBar != null) {
                    scrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.doubleValue() >= scrollBar.getMax() && !loading) {
                            loadMoreItems();
                        }
                    });
                }
            }
        });

        listView.setOnMouseClicked(this::handleListClick);

        Button closeBtn = new Button("X");
        closeBtn.getStyleClass().add("close-button");
        closeBtn.setOnAction(e -> onCloseAction.run());

        HBox titleRow = new HBox(titleLabel);
        titleRow.getStyleClass().add("top-row");
        titleRow.setAlignment(Pos.CENTER);

        HBox searchRow = new HBox(searchField);
        searchRow.getStyleClass().add("search-row");
        searchRow.setAlignment(Pos.CENTER);

        HBox radioRow = new HBox(10, allRadioButton, onlyMyRadioButton);
        radioRow.getStyleClass().add("radio-row");
        radioRow.setAlignment(Pos.CENTER);

        HBox closeRow = new HBox(closeBtn);
        closeRow.setAlignment(Pos.CENTER_RIGHT);
        closeRow.setPadding(new Insets(10, 0, 0, 0));

        this.getStyleClass().add("leaderboard-container");

        this.getChildren().addAll(
                titleRow,
                searchRow,
                radioRow,
                listView,
                closeRow
        );

        toggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == onlyMyRadioButton) {
                listView.getItems().setAll("bot1 - 12354 (mine?)");
            } else {
                listView.getItems().clear();
                currentPage = 1;
                loadMoreItems();
            }
        });
    }

    /**
     * Loads additional items (simulating pagination) and appends them to the ListView.
     */
    private void loadMoreItems() {
        loading = true;
        for (int i = 1; i <= pageSize; i++) {
            int botNumber = (currentPage - 1) * pageSize + i;
            listView.getItems().add("bot" + botNumber + " - " + (12354 + botNumber));
        }
        currentPage++;
        loading = false;
    }


    /**
     * Handles mouse clicks on the ListView. When a record is clicked, it loads the profile page.
     */
    private void handleListClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gnome/gnome/pages/profile-page.fxml"));
                    Parent profileRoot = loader.load();

                    profileRoot.getStylesheets().add(
                            Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/css/profile.css")).toExternalForm()
                    );

                    ProfileController profileController = loader.getController();
                    profileController.setPlayer(selected);

                    Stage stage = (Stage) listView.getScene().getWindow();
                    stage.getScene().setRoot(profileRoot);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}