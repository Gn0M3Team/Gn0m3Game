package com.gnome.gnome.loginRegistration.service;

import com.gnome.gnome.dao.userDAO.AuthUserDAO;
import com.gnome.gnome.dao.userDAO.PasswordUtils;
import com.gnome.gnome.models.user.AuthUser;

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
    /**
     *length ≥ 12, at least 1 capital letter, 1 number, 1 special character
     */
    private static final String PASSWORD_REGEX="^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=\\[\\]{};:'\",.<>/?\\-]).{12,}$";
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
     * Attempting to log in the user. If the user does not exist, it is registered.
     *
     * @param username username
     * @param password password
     * @return AuthUser object if login or registration is successful, or null if failed
     */
    public static LoginResult loginOrRegisterUser(String username, String password) {
        try {

            if (!isPasswordValid(password)){
                return new LoginResult(null, "Password must be at least 12 characters, with 1 uppercase letter, 1 number, and 1 special character.");
            }

            AuthUser user = authUserDAO.getAuthUserByUsername(username);

            if (user != null) {
                if (PasswordUtils.checkPassword(password, user.getPassword())){
                    System.out.println("User found: " + user);
                    return new LoginResult(user,null);
                }else {
                    return new LoginResult(null, "Incorrect password.");
                }
            } else {
                AuthUser newUser = new AuthUser(username, PasswordUtils.hashPassword(password), "user");
                System.out.println(newUser);
                authUserDAO.insertAuthUser(newUser);
                System.out.println("User not found.User creation: "+username);

                user = authUserDAO.getAuthUserByUsername(username);
                if (user != null) {
                    return new LoginResult(user, null);
                } else {
                    return new LoginResult(null, "Failed to create user.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return null;
        }
    }
}
