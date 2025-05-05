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
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.File;
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
    @FXML private Button uploadStatsButton;
    @FXML private Button downloadStatsButton;
    @FXML private Button downloadFullProfileButton;
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

        this.bundle = MainApplication.getLangBundle();

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

    private void resetStats() {
        totalMapsPlayed.setText(String.format(bundle.getString("profile.stats.gamesplayed"), 0));
        totalWins.setText(String.format(bundle.getString("profile.stats.wins"), 0));
        totalDeaths.setText(String.format(bundle.getString("profile.stats.deaths"), 0));
        totalMonsterKilled.setText(String.format(bundle.getString("profile.stats.monsterskilled"), 0));
        totalChestOpened.setText(String.format(bundle.getString("profile.stats.chestsopened"), 0));
        winningPercentage.setText(String.format(bundle.getString("profile.stats.winrate"), 0.0));
        deathCounter.setText(String.format(bundle.getString("profile.stats.deathcount"), 0));
    }

    private void updateButtonVisibility() {
        boolean isAdmin = userState.getRole().equals(PlayerRole.ADMIN);
        boolean isTargetAdmin = selectedUserRole.equals(PlayerRole.ADMIN);
        if (!isAdmin) {
            uploadStatsButton.setVisible(false);
            uploadStatsButton.setManaged(false);
            downloadStatsButton.setVisible(false);
            downloadStatsButton.setManaged(false);
            downloadFullProfileButton.setVisible(false);
            downloadFullProfileButton.setManaged(false);
        }
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
            uploadStatsButton.setVisible(true);
            uploadStatsButton.setManaged(true);
            downloadStatsButton.setVisible(true);
            downloadStatsButton.setManaged(true);
            downloadFullProfileButton.setVisible(true);
            downloadFullProfileButton.setManaged(true);
            roleLabel.setDisable(false);
            leftButton.setVisible(true);
            leftButton.setManaged(true);
            rightButton.setVisible(true);
            rightButton.setManaged(true);
            confirmRoleButton.setVisible(true);
            confirmRoleButton.setManaged(true);
        }
    }

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
                    map.getName(),
                    successPercent,
                    map.getScoreVal(),
                    map.getTimesPlayed(),
                    map.getTimesCompleted()
            ));
        }
    }

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

    @FXML
    private void handleBanUser(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        Popup confirmPopup = new Popup();
        confirmPopup.setAutoHide(true);

        VBox menuBox = new VBox(20);
        menuBox.getStylesheets().add(getClass().getResource("/com/gnome/gnome/pages/css/profile-delete-user.css").toExternalForm());
        menuBox.setAlignment(Pos.CENTER);
        menuBox.getStyleClass().add("menu-popup");

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

    @FXML
    private void handleUploadStats(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select User Statistics FXML");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("FXML Files", "*.fxml"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                UserStatisticsDAO userStatisticsDAO = new UserStatisticsDAO();
                UserStatistics stats = parseStatisticsFromFXML(file);
                stats.setUsername(selectedUsername);
                boolean updated = userStatisticsDAO.updateUserStatistics(stats);

                if (updated) {
                    CustomPopupUtil.showSuccess(stage, "Statistics successfully uploaded");
                    setPlayer(selectedUsername); // Refresh profile page
                } else {
                    logger.warning("Failed to update statistics for user: " + selectedUsername);
                    CustomPopupUtil.showError(stage, "Failed to update statistics in database");
                }
            } catch (Exception e) {
                logger.warning("Error uploading statistics: " + e.getMessage());
                CustomPopupUtil.showError(stage, "Error parsing FXML file: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDownloadStats(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save User Statistics FXML");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("FXML Files", "*.fxml"));
        fileChooser.setInitialFileName(selectedUsername + "_stats.fxml");
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                UserStatisticsDAO userStatisticsDAO = new UserStatisticsDAO();
                UserStatistics stats = userStatisticsDAO.getUserStatisticsByUsername(selectedUsername);
                saveStatisticsToFXML(stats, file);
                CustomPopupUtil.showSuccess(stage, "Statistics successfully downloaded");
            } catch (Exception e) {
                logger.warning("Error downloading statistics: " + e.getMessage());
                CustomPopupUtil.showError(stage, "Error saving FXML file");
            }
        }
    }

    @FXML
    private void handleDownloadFullProfile(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Full User Profile FXML");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("FXML Files", "*.fxml"));
        fileChooser.setInitialFileName(selectedUsername + "_full_profile.fxml");
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try {
                UserStatisticsDAO userStatisticsDAO = new UserStatisticsDAO();
                UserStatistics stats = userStatisticsDAO.getUserStatisticsByUsername(selectedUsername);
                UserGameStateDAO gameStateDAO = new UserGameStateDAO();
                UserGameState gameState = gameStateDAO.getUserGameStateByUsername(selectedUsername);
                saveFullProfileToFXML(stats, user, gameState, userMaps, file);
                CustomPopupUtil.showSuccess(stage, "Full profile successfully downloaded");
            } catch (Exception e) {
                logger.warning("Error downloading full profile: " + e.getMessage());
                CustomPopupUtil.showError(stage, "Error saving FXML file");
            }
        }
    }

    private UserStatistics parseStatisticsFromFXML(File file) throws Exception {
        UserStatistics stats = new UserStatistics();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();

        Element root = doc.getDocumentElement();
        Element statsElement;

        if (root.getTagName().equals("UserStatistics")) {
            statsElement = root;
        } else if (root.getTagName().equals("UserProfile")) {
            NodeList statsNodes = root.getElementsByTagName("statistics");
            if (statsNodes.getLength() == 0) {
                throw new Exception("No statistics element found in UserProfile FXML");
            }
            statsElement = (Element) statsNodes.item(0);
        } else {
            throw new Exception("Invalid root element: " + root.getTagName());
        }

        // Helper method to safely get and parse integer values
        String[] requiredFields = {
                "totalMapsPlayed", "totalWins", "totalDeaths",
                "totalMonstersKilled", "totalChestsOpened"
        };

        for (String field : requiredFields) {
            NodeList nodeList = statsElement.getElementsByTagName(field);
            if (nodeList.getLength() == 0) {
                throw new Exception("Missing required field: " + field);
            }
            String value = nodeList.item(0).getTextContent();
            if (value == null || value.trim().isEmpty()) {
                throw new Exception("Empty value for field: " + field);
            }
            try {
                int intValue = Integer.parseInt(value);
                switch (field) {
                    case "totalMapsPlayed":
                        stats.setTotalMapsPlayed(intValue);
                        break;
                    case "totalWins":
                        stats.setTotalWins(intValue);
                        break;
                    case "totalDeaths":
                        stats.setTotalDeaths(intValue);
                        break;
                    case "totalMonstersKilled":
                        stats.setTotalMonstersKilled(intValue);
                        break;
                    case "totalChestsOpened":
                        stats.setTotalChestsOpened(intValue);
                        break;
                }
            } catch (NumberFormatException e) {
                throw new Exception("Invalid integer value for field " + field + ": " + value);
            }
        }

        logger.info("Successfully parsed statistics from FXML for user: " + selectedUsername);
        return stats;
    }

    private void saveStatisticsToFXML(UserStatistics stats, File file) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        Element rootElement = doc.createElement("UserStatistics");
        doc.appendChild(rootElement);

        Element mapsPlayed = doc.createElement("totalMapsPlayed");
        mapsPlayed.appendChild(doc.createTextNode(String.valueOf(stats.getTotalMapsPlayed())));
        rootElement.appendChild(mapsPlayed);

        Element wins = doc.createElement("totalWins");
        wins.appendChild(doc.createTextNode(String.valueOf(stats.getTotalWins())));
        rootElement.appendChild(wins);

        Element deaths = doc.createElement("totalDeaths");
        deaths.appendChild(doc.createTextNode(String.valueOf(stats.getTotalDeaths())));
        rootElement.appendChild(deaths);

        Element monstersKilled = doc.createElement("totalMonstersKilled");
        monstersKilled.appendChild(doc.createTextNode(String.valueOf(stats.getTotalMonstersKilled())));
        rootElement.appendChild(monstersKilled);

        Element chestsOpened = doc.createElement("totalChestsOpened");
        chestsOpened.appendChild(doc.createTextNode(String.valueOf(stats.getTotalChestsOpened())));
        rootElement.appendChild(chestsOpened);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    private void saveFullProfileToFXML(UserStatistics stats, AuthUser user, UserGameState gameState, List<Map> maps, File file) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        Element rootElement = doc.createElement("UserProfile");
        doc.appendChild(rootElement);

        // User Information
        Element username = doc.createElement("username");
        username.appendChild(doc.createTextNode(user.getUsername()));
        rootElement.appendChild(username);

        Element role = doc.createElement("role");
        role.appendChild(doc.createTextNode(user.getRole().toString()));
        rootElement.appendChild(role);

        Element mapLevel = doc.createElement("mapLevel");
        mapLevel.appendChild(doc.createTextNode(String.valueOf(gameState != null ? gameState.getMapLevel() : 0)));
        rootElement.appendChild(mapLevel);

        // Statistics
        Element statistics = doc.createElement("statistics");
        rootElement.appendChild(statistics);

        Element mapsPlayed = doc.createElement("totalMapsPlayed");
        mapsPlayed.appendChild(doc.createTextNode(String.valueOf(stats.getTotalMapsPlayed())));
        statistics.appendChild(mapsPlayed);

        Element wins = doc.createElement("totalWins");
        wins.appendChild(doc.createTextNode(String.valueOf(stats.getTotalWins())));
        statistics.appendChild(wins);

        Element deaths = doc.createElement("totalDeaths");
        deaths.appendChild(doc.createTextNode(String.valueOf(stats.getTotalDeaths())));
        statistics.appendChild(deaths);

        Element monstersKilled = doc.createElement("totalMonstersKilled");
        monstersKilled.appendChild(doc.createTextNode(String.valueOf(stats.getTotalMonstersKilled())));
        statistics.appendChild(monstersKilled);

        Element chestsOpened = doc.createElement("totalChestsOpened");
        chestsOpened.appendChild(doc.createTextNode(String.valueOf(stats.getTotalChestsOpened())));
        statistics.appendChild(chestsOpened);

        // Created Maps
        Element createdMaps = doc.createElement("createdMaps");
        rootElement.appendChild(createdMaps);

        for (Map map : maps) {
            Element mapElement = doc.createElement("map");
            createdMaps.appendChild(mapElement);

            Element mapName = doc.createElement("mapName");
            mapName.appendChild(doc.createTextNode(map.getMapNameEng()));
            mapElement.appendChild(mapName);

            Element timesPlayed = doc.createElement("timesPlayed");
            timesPlayed.appendChild(doc.createTextNode(String.valueOf(map.getTimesPlayed())));
            mapElement.appendChild(timesPlayed);

            Element timesCompleted = doc.createElement("timesCompleted");
            timesCompleted.appendChild(doc.createTextNode(String.valueOf(map.getTimesCompleted())));
            mapElement.appendChild(timesCompleted);

            Element scoreVal = doc.createElement("scoreVal");
            scoreVal.appendChild(doc.createTextNode(String.valueOf(map.getScoreVal())));
            mapElement.appendChild(scoreVal);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    @FXML
    private void handleLeftRole() {
        currentRoleIndex = (currentRoleIndex > 0) ? currentRoleIndex - 1 : roles.size() - 1;
        roleLabel.setText("Role: " + roles.get(currentRoleIndex));
    }

    @FXML
    private void handleRightRole() {
        currentRoleIndex = (currentRoleIndex < roles.size() - 1) ? currentRoleIndex + 1 : 0;
        roleLabel.setText("Role: " + roles.get(currentRoleIndex));
    }

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
                    selectedRole.toString(),
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