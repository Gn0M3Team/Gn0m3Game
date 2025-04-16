package com.gnome.gnome.loginRegistration;

import com.gnome.gnome.loginRegistration.service.LoginRegistrationService;
import com.gnome.gnome.loginRegistration.service.LoginResult;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
public class LoginRegistrationController {

    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Label loginMessage;

    @FXML
    public BorderPane loginRegistretion;
    private PageSwitcherInterface pageSwitch;

    @FXML
    private void initialize() {
        pageSwitch = new SwitchPage();
    }

    @FXML
    private void handleLogin() {
        String username = loginUsername.getText();
        String password = loginPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            loginMessage.setText("Fill in all fields!");
            return;
        }
        if (username.length()>32) {
            loginMessage.setText("The name must be less than 32 characters!");
            return;
        }


        LoginResult result = LoginRegistrationService.loginOrRegisterUser(username,password);

        if (result.getUser() != null) {
            pageSwitch.mainMenu(loginRegistretion);
        } else {
            loginMessage.setText(result.getMessage());
        }
    }
}