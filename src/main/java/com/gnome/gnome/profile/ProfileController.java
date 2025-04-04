package com.gnome.gnome.profile;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ProfileController {

    @FXML private Label nameLabel;
    @FXML private Label recordLabel;
    @FXML private Label roleLabel;
    @FXML private Label gamesPlayedLabel;
    @FXML private Label winRateLabel;
    @FXML private ListView<String> cardListView;
    @FXML private ListView<String> mapListView;

    private int currentCardPage = 1;
    private final int cardPageSize = 10;
    private boolean cardLoading = false;

    private int currentMapPage = 1;
    private final int mapPageSize = 5;
    private boolean mapLoading = false;

    /**
     * Called by LeaderBoardView to initialize this profile page with the selected player's data.
     * @param playerData the selected player's identifier (e.g., "bot3 - 12357")
     */
    public void setPlayer(String playerData) {
        nameLabel.setText("Profile of " + playerData);
        recordLabel.setText("Record: 12345");

        gamesPlayedLabel.setText("Games Played: 100");
        winRateLabel.setText("Win Rate: 75%");
        roleLabel.setText("Role: User");  // default role

        cardListView.setPlaceholder(new Label("No cards available"));
        mapListView.setPlaceholder(new Label("No maps available"));

        loadMoreCards();
        loadMoreMaps();

        cardListView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) cardListView.lookup(".scroll-bar:vertical");
                if (scrollBar != null) {
                    scrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.doubleValue() >= scrollBar.getMax() && !cardLoading) {
                            loadMoreCards();
                        }
                    });
                }
            }
        });

        mapListView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar mapScrollBar = (ScrollBar) mapListView.lookup(".scroll-bar:vertical");
                if (mapScrollBar != null) {
                    mapScrollBar.valueProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue.doubleValue() >= mapScrollBar.getMax() && !mapLoading) {
                            loadMoreMaps();
                        }
                    });
                }
            }
        });
    }

    /**
     * Loads additional card items (simulating pagination) and appends them to the cardListView.
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
     * Loads additional map items (simulating pagination) and appends them to mapListView.
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
     * Handles the Back button action to return to the main page.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Parent mainRoot = FXMLLoader.load(
                    Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/hello-view.fxml"))
            );
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();


            FadeTransition fade = new FadeTransition(Duration.millis(300), mainRoot);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();

            stage.getScene().setRoot(mainRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the Ban User button action.
     * (For demonstration, this prints a message. Replace with real functionality.)
     */
    @FXML
    private void handleBanUser(ActionEvent event) {
        System.out.println("Ban User clicked! Implement admin functionality here.");
    }

    @FXML
    private void handleEditRole(ActionEvent event) {
        List<String> choices = new ArrayList<>();
        choices.add("User");
        choices.add("ProUser");
        choices.add("Admin");

        ChoiceDialog<String> dialog = new ChoiceDialog<>("User", choices);
        dialog.setTitle("Edit Role");
        dialog.setHeaderText("Select a role for the user:");
        dialog.setContentText("Role:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(selectedRole -> {
            roleLabel.setText("Role: " + selectedRole);
            System.out.println("Selected role: " + selectedRole);
        });
    }
}