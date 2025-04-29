package com.gnome.gnome;

import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.dao.MonsterDAO;
import com.gnome.gnome.dao.userDAO.AuthUserDAO;
import com.gnome.gnome.game.MapLoaderService;
import com.gnome.gnome.game.MapLoaderUIHandler;
import com.gnome.gnome.models.Map;
import com.gnome.gnome.models.user.AuthUser;
import com.gnome.gnome.music.MusicWizard;
import com.gnome.gnome.dao.userDAO.UserSession;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import com.gnome.gnome.components.LeaderBoardView;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Objects;

// TODO: If we have time, we need to change logic with creating DAO, because we in two places crete monster dao, map dao, and for me we need to change this.
public class HelloController {

    @FXML private ImageView musicIcon;
    @FXML
    private Button musicButton;
    @FXML
    private BorderPane helloPage;
    @FXML
    private Button editorButton;
    @FXML
    private Button continueGameButton;
    @FXML
    private Label nicknameLabel;

    private PageSwitcherInterface pageSwitch;
    private LeaderBoardView leaderboard;
    private final AuthUserDAO userDAO = new AuthUserDAO();

    Stage primaryStage;

    MapLoaderService mapLoaderService;


    /**
     * Initializes the controller and sets the music icon for the button.
     * It also runs user tests and sets up the music button with an icon.
     */
    @FXML
    public void initialize() {
//        pageSwitch=new SwitchPage();
//
//        musicButton = new Button();
//
//        musicIcon.setImage(
//                new Image(
//                        Objects.requireNonNull(
//                                getClass().getResourceAsStream("/com/gnome/gnome/images/musicicon.png")
//                        )
//                )
//        );
//
//        User_test_for_creation();
//        user_test();
//
//
//        Platform.runLater(() -> {
//            primaryStage = (Stage) .getScene().getWindow();
//            mapLoader = new MapLoader(primaryStage);
//        });
        pageSwitch = new SwitchPage();

        // НЕ СОЗДАЕМ НОВУЮ КНОПКУ!!! Используем ту, что пришла из FXML
        // musicButton = new Button(); ❌ Убрать эту строку!

        musicIcon.setImage(
                new Image(
                        Objects.requireNonNull(
                                getClass().getResourceAsStream("/com/gnome/gnome/images/musicicon.png")
                        )
                )
        );

        User_test_for_creation();
        user_test();

        musicButton.setGraphic(musicIcon);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;

        MapDAO mapDAO = new MapDAO();
        MonsterDAO monsterDAO = new MonsterDAO();

        this.mapLoaderService = new MapLoaderService(mapDAO, monsterDAO);

        continueGameButton.setOnAction(e -> {
            if (mapLoaderService != null) {
                Map selectedMap = mapDAO.getMapByLevel(1);
                new MapLoaderUIHandler(mapLoaderService, primaryStage).showStartMap(selectedMap.getId());
            }
        });
    }

    /**
     * Checks if there is a current user. If not, sets a default user (Admin).
     */
    private void User_test_for_creation(){
        System.out.println("Current User:" + UserSession.getInstance().getCurrentUser());
        if (UserSession.getInstance().getCurrentUser()==null){
            AuthUser admin=userDAO.getAuthUserByUsername("Admin");
            UserSession.getInstance().setCurrentUser(admin);
            System.out.println("Current User:" + UserSession.getInstance().getCurrentUser());
        }
    }
    /**
     * Displays the current user's username and sets visibility of the editor button based on user role.
     */
    private void user_test(){
        AuthUser authUser=UserSession.getInstance().getCurrentUser();
        nicknameLabel.setText("User: "+authUser.getUsername());
        if (authUser.getRole().equals("user")){
            editorButton.setVisible(false);
        }
    }


    /**
     * Navigates to the editor page when "Create a map" is clicked.
     */
    @FXML
    protected void onEditorButtonClick(ActionEvent event){
        pageSwitch.goEditor(helloPage);
    }

    /**
     * Navigates to the registration/switcher page.
     */
    @FXML
    protected void onNewGameButtonClick(ActionEvent event){
        pageSwitch.goNewGame(helloPage);
    }

    /**
     * Placeholder for handling continue game logic (to be implemented).
     */
    @FXML
    public void onContinueGameButtonClick(ActionEvent event) {
        if (mapLoaderService != null) {
            MapDAO mapDAO = new MapDAO();
            Map selectedMap = mapDAO.getMapByLevel(1);

            new MapLoaderUIHandler(mapLoaderService, primaryStage)
                    .showStartMap(selectedMap.getId());
        }
    }

    /**
     * Placeholder for settings logic (to be implemented).
     */
    @FXML
    public void onSettingsButtonClick(ActionEvent event) {
        pageSwitch.goSetting(helloPage);
    }

    /**
     * Returns the main layout container (BorderPane) of the Hello page.
     *
     * @return the BorderPane representing the Hello page UI.
     */
    @FXML
    public BorderPane getHelloPage() {
        return helloPage;
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
            fadeOut.setOnFinished(e -> helloPage.setLeft(null));
            fadeOut.play();
        });

        this.leaderboard.setOpacity(0.0);

        helloPage.setLeft(this.leaderboard);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this.leaderboard);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    /**
     * Starts and ends music by fading
     */
    @FXML
    public void onMusicIconClick(ActionEvent event) {
        //System.out.println("Music");
        if (!MusicWizard.musicRunning){
            MusicWizard.start_music_loop();
            MusicWizard.start_ambient();
        }

        else{
            MusicWizard.stop = true;
            MusicWizard.stop_ambient();
        }
    }

    /**
     * Closes the application.
     */
    @FXML
    public void onExitButtonClick(ActionEvent event) {
        MusicWizard.stop = true;
//        Platform.exit();
        pageSwitch.goLogin(helloPage);
    }
}