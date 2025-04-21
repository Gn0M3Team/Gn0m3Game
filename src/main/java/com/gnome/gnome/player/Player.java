package com.gnome.gnome.player;

import com.gnome.gnome.monsters.Monster;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;

import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the player character in the game.
 * <p>
 * Handles movement, health, coin collection, and combat interactions with monsters.
 * The player is rendered as a yellow square in the UI.
 * </p>
 */
@Getter
public class Player {
    /**
     * Current X, Y position of the player on the grid.
     */
    private int x, y;
    /**
     * The maximum health of the player.
     */
    private final int maxHealth;

    /**
     * The current health of the player.
     */
    private int currentHealth;
    /**
     * Node representing the player visually on screen.
     */
    private final Node representation;
    /**
     * Total number of coins the player has collected.
     */
    private int playerCoins = 0;

    /**
     * Creates a new player at the specified position with the given maximum health.
     *
     * @param startX     the starting X position
     * @param startY     the starting Y position
     * @param maxHealth  the maximum health of the player
     */
    public Player(int startX, int startY, int maxHealth) {
        this.x = startX;
        this.y = startY;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.representation = new Rectangle(TILE_SIZE, TILE_SIZE, Color.YELLOW);
    }

    // Movement methods
    public void moveLeft() { x--; }
    public void moveRight() { x++; }
    public void moveUp() { y--; }
    public void moveDown() { y++; }

    /**
     * Reduces player's health by a given damage amount.
     * Health cannot go below 0.
     */
    public void takeDamage(int damage) {
        currentHealth -= damage;
        if (currentHealth < 0) {
            currentHealth = 0;
        }
    }

    /**
     * Player attacks nearby monsters.
     * Deals damage to monsters within the specified range.
     * Returns a list of monsters that were eliminated.
     *
     * @param monsters List of all monsters in the game
     * @param attackRange Range in which the player can hit
     * @param damage Damage dealt to each monster
     * @return List of monsters that died from the attack
     */
    public List<Monster> attack(List<Monster> monsters, int attackRange, int damage) {
        List<Monster> eliminated = new ArrayList<>();
        for (Monster monster : monsters) {
            int dx = Math.abs(monster.getX() - this.x);
            int dy = Math.abs(monster.getY() - this.y);
            if (dx <= attackRange && dy <= attackRange) {
                monster.takeDamage(damage);
                if (monster.getHealth() <= 0) {
                    eliminated.add(monster);
                }
            }
        }
        return eliminated;
    }

    /**
     * Resets the player's position and health to the initial state.
     * Useful when restarting the game.
     */
    public void reset(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.currentHealth = maxHealth;
    }


    /**
     * Adds a specified amount of coins to the player's total.
     *
     * @param coin the number of coins to add
     */
    public void addCoin(int coin) {
        playerCoins += coin;
    }

    /**
     * Returns the bounds of the player's visual representation on screen.
     * Useful for collision detection.
     *
     * @return the bounds of the player's representation node
     */
    public Bounds getBounds() {
        return representation.getBoundsInParent();
    }
}
