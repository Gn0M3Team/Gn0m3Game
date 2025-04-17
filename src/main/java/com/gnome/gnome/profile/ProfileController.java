package com.gnome.gnome.profile;

import com.gnome.gnome.dao.userDAO.UserSession;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProfileController {

    private static final Logger logger = Logger.getLogger(ProfileController.class.getName());

    @FXML private Label nameLabel;
    @FXML private Label recordLabel;
    @FXML private Label roleLabel;
    @FXML private Label gamesPlayedLabel;
    @FXML private Label winRateLabel;
    @FXML private ListView<String> cardListView;
    @FXML private ListView<String> mapListView;
    @FXML
    private BorderPane profilePage;
    private PageSwitcherInterface pageSwitch;

    private int currentCardPage = 1;
    private final int cardPageSize = 10;
    private boolean cardLoading = false;

    private int currentMapPage = 1;
    private final int mapPageSize = 5;
    private boolean mapLoading = false;

    private String selectedPlayer;

    @FXML
    public void initialize() {
        pageSwitch = new SwitchPage();
    }

    /**
     * Initializes the profile page with data for the selected player.
     * This method is called when navigating from the leaderboard.
     */
    public void setPlayer(String playerData) {
        this.selectedPlayer = playerData;
        logger.info("Loading profile for: " + playerData);

        System.out.println(playerData);
        nameLabel.setText("Profile of " + playerData);
        recordLabel.setText("Record: 12345");
        gamesPlayedLabel.setText("Games Played: 100");
        winRateLabel.setText("Win Rate: 75%");
        roleLabel.setText("Role: User");

        cardListView.setPlaceholder(new Label("No cards available"));
        mapListView.setPlaceholder(new Label("No maps available"));

        loadMoreCards();
        loadMoreMaps();
        setupScrollPagination(cardListView, this::loadMoreCards, () -> cardLoading);
        setupScrollPagination(mapListView, this::loadMoreMaps, () -> mapLoading);
    }

    /**
     * Adds lazy-loading behavior to a ListView by monitoring its vertical scroll bar.
     * When user scrolls to the bottom, new data is fetched if not already loading.
     */
    private void setupScrollPagination(ListView<String> listView, Runnable loader, Supplier<Boolean> isLoading) {
        listView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) listView.lookup(".scroll-bar:vertical");
                if (scrollBar != null) {
                    scrollBar.valueProperty().addListener((o, oldVal, newVal) -> {
                        if (newVal.doubleValue() >= scrollBar.getMax() && !isLoading.get()) {
                            loader.run();
                        }
                    });
                }
            }
        });
    }

    /**
     * Loads a new page of cards into the card list.
     */
    private void loadMoreCards() {
        cardLoading = true;
        for (int i = 1; i <= cardPageSize; i++) {
            int cardNumber = (currentCardPage - 1) * cardPageSize + i;
            cardListView.getItems().add("Card " + cardNumber);
        }
        currentCardPage++;
        cardLoading = false;
    }

    /**
     * Loads a new page of maps into the map list.
     */
    private void loadMoreMaps() {
        mapLoading = true;
        for (int i = 1; i <= mapPageSize; i++) {
            int mapNumber = (currentMapPage - 1) * mapPageSize + i;
            mapListView.getItems().add("Map " + mapNumber);
        }
        currentMapPage++;
        mapLoading = false;
    }

    /**
     * Handles the "Back" button click to return to the main menu (hello-view.fxml).
     * Adds a fade transition effect during scene switch.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        pageSwitch.goHello(profilePage);
    }

    /**
     * Simulates banning a user.
     * Currently displays an alert and logs the action.
     */
    @FXML
    private void handleBanUser(ActionEvent event) {
        logger.warning("Ban user requested for: " + selectedPlayer);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ban User");
        alert.setHeaderText(null);
        alert.setContentText("User '" + selectedPlayer + "' has been banned (simulated).");
        alert.showAndWait();
    }

    /**
     * Opens a dialog box to allow an admin to change the role of the user.
     * Updates the UI and logs the role change.
     */
    @FXML
    private void handleEditRole(ActionEvent event) {
        List<String> choices = List.of("User", "ProUser", "Admin");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("User", choices);
        dialog.setTitle("Edit Role");
        dialog.setHeaderText("Select a new role for the user:");
        dialog.setContentText("Role:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(role -> {
            roleLabel.setText("Role: " + role);
            logger.info("Role changed to " + role + " for " + selectedPlayer);
        });
    }
}