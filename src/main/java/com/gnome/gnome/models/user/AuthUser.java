package com.gnome.gnome.models.user;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents the authentication details of a user in the game, mirroring the authentication-related
 * columns of the "Users" table in the database. The username serves as a unique identifier.
 * This class is intended for security and login purposes, separate from game state.
 */
@Data
@AllArgsConstructor
public class AuthUser {
    private String username;
    private String password;
    private PlayerRole role;;
}