package com.gnome.gnome.profile;

import com.gnome.gnome.MainApplication;
import com.gnome.gnome.dao.UserStatisticsDAO;
import com.gnome.gnome.dao.userDAO.AuthUserDAO;
import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.dao.userDAO.UserGameStateDAO;
import com.gnome.gnome.game.GameController;
import com.gnome.gnome.models.Map;
import com.gnome.gnome.models.UserStatistics;
import com.gnome.gnome.models.user.AuthUser;
import com.gnome.gnome.models.user.PlayerRole;
import com.gnome.gnome.models.user.UserGameState;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import com.gnome.gnome.userState.UserState;
import com.gnome.gnome.utils.CustomPopupUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import java.util.ResourceBundle;

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
    @FXML private Label mapOwner;
    @FXML private ImageView avatarImage;
    @FXML private Button banUserButton;
    @FXML private Button leftButton;
    @FXML private Button rightButton;
    @FXML private Button confirmRoleButton;
    @FXML private ListView<String> mapListView;
    @FXML private BorderPane profilePage;
    @FXML private ScrollPane mainScrollPane;
    @FXML private Button goToGameButton;

    private ResourceBundle bundle;

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

        if (MainApplication.lang == 'S'){
            this.bundle = ResourceBundle.getBundle("slovak");
        }
        else {
            this.bundle = ResourceBundle.getBundle("english");
        }


        // Configure main ScrollPane
        if (mainScrollPane != null) {
            mainScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            mainScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        } else {
            logger.warning("mainScrollPane is null during initialization");
        }

        if (goToGameButton != null) {
            boolean isGameRunning = GameController.getGameController() != null;
            goToGameButton.setVisible(isGameRunning);
            goToGameButton.setManaged(isGameRunning);
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
        nameLabel.setText(String.format(bundle.getString("profile.title"), user.getUsername()));
        recordLabel.setText(String.format(bundle.getString("profile.score") ,  (gameState != null ? gameState.getScore() : 0)));

        String mapsProp = Objects.equals(userState.getUsername(), user.getUsername())
                ? "profile.maps.your"
                : "profile.maps.user";

        mapOwner.setText(Objects.equals(userState.getUsername(), user.getUsername())
                ? bundle.getString(mapsProp)
                : String.format(bundle.getString(mapsProp), user.getUsername()));


        if (userStatistics != null) {
            totalMapsPlayed.setText(
                    String.format(bundle.getString("profile.stats.gamesplayed"),
                    userStatistics.getTotalMapsPlayed()));

            totalWins.setText(
                    String.format(bundle.getString("profile.stats.wins"),
                    userStatistics.getTotalWins()));

            totalDeaths.setText(
                    String.format(bundle.getString("profile.stats.deaths"),
                    userStatistics.getTotalDeaths()));

            totalMonsterKilled.setText(
                    String.format(bundle.getString("profile.stats.monsterskilled"),
                    userStatistics.getTotalMonstersKilled()));

            totalChestOpened.setText(
                    String.format(bundle.getString("profile.stats.chestsopened"),
                    userStatistics.getTotalChestsOpened()));

            double ratio = userStatistics.getTotalMapsPlayed() > 0
                    ? (double) userStatistics.getTotalWins() / userStatistics.getTotalMapsPlayed()
                    : 0;
            double rounded = Math.round(ratio * 100.0) / 100.0;
            winningPercentage.setText(
                    String.format(bundle.getString("profile.stats.winrate"),
                    rounded));
            deathCounter.setText(
                    String.format(bundle.getString("profile.stats.deathcount"),
                    userStatistics.getTotalDeaths()));

        } else {
            logger.warning("User statistics not found for: " + selectedUsername);
            resetStats();
        }

        if (gameState != null) {
            mapLevel.setText(String.format(
                    bundle.getString("profile.stats.maplevel"),
                    gameState.getMapLevel()
            ));
        } else {
            mapLevel.setText(String.format(
                    bundle.getString("profile.stats.maplevel"),
                    0
            ));
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
            mapListView.setPlaceholder(new Label(bundle.getString("profile.maps.empty")));
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
        totalMapsPlayed.setText(String.format(bundle.getString("profile.stats.gamesplayed"), 0));
        totalWins.setText(String.format(bundle.getString("profile.stats.wins"), 0));
        totalDeaths.setText(String.format(bundle.getString("profile.stats.deaths"), 0));
        totalMonsterKilled.setText(String.format(bundle.getString("profile.stats.monsterskilled"), 0));
        totalChestOpened.setText(String.format(bundle.getString("profile.stats.chestsopened"), 0));
        winningPercentage.setText(String.format(bundle.getString("profile.stats.winrate"), 0.0));
        deathCounter.setText(String.format(bundle.getString("profile.stats.deathcount"), 0));
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
                    bundle.getString("profile.maps.entry"),
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

    @FXML
    public void handleGame(ActionEvent actionEvent) {
        GameController controller = GameController.getGameController();
        if (controller != null) {
            Stage stage = (Stage) profilePage.getScene().getWindow();
            Scene currentScene = stage.getScene();

            if (controller.getRootBorder() != null) {
                currentScene.setRoot(controller.getRootBorder());
            }
        }
    }

    /**
     * Handles the "Ban User" button, showing a confirmation popup.
     */
    @FXML
    private void handleBanUser(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Popup confirmPopup = new Popup();
        confirmPopup.setAutoHide(true);

        VBox menuBox = new VBox(20);
        menuBox.getStylesheets().add(getClass().getResource("/com/gnome/gnome/pages/css/profile-delete-user.css").toExternalForm());
        menuBox.setAlignment(Pos.CENTER);
        menuBox.getStyleClass().add("menu-popup");
//        menuBox.setStyle("-fx-padding: 20; -fx-background-radius: 20;");

        Label title = new Label(bundle.getString("profile.delete.title"));
        title.getStyleClass().add("popup-title");

        Label userLabel = new Label(String.format(
                bundle.getString("profile.delete.message"),
                selectedUsername,
                selectedUserRole
        ));
        userLabel.getStyleClass().add("popup-title");

        Button yesButton = new Button(bundle.getString("button.delete.yes"));
        yesButton.getStyleClass().add("menu-button");
        Button noButton = new Button(bundle.getString("button.cancel"));
        noButton.getStyleClass().add("menu-button");

        yesButton.setOnAction(e -> {
            boolean deleted = userDAO.deleteUserByUsername(selectedUsername);
            confirmPopup.hide();
            if (deleted) {
                logger.info("User deleted: " + selectedUserRole);
                pageSwitch.goMainMenu(profilePage);
                CustomPopupUtil.showSuccess(stage, bundle.getString("popup.user.deleted.success"));
            } else {
                logger.warning("Failed to delete user: " + selectedUserRole);
                CustomPopupUtil.showError(stage, String.format(bundle.getString("popup.user.delete.failed"),selectedUserRole) );
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
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        if (user == null) {
            logger.warning("Cannot update role: user is null");
            CustomPopupUtil.showError(stage, bundle.getString("popup.user.update.null"));
            return;
        }

        PlayerRole selectedRole = roles.get(currentRoleIndex);
        user.setRole(selectedRole);
        boolean updated = userDAO.updateUserRole(user);
        if (updated) {
            logger.info("Role updated to " + selectedRole + " for user " + selectedUsername);
            CustomPopupUtil.showSuccess(stage, String.format(
                    bundle.getString("popup.role.updated.success"),
                    selectedRole.toString(),  // Преобразуем enum в строку
                    selectedUsername
            ));
            selectedUserRole = selectedRole;
            updateButtonVisibility();
        } else {
            logger.warning("Failed to update role for user: " + selectedUsername);
            CustomPopupUtil.showError(stage, String.format(
                    bundle.getString("popup.role.update.failed"),
                    selectedUsername
            ));
        }
    }

}