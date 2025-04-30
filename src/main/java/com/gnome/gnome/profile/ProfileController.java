package com.gnome.gnome.profile;

import com.gnome.gnome.dao.UserStatisticsDAO;
import com.gnome.gnome.dao.userDAO.AuthUserDAO;
import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.dao.userDAO.UserGameStateDAO;
import com.gnome.gnome.models.Map;
import com.gnome.gnome.models.UserStatistics;
import com.gnome.gnome.models.user.AuthUser;
import com.gnome.gnome.models.user.PlayerRole;
import com.gnome.gnome.models.user.UserGameState;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import com.gnome.gnome.userState.UserState;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class ProfileController {

    private static final Logger logger = Logger.getLogger(ProfileController.class.getName());

    // UI Components
    @FXML private Label nameLabel;
    @FXML private Label recordLabel;
    @FXML private Label roleLabel;
    @FXML private Label mapLevel;
    @FXML private Label totalMapsPlayed;
    @FXML private Label totalWins;
    @FXML private Label totalDeaths;
    @FXML private Label totalMonsterKilled;
    @FXML private Label totalChestOpened;
    @FXML private Label winningPercentage;
    @FXML private Label deathCounter;
    @FXML private ImageView avatarImage;
    @FXML private Button banUserButton;
    @FXML private Button leftButton;
    @FXML private Button rightButton;
    @FXML private Button confirmRoleButton;
    @FXML private ListView<String> mapListView;
    @FXML private BorderPane profilePage;
    @FXML private ScrollPane mainScrollPane;

    // Dependencies
    private PageSwitcherInterface pageSwitch;
    private final AuthUserDAO userDAO = new AuthUserDAO();
    private final MapDAO mapDAO = new MapDAO();
    private final UserState userState = UserState.getInstance();

    // State
    private boolean mapLoading = false;
    private List<Map> userMaps;
    private PlayerRole selectedUserRole;
    private String selectedUsername;
    private AuthUser user;
    private final List<PlayerRole> roles = List.of(PlayerRole.USER, PlayerRole.MAP_CREATOR);
    private int currentRoleIndex = 0;

    @FXML
    public void initialize() {
        // Initialize page switcher
        pageSwitch = new SwitchPage();

        // Configure main ScrollPane
        if (mainScrollPane != null) {
            mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        } else {
            logger.warning("mainScrollPane is null during initialization");
        }
    }

    /**
     * Sets up the profile page with data for the selected player.
     * @param playerData The player data string in the format "Score: username"
     */
    public void setPlayer(String playerData) {
        if (playerData == null) {
            logger.warning("Invalid playerData: " + playerData);
            System.out.println("Invalid playerData: ");
            return;
        }

        this.selectedUsername = playerData;
        logger.info("Loading profile for: " + selectedUsername);

        // Load user data
        user = userDAO.getAuthUserByUsername(selectedUsername);
        if (user == null) {
            logger.warning("User not found: " + selectedUsername);
            return;
        }

        // Load user maps
        userMaps = mapDAO.getMapsByUsernameOrdered(selectedUsername);

        // Load game state and statistics
        UserGameStateDAO gameStateDAO = new UserGameStateDAO();
        UserStatisticsDAO userStatisticsDAO = new UserStatisticsDAO();
        UserGameState gameState = gameStateDAO.getUserGameStateByUsername(selectedUsername);
        UserStatistics userStatistics = userStatisticsDAO.getUserStatisticsByUsername(selectedUsername);

        // Update UI with user data
        nameLabel.setText("Profile of " + user.getUsername());
        recordLabel.setText("Score: " + (gameState != null ? gameState.getScore() : 0));

        if (userStatistics != null) {
            totalMapsPlayed.setText("Games Played: " + userStatistics.getTotalMapsPlayed());
            totalWins.setText("Wins: " + userStatistics.getTotalWins());
            totalDeaths.setText("Deaths: " + userStatistics.getTotalDeaths());
            totalMonsterKilled.setText("Monsters Killed: " + userStatistics.getTotalMonstersKilled());
            totalChestOpened.setText("Chests Opened: " + userStatistics.getTotalChestsOpened());
            double ratio = userStatistics.getTotalMapsPlayed() > 0
                    ? (double) userStatistics.getTotalWins() / userStatistics.getTotalMapsPlayed()
                    : 0;
            double rounded = Math.round(ratio * 100.0) / 100.0;
            winningPercentage.setText("Win Rate: " + rounded + "%");
            deathCounter.setText("Death Count: " + userStatistics.getTotalDeaths());
        } else {
            logger.warning("User statistics not found for: " + selectedUsername);
            resetStats();
        }

        if (gameState != null) {
            mapLevel.setText("Map Level: " + gameState.getMapLevel());
        } else {
            mapLevel.setText("Map Level: 0");
        }

        // Set default avatar
        try {
            avatarImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/gnome/gnome/images/default-avatar-2.png"))));
        } catch (Exception e) {
            logger.warning("Failed to load default avatar: " + e.getMessage());
        }

        // Set role
        selectedUserRole = user.getRole() != null ? user.getRole() : PlayerRole.USER;
        currentRoleIndex = roles.indexOf(selectedUserRole);
        if (currentRoleIndex == -1) {
            currentRoleIndex = 0;
        }
        roleLabel.setText("Role: " + selectedUserRole);

        // Update button visibility based on user permissions
        updateButtonVisibility();

        // Load maps
        mapListView.getItems().clear();
        if (userMaps == null || userMaps.isEmpty()) {
            mapListView.setPlaceholder(new Label("No maps available"));
        } else {
            loadAllMaps();
            if (mapListView != null) {
                setupScrollPagination();
            } else {
                logger.warning("mapListView is null");
            }
        }
    }

    /**
     * Resets stats labels to default values if user statistics are unavailable.
     */
    private void resetStats() {
        totalMapsPlayed.setText("Games Played: 0");
        totalWins.setText("Wins: 0");
        totalDeaths.setText("Deaths: 0");
        totalMonsterKilled.setText("Monsters Killed: 0");
        totalChestOpened.setText("Chests Opened: 0");
        winningPercentage.setText("Win Rate: 0%");
        deathCounter.setText("Death Count: 0");
    }

    /**
     * Updates visibility of admin buttons based on user permissions.
     */
    private void updateButtonVisibility() {
        boolean isAdmin = userState.getRole().equals(PlayerRole.ADMIN);
        boolean isTargetAdmin = selectedUserRole.equals(PlayerRole.ADMIN);

        System.out.println("ROLE: " + selectedUserRole);

        if (!isAdmin || isTargetAdmin) {
            banUserButton.setVisible(false);
            banUserButton.setManaged(false);
            roleLabel.setDisable(true);
            leftButton.setVisible(false);
            leftButton.setManaged(false);
            rightButton.setVisible(false);
            rightButton.setManaged(false);
            confirmRoleButton.setVisible(false);
            confirmRoleButton.setManaged(false);
        } else {
            banUserButton.setVisible(true);
            banUserButton.setManaged(true);
            roleLabel.setDisable(false);
            leftButton.setVisible(true);
            leftButton.setManaged(true);
            rightButton.setVisible(true);
            rightButton.setManaged(true);
            confirmRoleButton.setVisible(true);
            confirmRoleButton.setManaged(true);
        }
    }

    /**
     * Sets up lazy-loading pagination for the map ListView.
     */
    private void setupScrollPagination() {
        mapListView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar scrollBar = (ScrollBar) mapListView.lookup(".scroll-bar:vertical");
                if (scrollBar != null) {
                    scrollBar.valueProperty().addListener((o, oldVal, newVal) -> {
                        if (newVal.doubleValue() >= scrollBar.getMax() && !mapLoading) {
                            loadAllMaps();
                        }
                    });
                } else {
                    logger.warning("Vertical scroll bar not found in mapListView");
                }
            }
        });
    }

    /**
     * Loads the next batch of maps into the ListView.
     */
    private void loadAllMaps() {
        if (userMaps == null || userMaps.isEmpty()) return;

        mapListView.getItems().clear();

        for (int i = 0; i < userMaps.size(); i++) {
            Map map = userMaps.get(i);
            double successPercent = map.getTimesPlayed() > 0
                    ? (double) map.getTimesCompleted() / map.getTimesPlayed()
                    : 0;

            mapListView.getItems().add(String.format(
                    "Map %s: Success Percent -> %.2f%% # Score -> %d # Times Played -> %d # Times Completed -> %d",
                    map.getMapNameEng(),
                    successPercent,
                    map.getScoreVal(),
                    map.getTimesPlayed(),
                    map.getTimesCompleted()
            ));
        }
    }

    /**
     * Handles the "Back" button to return to the main menu.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        if (pageSwitch != null && profilePage != null) {
            pageSwitch.goMainMenu(profilePage);
        } else {
            logger.warning("Cannot navigate back: pageSwitch or profilePage is null");
        }
    }

    /**
     * Handles the "Ban User" button, showing a confirmation popup.
     */
    @FXML
    private void handleBanUser(ActionEvent event) {
        Popup confirmPopup = new Popup();
        confirmPopup.setAutoHide(true);

        VBox menuBox = new VBox(20);
        menuBox.getStylesheets().add(getClass().getResource("/com/gnome/gnome/pages/css/profile-delete-user.css").toExternalForm());
        menuBox.setAlignment(Pos.CENTER);
        menuBox.getStyleClass().add("menu-popup");
//        menuBox.setStyle("-fx-padding: 20; -fx-background-radius: 20;");

        Label title = new Label("Confirm Deletion");
        title.getStyleClass().add("popup-title");

        Label userLabel = new Label("Delete user: " + selectedUsername + "(" + selectedUserRole + ")" + "?");
        userLabel.getStyleClass().add("popup-title");

        Button yesButton = new Button("Yes, Delete");
        yesButton.getStyleClass().add("menu-button");
        Button noButton = new Button("Cancel");
        noButton.getStyleClass().add("menu-button");

        yesButton.setOnAction(e -> {
            boolean deleted = userDAO.deleteUserByUsername(selectedUsername);
            confirmPopup.hide();
            if (deleted) {
                logger.info("User deleted: " + selectedUserRole);
                pageSwitch.goMainMenu(profilePage);
            } else {
                logger.warning("Failed to delete user: " + selectedUserRole);
            }
        });

        noButton.setOnAction(e -> confirmPopup.hide());

        menuBox.getChildren().addAll(title, userLabel, yesButton, noButton);
        confirmPopup.getContent().add(menuBox);

        Scene scene = profilePage.getScene();
        if (scene != null) {
            Bounds bounds = scene.getRoot().localToScreen(scene.getRoot().getBoundsInLocal());
            double centerX = bounds.getMinX() + bounds.getWidth() / 2;
            double centerY = bounds.getMinY() + bounds.getHeight() / 2;

            confirmPopup.show(scene.getWindow());

            double popupWidth = confirmPopup.getWidth();
            double popupHeight = confirmPopup.getHeight();
            confirmPopup.setX(centerX - popupWidth / 2);
            confirmPopup.setY(centerY - popupHeight / 2);
        }
    }
    /**
     * Moves to the previous role in the role list.
     */
    @FXML
    private void handleLeftRole() {
        currentRoleIndex = (currentRoleIndex > 0) ? currentRoleIndex - 1 : roles.size() - 1;
        roleLabel.setText("Role: " + roles.get(currentRoleIndex));
    }

    /**
     * Moves to the next role in the role list.
     */
    @FXML
    private void handleRightRole() {
        currentRoleIndex = (currentRoleIndex < roles.size() - 1) ? currentRoleIndex + 1 : 0;
        roleLabel.setText("Role: " + roles.get(currentRoleIndex));
    }

    /**
     * Confirms and updates the selected role for the user.
     */
    @FXML
    private void handleConfirmRole(ActionEvent event) {
        if (user == null) {
            logger.warning("Cannot update role: user is null");
            return;
        }

        PlayerRole selectedRole = roles.get(currentRoleIndex);
        user.setRole(selectedRole);
        boolean updated = userDAO.updateUserRole(user);
        if (updated) {
            logger.info("Role updated to " + selectedRole + " for user " + selectedUsername);
            selectedUserRole = selectedRole;
            updateButtonVisibility();
        } else {
            logger.warning("Failed to update role for user: " + selectedUsername);
        }
    }
}