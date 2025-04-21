package com.gnome.gnome.monsters;


import com.gnome.gnome.monsters.movements.MovementStrategy;
import javafx.scene.Node;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Abstract base class representing a monster in the game.
 * <p>
 * Each monster has attributes such as health, attack power, movement strategy,
 * and localization-friendly names. The class also defines behavior for moving,
 * attacking, taking damage, and position updates.
 * </p>
 */
@Data
@AllArgsConstructor
public abstract class Monster {
    /**
     * The amount of damage the monster can deal.
     */
    protected int attack;
    /**
     * The current health of the monster.
     */
    protected int health;
    /**
     * The coin value awarded to the player upon defeating the monster.
     */
    protected int cost;
    /**
     * The attack range of the monster (in tiles).
     */
    protected int attackRange;
    /**
     * The English name of the monster.
     */
    protected String nameEng;
    /**
     * The Slovak name of the monster.
     */
    protected String nameSk;

    /**
     * The current X, Y position of the monster on the grid.
     */
    protected int x, y;

    /**
     * The internal identifier or value for the monster type (e.g., used on the map).
     */
    protected int value;

    /**
     * The movement strategy used by the monster.
     */
    protected MovementStrategy movementStrategy;


    /**
     * Executes an attack action based on monster type.
     * Must be implemented by concrete subclasses.
     *
     * @param cameraStartCol the starting column of the camera viewport
     * @param cameraStartRow the starting row of the camera viewport
     * @param playerGridX    the player's current X position
     * @param playerGridY    the player's current Y position
     * @return attack result (e.g., projectile or null if not attacking)
     */
    public abstract Object attack(int cameraStartCol, int cameraStartRow, int playerGridX, int playerGridY);


    /**
     * Moves the monster based on its defined movement strategy.
     */
    public void move() {
        if (movementStrategy != null)
            movementStrategy.move(this);
    }


    /**
     * Sets the monster's position on the grid.
     *
     * @param newX new X coordinate
     * @param newY new Y coordinate
     */
    public void setPosition(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    /**
     * Returns the integer value used to represent the monster on the map.
     *
     * @return monster type value
     */
    public int getMonsterValue() {
        return value;
    }

    /**
     * Reduces the monster's health by a given amount.
     *
     * @param damage the amount of damage taken
     */
    public void takeDamage(int damage) {
        health -= damage;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Attack: %d, Health: %d, Cost: %d, Attack Range: %d, Position: (%d, %d)",
                nameEng, nameSk, attack, health, cost, attackRange, x, y);
    }
}
