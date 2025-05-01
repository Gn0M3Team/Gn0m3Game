package com.gnome.gnome.models.user;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents the game state and equipment details of a user in the game, mirroring the game-related
 * columns of the "Users" table in the database. The username links this state to an AuthUser.
 * This class manages user progress and equipped items, separate from authentication.
 */
@Data
@AllArgsConstructor
public class UserGameState {
    private String username; // Foreign key referencing AuthUser
    private float balance;
    private float health;
    private int score;
    private int mapLevel;
    private Integer weaponId; // Nullable foreign key to Weapon
    private Integer potionId; // Nullable foreign key to Potion
    private Integer armorId;  // Nullable foreign key to Armor
}
