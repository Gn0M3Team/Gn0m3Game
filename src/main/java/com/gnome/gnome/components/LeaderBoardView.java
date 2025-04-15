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
import java.util.logging.Level;
import java.util.logging.Logger;


public class LeaderBoardView extends VBox {
    private static final Logger logger = Logger.getLogger(LeaderBoardView.class.getName());


    private Label titleLabel;
    private TextField searchField;
    private RadioButton allRadioButton;
    private RadioButton onlyMyRadioButton;
    private ToggleGroup toggleGroup;
    private ListView<String> listView;

    // Pagination state for the list items
    private int currentPage = 1;
    private final int pageSize = 17;
    private boolean loading = false;

    /**
     * Constructs a LeaderBoardView.
     *
     * @param onCloseAction a Runnable to be executed when the close button is pressed.
     */
    public LeaderBoardView(Runnable onCloseAction) {
        // Set style and load the external css
        this.getStyleClass().add("leaderboard-view");
        this.getStylesheets().add(
                getClass().getResource("/com/gnome/gnome/pages/css/leaderboard.css").toExternalForm()
        );

        logger.info("Initializing LeaderBoardView...");

        initComponents();
        setupPagination();
        buildLayout(onCloseAction);
        setupToggleListener();
    }

    /**
     * Initializes UI components.
     */
    private void initComponents() {
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

        // Load the initial set of items into the ListView.
        loadMoreItems();
    }

    /**
     * Sets up pagination by adding a listener to the ListView's vertical scrollbar.
     */
    private void setupPagination() {
        listView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) listView.lookup(".scroll-bar:vertical");
                if (scrollBar != null) {
                    scrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.doubleValue() >= scrollBar.getMax() && !loading) {
                            logger.fine("User scrolled to bottom, loading more items...");
                            loadMoreItems();
                        }
                    });
                }
            }
        });
    }

    /**
     * Builds the layout by assembling UI components into rows.
     *
     * @param onCloseAction the action to execute when the close button is clicked.
     */
    private void buildLayout(Runnable onCloseAction) {
        // Row for the title
        HBox titleRow = new HBox(titleLabel);
        titleRow.getStyleClass().add("top-row");
        titleRow.setAlignment(Pos.CENTER);

        // Row for the search field
        HBox searchRow = new HBox(searchField);
        searchRow.getStyleClass().add("search-row");
        searchRow.setAlignment(Pos.CENTER);

        // Row for the radio buttons
        HBox radioRow = new HBox(10, allRadioButton, onlyMyRadioButton);
        radioRow.getStyleClass().add("radio-row");
        radioRow.setAlignment(Pos.CENTER);

        // Close button with its row
        Button closeBtn = new Button("X");
        closeBtn.getStyleClass().add("close-button");
        closeBtn.setOnAction(e -> onCloseAction.run());
        HBox closeRow = new HBox(closeBtn);
        closeRow.setAlignment(Pos.CENTER_RIGHT);
        closeRow.setPadding(new Insets(10, 0, 0, 0));

        // Add an extra style class for the container, if needed.
        this.getStyleClass().add("leaderboard-container");

        // Assemble the rows into the main VBox.
        this.getChildren().addAll(titleRow, searchRow, radioRow, listView, closeRow);

        // Set the double-click event to handle redirection.
        listView.setOnMouseClicked(this::handleListClick);
    }

    /**
     * Sets up a listener on the toggle group to filter the list items.
     */
    private void setupToggleListener() {
        toggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == onlyMyRadioButton) {
                logger.info("Filter: only my");
                listView.getItems().setAll("bot1 - 12354 (mine?)");
            } else {
                logger.info("Filter: all");
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
        logger.fine("Loading more leaderboard items...");

        for (int i = 1; i <= pageSize; i++) {
            int botNumber = (currentPage - 1) * pageSize + i;
            listView.getItems().add("bot" + botNumber + " - " + (12354 + botNumber));
        }
        currentPage++;
        loading = false;
    }

    /**
     * Handles mouse clicks on the ListView. When a record is double-clicked,
     * it loads the profile page and passes the selected player's data.
     *
     * @param event the MouseEvent triggered on the ListView.
     */
    private void handleListClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            String selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                logger.info("Opening profile for: " + selected);
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gnome/gnome/pages/profile-page.fxml"));
                    Parent profileRoot = loader.load();

                    profileRoot.getStylesheets().add(
                            Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/css/profile.css"))
                                    .toExternalForm()
                    );

                    ProfileController profileController = loader.getController();
                    profileController.setPlayer(selected);

                    Stage stage = (Stage) listView.getScene().getWindow();
                    stage.getScene().setRoot(profileRoot);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Failed to load profile-page.fxml", e);
                }
            }
        }
    }

}