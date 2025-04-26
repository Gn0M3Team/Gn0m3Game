package com.gnome.gnome.profile;

import com.gnome.gnome.dao.userDAO.AuthUserDAO;
import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.dao.userDAO.UserGameStateDAO;
import com.gnome.gnome.dao.userDAO.UserSession;
import com.gnome.gnome.models.Map;
import com.gnome.gnome.models.user.AuthUser;
import com.gnome.gnome.models.user.UserGameState;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.event.ActionEvent;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ProfileController {

    private static final Logger logger = Logger.getLogger(ProfileController.class.getName());

    @FXML private Label nameLabel;
    @FXML private Label recordLabel;
    @FXML private Label roleLabel;
    @FXML private Label gamesPlayedLabel;
    @FXML private Label deathCounter;

    @FXML
    private Button banUserButton;
    @FXML private Button leftButton;
    @FXML private Button rightButton;
    @FXML private Button confirmRoleButton;


    @FXML private ListView<String> mapListView;
    @FXML
    private BorderPane profilePage;
    private PageSwitcherInterface pageSwitch;
    private final AuthUserDAO userDAO = new AuthUserDAO();
    private final MapDAO MapDAO = new MapDAO();


    private int currentMapPage = 1;
    private final int mapPageSize = 5;
    private boolean mapLoading = false;
    private List<Map> userMaps;

    private String selectedPlayer;
    private AuthUser user;
    private final List<String> roles = List.of("user", "map_creator");
    private int currentRoleIndex = 0;

    @FXML
    public void initialize() {
        pageSwitch = new SwitchPage();
    }

    /**
     * Initializes the profile page with data for the selected player.
     * This method is called when navigating from the leaderboard.
     */
    public void setPlayer(String playerData) {
        this.selectedPlayer = playerData.split("-")[0];
        logger.info("Loading profile for: " + playerData);
        user = userDAO.getAuthUserByUsername(playerData);
        userMaps = MapDAO.getMapsByUsername(playerData);

        UserGameStateDAO GameState = new UserGameStateDAO();

        UserGameState gameState=GameState.getUserGameStateByUsername(selectedPlayer);

        nameLabel.setText("Profile of " + user.getUsername());
        recordLabel.setText("Score: " +gameState.getScore());

        gamesPlayedLabel.setText("MapLevel: "+gameState.getMapLevel());
        deathCounter.setText("Death counter: "+gameState.getDeathCounter());

        String userRole = user != null ? user.getRole() : "user";
        currentRoleIndex = roles.indexOf(userRole);
        if (currentRoleIndex == -1) currentRoleIndex = 0;
        roleLabel.setText("Role: "+roles.get(currentRoleIndex));


        user_test();

        mapListView.getItems().clear();
        if (userMaps.isEmpty()) {
            mapListView.setPlaceholder(new Label("No maps available"));
        } else {
            loadMoreMaps();
            setupScrollPagination(mapListView, this::loadMoreMaps, () -> mapLoading);
        }
//        if (userMaps.isEmpty()) {
//            mapListView.setPlaceholder(new Label("No maps available"));
//        } else {
//            for (Map map : userMaps) {
//                mapListView.getItems().add(map.toString());
//            }
//        }


    }
    /**
     * Adjusts the visibility of buttons depending on the current user's permissions.
     */
    private void user_test(){
        AuthUser authUser=UserSession.getInstance().getCurrentUser();
        if (authUser.getRole().equals("user")||authUser.getRole().equals("map_creator")){
            banUserButton.setVisible(false);
            roleLabel.setDisable(true);

            leftButton.setVisible(false);
            rightButton.setVisible(false);
            confirmRoleButton.setVisible(false);
        }else{
            banUserButton.setVisible(true);
            roleLabel.setDisable(false);

            leftButton.setVisible(true);
            rightButton.setVisible(true);
            confirmRoleButton.setVisible(true);
        }
        if (selectedPlayer.equals("Admin")&&authUser.getRole().equals("admin")){
            banUserButton.setVisible(false);
            roleLabel.setVisible(false);

            leftButton.setVisible(false);
            rightButton.setVisible(false);
            confirmRoleButton.setVisible(false);
        }
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
     * Loads the next batch of maps into the ListView.
     */
    private void loadMoreMaps() {
        if (userMaps == null || userMaps.isEmpty()) return;

        mapLoading = true;
        int start = (currentMapPage - 1) * mapPageSize;
        int end = Math.min(start + mapPageSize, userMaps.size());

        for (int i = start; i < end; i++) {
            mapListView.getItems().add("Map "+i+": Score Value "+userMaps.get(i).getScoreVal()+" "+
                    userMaps.get(i).getMapNameSk()+" "+
                    userMaps.get(i).getMapNameEng()+" "+
                    userMaps.get(i).getLevel());
        }

        currentMapPage++;

        if (end >= userMaps.size()) {
            mapLoading = true;
        } else {
            mapLoading = false;
        }
    }

    /**
     * Handles the "Back" button click to return to the main menu (hello-view.fxml).
     * Adds a fade transition effect during scene switch.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        pageSwitch.goMainMenu(profilePage);
    }

    /**
     * Handles the ban/delete user button click.
     * Shows confirmation popup before deleting the user.
     */
    @FXML
    private void handleBanUser(ActionEvent event) {
        Popup confirmPopup = new Popup();
        confirmPopup.setAutoHide(true);

        VBox menuBox = new VBox(20);
        menuBox.getStylesheets().add(getClass().getResource("/com/gnome/gnome/pages/css/new-game.css").toExternalForm());
        menuBox.setAlignment(Pos.CENTER);
        menuBox.getStyleClass().add("menu-popup");
        menuBox.setStyle("-fx-background-color: #C0C0C0; -fx-padding: 20; -fx-background-radius: 20;");

        Label title = new Label("Confirm Deletion");
        title.getStyleClass().add("popup-title");

        Label userLabel = new Label("Delete user: " + selectedPlayer + "?");
        userLabel.getStyleClass().add("popup-title");

        Button yesButton = new Button("Yes, Delete");
        yesButton.getStyleClass().add("menu-button");
        Button noButton = new Button("Cancel");
        noButton.getStyleClass().add("menu-button");

        yesButton.setOnAction(e -> {
            boolean deleted = userDAO.deleteUserByUsername(selectedPlayer);
            confirmPopup.hide();
            if (deleted) {
                logger.info("User deleted: " + selectedPlayer);
                pageSwitch.goMainMenu(profilePage);
            } else {
                logger.warning("Failed to delete user: " + selectedPlayer);
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
        if (currentRoleIndex > 0) {
            currentRoleIndex--;
        } else {
            currentRoleIndex = roles.size() - 1;
        }
        roleLabel.setText("Role: "+roles.get(currentRoleIndex));
    }

    /**
     * Moves to the next role in the role list.
     */
    @FXML
    private void handleRightRole() {
        if (currentRoleIndex < roles.size() - 1) {
            currentRoleIndex++;
        } else {
            currentRoleIndex = 0;
        }
        roleLabel.setText("Role: "+roles.get(currentRoleIndex));
    }
    /**
     * Confirms and updates the selected role for the user.
     */
    @FXML
    private void handleConfirmRole(ActionEvent event) {
        String selectedRole = roles.get(currentRoleIndex);
        logger.info("Role updated to " + selectedRole + " for user " + selectedPlayer);
        logger.info(selectedPlayer+" "+selectedRole);
        this.user.setRole(selectedRole);
        userDAO.updateUserRole(this.user);
        logger.info("Role updated to " + selectedRole + " for user " + selectedPlayer);
    }

}