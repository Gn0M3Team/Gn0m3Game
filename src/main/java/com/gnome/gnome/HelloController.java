package com.gnome.gnome;

import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
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

import java.io.IOException;
import java.util.Objects;

public class HelloController {

    @FXML private ImageView musicIcon;
    @FXML
    private BorderPane helloPage;
    private PageSwitcherInterface pageSwitch;


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

    @FXML
    protected void onEditorButtonClick(ActionEvent event) throws IOException {
        Parent editorRoot = FXMLLoader.
                load(Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/editor-view.fxml")));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(editorRoot);
    }
    @FXML
    protected void onSwitcherButtonClick(ActionEvent event) throws IOException {
        pageSwitch.goSwitch(helloPage);
    }

    @FXML
    public void onContinueGameButtonClick(ActionEvent event) {
    }

    @FXML
    public void onSettingsButtonClick(ActionEvent event) {
    }

    @FXML
    public void onExitButtonClick(ActionEvent event) {
        Platform.exit();
    }
}