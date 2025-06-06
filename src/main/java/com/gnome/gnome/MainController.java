package com.gnome.gnome;

import com.gnome.gnome.dao.*;
import com.gnome.gnome.dao.userDAO.AuthUserDAO;
import com.gnome.gnome.game.MapLoaderService;
import com.gnome.gnome.game.MapLoaderUIHandler;
import com.gnome.gnome.models.user.PlayerRole;
import com.gnome.gnome.userState.UserState;
import com.gnome.gnome.models.Map;
import com.gnome.gnome.models.user.AuthUser;
import com.gnome.gnome.music.MusicWizard;
import com.gnome.gnome.dao.userDAO.UserSession;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import com.gnome.gnome.components.LeaderBoardView;
import com.gnome.gnome.utils.CongratsPopUp;
import com.gnome.gnome.utils.CustomPopupUtil;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

import static com.gnome.gnome.game.component.ObjectsConstants.COIN_IMAGE;

public class MainController {
// TODO: If we have time, we need to change logic with creating DAO, because we in two places crete monster dao, map dao, and for me we need to change this.

    @FXML private Button newGameButton;
    @FXML private Label menuLabel;
    @FXML private Button LeaderBoardButton;
    @FXML private ImageView coinIcon;
    @FXML
    private Label coinLabel;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private Button editorButton;
    @FXML
    private Button continueGameButton;
    @FXML
    private Button inventoryButton;
    @FXML
    private Label nicknameLabel;

    private PageSwitcherInterface pageSwitch;
    private LeaderBoardView leaderboard;
    private final AuthUserDAO userDAO = new AuthUserDAO();
    private final UserState userState = UserState.getInstance();
    private ResourceBundle bundle;
    private Stage primaryStage;
    private MapLoaderService mapLoaderService;

    public MainController() {
        if (MainApplication.getLang() == 'S'){
            this.bundle = ResourceBundle.getBundle("slovak");
        }
        else {
            this.bundle = ResourceBundle.getBundle("english");
        }
    }

    /**
     * Initializes the controller and sets the music icon for the button.
     * It also runs user tests and sets up the music button with an icon.
     */
    @FXML
    public void initialize() {
        pageSwitch = new SwitchPage();

        coinIcon.setImage(
                new Image(
                        Objects.requireNonNull(
                                getClass().getResourceAsStream("/com/gnome/gnome/images/" + COIN_IMAGE)
                        )
                )
        );

        User_test_for_creation();
//        checkUserRole();

        coinLabel.setText(String.valueOf(userState.getBalance()));

        User_test_for_creation();
//        checkUserRole();

//        musicButton.setGraphic(musicIcon);

        Platform.runLater(() -> {
            primaryStage = (Stage) continueGameButton.getScene().getWindow();
            this.setPrimaryStage(primaryStage);
        });

        nicknameLabel.setText(userState.getUsername() + ": " + userState.getRole());
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;

        MapDAO mapDAO = new MapDAO();
        MonsterDAO monsterDAO = new MonsterDAO();
        ArmorDAO armorDAO = new ArmorDAO();
        WeaponDAO weaponDAO = new WeaponDAO();
        PotionDAO potionDAO = new PotionDAO();

        this.mapLoaderService = new MapLoaderService(monsterDAO, armorDAO, weaponDAO, potionDAO);

        continueGameButton.setOnAction(e -> {
            if (mapLoaderService != null) {
                Map selectedMap = mapDAO.getMapByLevel(UserState.getInstance().getMapLevel());
                if (selectedMap != null)
                    new MapLoaderUIHandler(mapLoaderService, primaryStage).showStartMap(selectedMap);
                else
                    CongratsPopUp.showSuccess(primaryStage, bundle.getString("continue.game.alert"));
            }
        });
    }

    /**
     * Checks if there is a current user. If not, sets a default user (Admin).
     */
    private void User_test_for_creation(){
        System.out.println("Current User:" + UserSession.getInstance().getCurrentUser());
        if (UserSession.getInstance().getCurrentUser() == null){
            AuthUser admin = userDAO.getAuthUserByUsername("Admin");
            UserSession.getInstance().setCurrentUser(admin);
            System.out.println("Current User:" + UserSession.getInstance().getCurrentUser());
        }
    }
    /**
     * Displays the current user's username and sets visibility of the editor button based on user role.
     */
    private void checkUserRole() {
        if (userState.getRole() != null) {
            if (userState.getRole() == PlayerRole.USER) {
                editorButton.setVisible(false);
                editorButton.setManaged(false);
            }
        }
    }


    /**
     * Navigates to the editor page when "Create a map" is clicked.
     */
    @FXML
    protected void onEditorButtonClick(ActionEvent event){
        pageSwitch.goEditor(mainBorderPane);
    }

    @FXML
    protected void onInventoryButtonClick(ActionEvent event){
        pageSwitch.goInventory(mainBorderPane);
    }
    /**
     * Navigates to the registration/switcher page.
     */
    @FXML
    protected void onNewGameButtonClick(ActionEvent event){
        pageSwitch.goNewGame(mainBorderPane);
    }

    /**
     * Placeholder for settings logic (to be implemented).
     */
    @FXML
    public void onSettingsButtonClick(ActionEvent event) {
        pageSwitch.goSetting(mainBorderPane);
    }

    /**
     * Returns the main layout container (BorderPane) of the Hello page.
     *
     * @return the BorderPane representing the Hello page UI.
     */
    @FXML
    public BorderPane getMainBorderPane() {
        return mainBorderPane;
    }

    /**
     * Opens the leaderboard panel on the left side of the screen with fade-in animation.
     * Clicking the "X" inside the panel triggers fade-out and removes the panel.
     */
    @FXML
    public void onLeaderBoardButtonClick(ActionEvent event) {
        this.leaderboard = new LeaderBoardView(this,() -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), this.leaderboard);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> mainBorderPane.setLeft(null));
            fadeOut.play();
        });

        this.leaderboard.setOpacity(0.0);

        mainBorderPane.setLeft(this.leaderboard);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this.leaderboard);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    /**
     * Closes the application.
     */
    @FXML
    public void onExitButtonClick(ActionEvent event) {
        pageSwitch.goLogin(mainBorderPane);
    }
}