package com.gnome.gnome.loginRegistration.service;

import com.gnome.gnome.dao.UserStatisticsDAO;
import com.gnome.gnome.dao.userDAO.AuthUserDAO;
import com.gnome.gnome.dao.userDAO.PasswordUtils;
import com.gnome.gnome.dao.userDAO.UserGameStateDAO;
import com.gnome.gnome.models.UserStatistics;
import com.gnome.gnome.models.user.AuthUser;
import com.gnome.gnome.models.user.PlayerRole;
import com.gnome.gnome.models.user.UserGameState;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Service class responsible for handling user login and registration logic.
 * <p>
 * This class provides functionality to:
 * <ul>
 *   <li>Validate passwords against predefined security requirements</li>
 *   <li>Authenticate existing users</li>
 *   <li>Automatically register a new user if the username does not exist</li>
 * </ul>
 * <p>
 * Passwords are validated for security strength and hashed before storing
 * using the BCrypt algorithm to ensure secure authentication.
 *
 * <p><strong>Password requirements:</strong>
 * <ul>
 *   <li>Minimum length: 12 characters</li>
 *   <li>At least one uppercase letter</li>
 *   <li>At least one digit</li>
 *   <li>At least one special character</li>
 * </ul>
 *
 * This class communicates with the database through the {@link AuthUserDAO} class.
 */
public class LoginRegistrationService {

    private static final AuthUserDAO authUserDAO = new AuthUserDAO();
    private static final UserStatisticsDAO userStatisticsDAO = new UserStatisticsDAO();
    /**
     *length ≥ 12, at least 1 capital letter, 1 number, 1 special character
     */
    private static final String PASSWORD_REGEX="^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\[\\]{};:'\",.<>/?\\-]).{6,}$";
    private static final Pattern PASSWORD_PATTERN=Pattern.compile(PASSWORD_REGEX);

    /**
     * Checks if the password meets the specified requirements.
     *
     * @param password password to check
     * @return true if the password matches the requirements, otherwise false
     */
    private static boolean isPasswordValid(String password){
        if (password ==null){
            return false;
        }
        Matcher matcher=PASSWORD_PATTERN.matcher(password);
        return matcher.matches();
    }


    /**
     * Attempts to log in an existing user by verifying the provided credentials.
     *
     * @param username the username entered by the user
     * @param password the plain-text password entered by the user
     * @return a {@link LoginResult} object containing the authenticated {@link AuthUser} if successful,
     *         or an error message if login fails (e.g., user not found or incorrect password)
     */
    public static LoginResult loginUser(String username, String password) {
        try {
            AuthUser user = authUserDAO.getAuthUserByUsername(username);

            if (user == null) {
                return new LoginResult(null, "User does not exist.");
            }

            if (!PasswordUtils.checkPassword(password, user.getPassword())) {
                return new LoginResult(null, "Incorrect password.");
            }

            return new LoginResult(user, null);
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            return new LoginResult(null, "An error occurred during login.");
        }
    }

    /**
     * Registers a new user with the provided credentials if the username is not already taken.
     * The password is securely hashed before storage.
     *
     * @param username the desired username for the new user
     * @param password the plain-text password for the new user
     * @return a {@link LoginResult} object containing the created {@link AuthUser} if successful,
     *         or an error message if registration fails (e.g., username already exists)
     */
    public static LoginResult registerUser(String username, String password){

        AuthUser existing =authUserDAO.getAuthUserByUsername(username);
        if (existing != null) {
            return new LoginResult(null, "User already exists.");
        }
        if (!isPasswordValid(password)){
            return new LoginResult(null, "Password should contains at least one uppercase letter, one digit, one " +
                    "special character, and is at least 6 characters long.");
        }

        AuthUser newUser = new AuthUser(username, PasswordUtils.hashPassword(password),PlayerRole.USER);
        authUserDAO.insertAuthUser(newUser);

        UserStatistics newUserStatistics = new UserStatistics(username);
        userStatisticsDAO.insertUserStatistics(newUserStatistics);


        return new LoginResult(newUser, "successfully registered ");
    }
}
