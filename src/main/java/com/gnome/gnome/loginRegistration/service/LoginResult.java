package com.gnome.gnome.loginRegistration.service;

import com.gnome.gnome.models.user.AuthUser;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * A simple wrapper class for returning the result of a login or registration attempt.
 * <p>
 * This class contains either:
 * <ul>
 *     <li>An {@link AuthUser} object if the operation was successful</li>
 *     <li>Or an error message string if the operation failed</li>
 * </ul>
 */
@Data
@AllArgsConstructor
public class LoginResult {
    private AuthUser user;
    private String message;
}
