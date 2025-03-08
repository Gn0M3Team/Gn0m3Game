package com.gnome.gnome;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.Data;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}