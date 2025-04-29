package com.gnome.gnome.models.user;


/**
 * Represents the role of a player in the game.
 * Available roles are:
 * <ul>
 *     <li>USER - standard player</li>
 *     <li>MAP_CREATOR - player with map creation permissions</li>
 *     <li>ADMIN - player with administrative privileges</li>
 * </ul>
 */
public enum PlayerRole {
    USER,
    MAP_CREATOR,
    ADMIN;

    /**
     * Returns the PlayerRole corresponding to the given string.
     *
     * @param roleName the name of the role (case-insensitive)
     * @return the matching PlayerRole
     * @throws IllegalArgumentException if the input does not match any role
     */
    public static PlayerRole fromString(String roleName) {
        if (roleName == null) {
            throw new IllegalArgumentException("Role name cannot be null");
        }

        switch (roleName.trim().toLowerCase()) {
            case "user":
                return USER;
            case "map_creator":
                return MAP_CREATOR;
            case "admin":
                return ADMIN;
            default:
                throw new IllegalArgumentException("Unknown role: " + roleName);
        }
    }

    /**
     * Returns the role name in lowercase.
     *
     * @return the role name as a string
     */
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
