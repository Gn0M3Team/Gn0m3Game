package com.gnome.gnome.loginRegistretion;

import com.gnome.gnome.loginRegistretion.service.LoginRegistretionService;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

public class LoginRegistrationController {

    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Button loginButton;
    @FXML private Label loginMessage;


    @FXML private TextField regUsername;
    @FXML private PasswordField regPassword;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button registerButton;
    @FXML private Label registerMessage;


    @FXML
    public BorderPane loginRegistretion;
    private PageSwitcherInterface pageSwitch;



    @FXML
    private void initialize() {
        pageSwitch = new SwitchPage();
    }

    @FXML
    private void handleLogin() {
        LoginRegistretionService.loginOrRegisterUser();
        pageSwitch.goHello(loginRegistretion);
    }

    @FXML
    private void handleRegister() {
        pageSwitch.goHello(loginRegistretion);

    }

}