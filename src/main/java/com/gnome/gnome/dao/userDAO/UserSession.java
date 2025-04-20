package com.gnome.gnome.dao.userDAO;

import com.gnome.gnome.models.user.AuthUser;
import lombok.Getter;
import lombok.Setter;


/**
 * Singleton class that stores information about the currently authenticated user.
 * <p>
 * The {@code UserSession} class is used to keep track of the user who is currently logged in.
 * This allows any part of the application to access the current user without having to pass
 * the user object around manually.
 * </p>
 *
 * <p>
 * It follows the Singleton design pattern to ensure that only one instance of the session
 * exists throughout the application's lifecycle.
 * </p>
 */
@Getter
@Setter
public class UserSession {
    private static UserSession instance;
    private AuthUser currentUser;

    private UserSession(){}

    /**
     * Returns the singleton instance of the {@code UserSession} class.
     * If the instance doesn't exist yet, it creates a new one.
     *
     * @return the single instance of {@code UserSession}
     */
    public static UserSession getInstance(){
        if (instance==null){
            instance=new UserSession();
        }
        return instance;
    }
}
