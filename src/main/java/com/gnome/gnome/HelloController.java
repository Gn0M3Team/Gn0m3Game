package com.gnome.gnome;

import com.gnome.gnome.components.LeaderBoardView;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Objects;

public class HelloController {

    @FXML private ImageView musicIcon;
    @FXML private BorderPane rootPane;
    private LeaderBoardView leaderboard;

    @FXML
    public void initialize() {
        musicIcon.setImage(
                new Image(
                        Objects.requireNonNull(
                                getClass().getResourceAsStream("/com/gnome/gnome/images/musicicon.png")
                        )
                )
        );
    }

    @FXML
    protected void onEditorButtonClick(ActionEvent event) throws IOException {
        Parent editorRoot = FXMLLoader.
                load(Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/editor-view.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(editorRoot);
    }
    @FXML
    protected void onRegistrationButtonClick(ActionEvent event) throws IOException {
        Parent editorRoot = FXMLLoader.
                load(Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/switchingPage/switcher-page.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(editorRoot);
    }

    @FXML
    public void onContinueGameButtonClick(ActionEvent event) {
    }

    @FXML
    public void onSettingsButtonClick(ActionEvent event) {
    }

    @FXML
    public void onLeaderBoardButtonClick(ActionEvent event) {
        this.leaderboard = new LeaderBoardView(() -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(500), this.leaderboard);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> rootPane.setLeft(null));
            fadeOut.play();
        });

        this.leaderboard.setOpacity(0.0);

        rootPane.setLeft(this.leaderboard);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), this.leaderboard);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    @FXML
    public void onExitButtonClick(ActionEvent event) {
        Platform.exit();
    }
}