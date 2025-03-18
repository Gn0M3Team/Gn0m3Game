package com.gnome.gnome;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class HelloController {
    @FXML
    public Label welcomeText;

    @FXML
    public void initialize() {
        welcomeText.setText("Welcome to Main Page");
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
}