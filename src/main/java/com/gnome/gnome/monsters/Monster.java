package com.gnome.gnome.monsters;


import com.gnome.gnome.monsters.movements.MovementStrategy;
import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

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
    protected String imagePath;
    protected String hitGifPath = "/com/gnome/gnome/effects/red_monster.gif";  // Path to GIF

    protected boolean isHitEffectPlaying = false;

    protected boolean debug_mode = false;
    public Monster(int attack, int health, int cost, int attackRange, String nameEng, String nameSk, int startX, int startY, int value, MovementStrategy movementStrategy, String  imagePath) {
        this.attack = attack;
        this.health = health;
        this.cost = cost;
        this.attackRange = attackRange;
        this.nameEng = nameEng;
        this.nameSk = nameSk;
        this.x = startX;
        this.y = startY;
        this.value = value;
        this.movementStrategy = movementStrategy;
        this.imagePath = imagePath;
    }

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
        System.out.println("Monster at (" + x + ", " + y + ") took " + damage + " damage, health now: " + health);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Attack: %d, Health: %d, Cost: %d, Attack Range: %d, Position: (%d, %d)",
                nameEng, nameSk, attack, health, cost, attackRange, x, y);
    }

    /**
     * I'm lazy
     * @param gameObjectsPane
     * @param cameraStartCol
     * @param cameraStartRow
     * @param onFinish
     */
    public void showHitEffect(Pane gameObjectsPane, int cameraStartCol, int cameraStartRow, Runnable onFinish) {
        if (gameObjectsPane == null) {
            System.err.println("Error: gameObjectsPane is null in showHitEffect for Monster at (" + x + ", " + y + ")");
            if (onFinish != null) {
                onFinish.run();
            }
            return;
        }

        isHitEffectPlaying = true;

        // Create an ImageView for a GIF
        ImageView effectView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(hitGifPath))));
        effectView.setFitWidth(50);
        effectView.setFitHeight(50);

        // Position the GIF in absolute coordinates (not relative to the camera)
        double absolutePixelX = x * 50;
        double absolutePixelY = y * 50;

        // Initially adjust the position to the current camera offset
        double pixelX = absolutePixelX - (cameraStartCol * 50);
        double pixelY = absolutePixelY - (cameraStartRow * 50);
        effectView.setTranslateX(pixelX);
        effectView.setTranslateY(pixelY);

        // Add a property to store absolute coordinates
        effectView.getProperties().put("absoluteX", absolutePixelX);
        effectView.getProperties().put("absoluteY", absolutePixelY);

        gameObjectsPane.getChildren().add(effectView);
        System.out.println("Monster at (" + x + ", " + y + ") showing hit effect");

        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> {
            gameObjectsPane.getChildren().remove(effectView);
            System.out.println("Monster at (" + x + ", " + y + ") hit effect finished");
            isHitEffectPlaying = false;
            if (onFinish != null) {
                onFinish.run();
            }
        });
        pause.play();
    }
}