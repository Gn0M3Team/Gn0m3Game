package com.gnome.gnome;

import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import com.gnome.gnome.components.LeaderBoardView;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class HelloController {

    @FXML private ImageView musicIcon;
    @FXML
    private BorderPane helloPage;
    private PageSwitcherInterface pageSwitch;
    private LeaderBoardView leaderboard;

    /**
     * Initializes the controller and sets the music icon image.
     */
    @FXML
    public void initialize() {
        pageSwitch=new SwitchPage();
        musicIcon.setImage(
                new Image(
                        Objects.requireNonNull(
                                getClass().getResourceAsStream("/com/gnome/gnome/images/musicicon.png")
                        )
                )
        );
    }

    /**
     * Navigates to the editor page when "Create a map" is clicked.
     */
    @FXML
    protected void onEditorButtonClick(ActionEvent event) throws IOException {
        Parent editorRoot = FXMLLoader.
                load(Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/editor-view.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(editorRoot);
    }

    /**
     * Navigates to the registration/switcher page.
     */
    @FXML
    protected void onSwitcherButtonClick(ActionEvent event) throws IOException {
        pageSwitch.goSwitch(helloPage);
    }

    /**
     * Placeholder for handling continue game logic (to be implemented).
     */
    @FXML
    public void onContinueGameButtonClick(ActionEvent event) throws IOException {
        Parent continueGameRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/continueGame.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/css/continueGame.css")).toExternalForm()
        );
        stage.getScene().setRoot(continueGameRoot);
    }

    /**
     * Placeholder for settings logic (to be implemented).
     */
    @FXML
    public void onSettingsButtonClick(ActionEvent event) {
    }

    /**
     * Opens the leaderboard panel on the left side of the screen with fade-in animation.
     * Clicking the "X" inside the panel triggers fade-out and removes the panel.
     */
    @FXML
    public void onLeaderBoardButtonClick(ActionEvent event) {
        this.leaderboard = new LeaderBoardView(() -> {
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
     * Closes the application.
     */
    @FXML
    public void onExitButtonClick(ActionEvent event) {
        Platform.exit();
    }
}