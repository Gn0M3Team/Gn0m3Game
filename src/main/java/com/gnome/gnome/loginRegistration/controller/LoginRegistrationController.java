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
import com.gnome.gnome.utils.CustomPopupUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

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

    @FXML private TextField registerUsername;
    @FXML private PasswordField registerPassword;
    @FXML private PasswordField registerConfirmPassword;
    @FXML private Label registerMessage;

    @FXML private RadioButton loginRadio;
    @FXML private RadioButton registerRadio;
    @FXML private VBox loginPane;
    @FXML private VBox registerPane;

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
        showLoginPane();
    }

    /**
     * Switches the UI to display the login pane.
     * Triggered when the "Login" radio button is selected.
     */
    @FXML
    private void switchToLogin() {
        showLoginPane();
    }

    /**
     * Switches the UI to display the registration pane.
     * Triggered when the "Register" radio button is selected.
     */
    @FXML
    private void switchToRegister() {
        showRegisterPane();
    }
    /**
     * Makes the login pane visible and hides the registration pane.
     * Also ensures only the visible pane is managed by the layout.
     */
    private void showLoginPane() {
        loginPane.setVisible(true);
        loginPane.setManaged(true);
        registerPane.setVisible(false);
        registerPane.setManaged(false);
    }

    /**
     * Makes the registration pane visible and hides the login pane.
     * Also ensures only the visible pane is managed by the layout.
     */
    private void showRegisterPane() {
        registerPane.setVisible(true);
        registerPane.setManaged(true);
        loginPane.setVisible(false);
        loginPane.setManaged(false);
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

        try {
            LoginResult result = LoginRegistrationService.loginUser(username, password);
            if (result.getUser() != null) {
                userUploaded(result, username);
            } else {
                loginMessage.setText(result.getMessage());
            }
        } catch (Exception e) {
            Stage stage = (Stage) loginUsername.getScene().getWindow();
            CustomPopupUtil.showError(stage, "Check your internet connection");
        }

    }
    /**
     * Handles the user registration process triggered by the registration button.
     *
     * Validates the input fields (username, password, and confirm password),
     * checks for empty fields, password confirmation match, and username length.
     * If validation passes, it attempts to register the user via the {@link LoginRegistrationService}.
     * On successful registration, it pre-fills the login fields for convenience.
     *
     * @param event the action event triggered by the register button
     */
    @FXML
    public void handleRegister(ActionEvent event){
        String username = registerUsername.getText();
        String password = registerPassword.getText();
        String confirmPassword = registerConfirmPassword.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            registerMessage.setText("Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            registerMessage.setText("Passwords do not match.");
            return;
        }

        if (username.length() > 32) {
            registerMessage.setText("Username must be less than 32 characters.");
            return;
        }

        try {
            LoginResult result = LoginRegistrationService.registerUser(username, password);

            if (result.getUser() != null) {
                userUploaded(result, username);
            } else {
                registerMessage.setText(result.getMessage());
            }
        } catch (Exception e) {
            Stage stage = (Stage) loginUsername.getScene().getWindow();
            CustomPopupUtil.showError(stage, "Check your internet connection");
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

    public void userUploaded(LoginResult result, String username) {
        UserSession.getInstance().setCurrentUser(result.getUser());

        AuthUserDAO authUserDAO = new AuthUserDAO();
        UserGameStateDAO userGameStateDAO = new UserGameStateDAO();
        UserStatisticsDAO userStatisticsDAO = new UserStatisticsDAO();

        UserGameState userGameState = userGameStateDAO.getUserGameStateByUsername(username);
        AuthUser authUser = authUserDAO.getAuthUserByUsername(username);
        UserStatistics userStatistics = userStatisticsDAO.getUserStatisticsByUsername(username);

        if (authUser != null && userGameState != null && userStatistics != null) {
            UserState.init(authUser, userGameState, userStatistics);
        }

        pageSwitch.goMainMenu(loginRegistretion);
    }

}