package com.gnome.gnome.dao.userDAO;
import org.mindrot.jbcrypt.BCrypt;


/**
 * Utility class for securely hashing and verifying passwords using the BCrypt algorithm.
 * <p>
 * This class provides methods to:
 * <ul>
 *   <li>Hash a plain text password with a generated salt</li>
 *   <li>Verify a plain text password against a hashed password</li>
 * </ul>
 * <p>
 * BCrypt automatically includes a salt, which enhances security and protects
 * against rainbow table attacks.
 */
public class PasswordUtils {

    /**
     * Generates a BCrypt hash for the given plain text password.
     *
     * @param plainPassword The password in plain text to be hashed.
     * @return A hashed version of the password, including the salt
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Verifies that a plain text password matches a previously hashed password.
     *
     * @param plainPassword  The plain text password provided by the user.
     * @param hashedPassword The hashed password retrieved from the database.
     * @return true if the passwords match, false otherwise.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}