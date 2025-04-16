package com.gnome.gnome.player;

import com.gnome.gnome.monsters.Monster;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Player {
    private int x;
    private int y;
    private final int maxHealth;
    private int currentHealth;
    private final Node representation;

    public Player(int startX, int startY, int maxHealth) {
        this.x = startX;
        this.y = startY;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.representation = createRepresentation();
    }

    /**
     * Creates a visual representation of the player as a yellow square.
     */
    private Node createRepresentation() {
        Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE);
        rect.setFill(Color.YELLOW);
        return rect;
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
}
