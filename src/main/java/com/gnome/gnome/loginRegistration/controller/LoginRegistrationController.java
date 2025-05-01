package com.gnome.gnome.loginRegistration.controller;

import com.gnome.gnome.dao.UserStatisticsDAO;
import com.gnome.gnome.dao.userDAO.AuthUserDAO;
import com.gnome.gnome.dao.userDAO.UserGameStateDAO;
import com.gnome.gnome.dao.userDAO.UserSession;
import com.gnome.gnome.models.UserStatistics;
import com.gnome.gnome.userState.UserState;
import com.gnome.gnome.loginRegistration.service.LoginRegistrationService;
import com.gnome.gnome.loginRegistration.service.LoginResult;
import com.gnome.gnome.models.user.AuthUser;
import com.gnome.gnome.models.user.UserGameState;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

/**
 * Controller class for the Login and Registration view.
 *
 * Handles user input for logging in or registering a new user,
 * provides feedback messages, and transitions to the main menu upon success.
 */
public class LoginRegistrationController {

    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Label loginMessage;

    @FXML
    public BorderPane loginRegistretion;
    private PageSwitcherInterface pageSwitch;
    /**
     * Initializes the controller.
     * Called automatically after the FXML file is loaded.
     * Sets up the page switcher instance.
     */
    @FXML
    private void initialize() {
        pageSwitch = new SwitchPage();
    }

    /**
     * Handles the login or registration action.
     * Validates the input, attempts to log in or register the user via {@link LoginRegistrationService},
     * and navigates to the main menu upon success. Displays appropriate error messages otherwise.
     */
    @FXML
    private void handleLogin() {
        String username = loginUsername.getText();
        String password = loginPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            loginMessage.setText("Fill in all fields!");
            return;
        }
        if (username.length() > 32) {
            loginMessage.setText("The name must be less than 32 characters!");
            return;
        }


        LoginResult result = LoginRegistrationService.loginOrRegisterUser(username, password);

        if (result.getUser() != null) {
            UserSession.getInstance().setCurrentUser(result.getUser());

            AuthUserDAO authUserDAO = new AuthUserDAO();
            UserGameStateDAO userGameStateDAO = new UserGameStateDAO();
            UserStatisticsDAO userStatisticsDAO = new UserStatisticsDAO();

            UserGameState userGameState = userGameStateDAO.getUserGameStateByUsername(username);
            AuthUser authUser = authUserDAO.getAuthUserByUsername(username);
            UserStatistics userStatistics = userStatisticsDAO.getUserStatisticsByUsername(username);

            if (authUser != null && userGameState != null) {
                UserState.init(authUser, userGameState, userStatistics);
            }

            pageSwitch.goMainMenu(loginRegistretion);
        } else {
            loginMessage.setText(result.getMessage());
        }
    }

    /**
     * Handles the action of exiting the application.
     *
     * @param event the action event triggered by the exit button
     */
    @FXML
    public void onExitButtonClick(ActionEvent event) {
        Platform.exit();
    }

}