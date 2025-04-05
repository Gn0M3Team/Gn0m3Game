package com.gnome.gnome.loginRegistretion;

import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.util.HashMap;
import java.util.Map;

public class LoginRegistrationController {

    // Login components
    @FXML private TextField loginUsername;
    @FXML private PasswordField loginPassword;
    @FXML private Button loginButton;
    @FXML private Label loginMessage;

    // Registration components
    @FXML private TextField regUsername;
    @FXML private PasswordField regPassword;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Button registerButton;
    @FXML private Label registerMessage;


    @FXML
    public AnchorPane loginRegistretion;
    private PageSwitcherInterface pageSwitch;
    // Зберігання користувачів (тимчасово в пам'яті, в реальному додатку використовуйте базу даних)
    private Map<String, User> users = new HashMap<>();

    // Ініціалізація
    @FXML
    private void initialize() {
        pageSwitch = new SwitchPage();
    }

    @FXML
    private void handleLogin() {
//        String username = loginUsername.getText().trim();
//        String password = loginPassword.getText();
//
//        if (username.isEmpty() || password.isEmpty()) {
//            loginMessage.setText("Please fill all fields");
//            return;
//        }
//
//        User user = users.get(username);
//        if (user != null && user.getPassword().equals(password)) {
//            loginMessage.setText("Login successful! Role: " + user.getRole());
//            // Тут можна додати перехід на головну сторінку
//        } else {
//            loginMessage.setText("Invalid username or password");
//        }


//        pageSwitch.goHello(loginRegistretion);
    }

    @FXML
    private void handleRegister() {
        String username = regUsername.getText().trim();
        String password = regPassword.getText();
        String role = roleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            registerMessage.setText("Please fill all fields");
            return;
        }

        if (users.containsKey(username)) {
            registerMessage.setText("Username already exists");
            return;
        }

        User newUser = new User(username, password, role);
        users.put(username, newUser);
        registerMessage.setText("Registration successful!");

        // Очистка полів
        regUsername.clear();
        regPassword.clear();
        roleComboBox.getSelectionModel().clearSelection();
    }

    // Клас для зберігання даних користувача
    private static class User {
        private final String username;
        private final String password;
        private final String role;

        public User(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }

        public String getPassword() {
            return password;
        }

        public String getRole() {
            return role;
        }
    }
}