package com.gnome.gnome.monsters;


import com.gnome.gnome.monsters.movements.MovementStrategy;
import com.gnome.gnome.player.Player;
import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Objects;

import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;

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

    protected String imagePath; // The file path to the monster's image. This is used to visually represent the monster on the map
    protected String hitGifPath = "/com/gnome/gnome/effects/red_monster.gif";  // The file path to the GIF used for the monster's hit effect (shown when the monster takes damage). This is a default value for all monsters

    protected boolean isHitEffectPlaying = false; // // A flag indicating whether the monster's hit effect animation is currently playing. Used to prevent the monster from moving while the animation is active

    protected boolean isMeleeAttacking = false; // A flag indicating whether the monster is currently performing a melee attack animation. Used to prevent the monster from moving or attacking again while the animation is active
    protected long lastMeleeAttackTime = 0; // The timestamp (in nanoseconds) of the monster's last melee attack. Used to enforce a cooldown between attacks
    protected static final long MELEE_ATTACK_COOLDOWN = 3_000_000_000L; // The cooldown period (in nanoseconds) between melee attacks. 3 billion nanoseconds = 3 seconds. The monster cannot attack again until this time has passed

    protected boolean debug_mode = false; // A flag for enabling debug mode. If true, the monster might print additional debug information (though not used in this code)

    /**
     * Constructor for the Monster class. This method is called when a new Monster object is created.
     * It initializes all the monster's attributes with the provided values.
     *
     * @param attack The damage the monster deals per attack.
     * @param health The monster's initial health.
     * @param cost The coin value awarded to the player when the monster is defeated.
     * @param attackRange The range (in tiles) within which the monster can attack.
     * @param nameEng The English name of the monster.
     * @param nameSk The Slovak name of the monster.
     * @param startX The starting X-coordinate of the monster on the grid (in tile units).
     * @param startY The starting Y-coordinate of the monster on the grid (in tile units).
     * @param value The internal identifier for the monster type (used on the map).
     * @param movementStrategy The strategy that determines how the monster moves.
     * @param imagePath The file path to the monster's image.
     */
    public Monster(int attack, int health, int cost, int attackRange, String nameEng, String nameSk, int startX, int startY, int value, MovementStrategy movementStrategy, String  imagePath) {
        this.attack = attack; // Set the monster's attack damage
        this.health = health; // Set the monster's initial health
        this.cost = cost; // Set the coin reward for defeating the monster
        this.attackRange = attackRange; // Set the monster's attack range
        this.nameEng = nameEng; // Set the English name
        this.nameSk = nameSk; // Set the Slovak name
        this.x = startX; // Set the starting X-coordinate
        this.y = startY; // Set the starting Y-coordinate
        this.value = value; // Set the monster's identifier for the map
        this.movementStrategy = movementStrategy; // Set the movement strategy
        this.imagePath = imagePath; // Set the path to the monster's image
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
     * Moves the monster according to its defined movement strategy.
     * This method delegates the movement logic to the MovementStrategy object assigned to the monster
     */
    public void move() {
        // Check if the monster has a movement strategy (not null)
        if (movementStrategy != null)
            movementStrategy.move(this); // Call the move method of the movement strategy, passing this monster as the argument. The movement strategy will update the monster's x and y coordinates based on its logic (e.g., random movement, chasing the player).
    }


    /**
     * Sets the monster's position on the grid.
     *
     * @param newX new X coordinate
     * @param newY new Y coordinate
     */
    public void setPosition(int newX, int newY) {
        this.x = newX; // Update the monster's X-coordinate
        this.y = newY; // Update the monster's Y-coordinate
    }

    /**
     * Returns the integer value used to represent the monster on the map.
     *
     * @return monster type value
     */
    public int getMonsterValue() {
        return value; // Return the monster's identifier
    }

    /**
     * Reduces the monster's health by a given amount.
     *
     * @param damage the amount of damage taken
     */
    public void takeDamage(int damage) {
        health -= damage; // Subtract the damage from the monster's current health
        System.out.println("Monster at (" + x + ", " + y + ") took " + damage + " damage, health now: " + health);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Attack: %d, Health: %d, Cost: %d, Attack Range: %d, Position: (%d, %d)",
                nameEng, nameSk, attack, health, cost, attackRange, x, y);
    }

    /**
     * Displays a hit effect animation when the monster takes damage.
     * The effect is shown as a GIF (red_monster.gif) at the monster's position on the screen.
     * The animation lasts for 1 second, during which the monster cannot move.
     *
     * @param gameObjectsPane The JavaFX Pane where the hit effect will be displayed.
     * @param cameraStartCol The starting column of the camera's viewport (used for positioning the effect).
     * @param cameraStartRow The starting row of the camera's viewport (used for positioning the effect).
     * @param onFinish A callback function (Runnable) that is executed when the hit effect animation finishes.
     */
    public void showHitEffect(Pane gameObjectsPane, int cameraStartCol, int cameraStartRow, Runnable onFinish) {
        // Check if the gameObjectsPane is null (i.e., the container for visual effects is not available)
        if (gameObjectsPane == null) {
            System.err.println("Error: gameObjectsPane is null in showHitEffect for Monster at (" + x + ", " + y + ")");
            // If there is a callback function, execute it immediately since the effect cannot be shown
            if (onFinish != null) {
                onFinish.run();
            }
            return; // Exit the method since we cannot display the effect
        }

        // Set the flag to indicate that the hit effect animation is playing
        // This prevents the monster from moving while the animation is active
        isHitEffectPlaying = true;

        // Create an ImageView to display the hit effect GIF (red_monster.gif)
        // Objects.requireNonNull ensures the image resource exists, or it throws an exception
        ImageView effectView = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(hitGifPath))));
        effectView.setFitWidth(50); // Set the width of the GIF to 50 pixels
        effectView.setFitHeight(50); // Set the height of the GIF to 50 pixels


        // Calculate the absolute position of the monster in pixels (not relative to the camera)
        // Since each tile is 50x50 pixels (TILE_SIZE=50), multiply the monster's grid coordinates by 50
        double absolutePixelX = x * 50; // Absolute X position in pixels
        double absolutePixelY = y * 50; // Absolute Y position in pixels

        // Adjust the GIF's position relative to the camera's viewport
        // The cameraStartCol and cameraStartRow indicate the top-left corner of the camera's viewport
        // Subtract these values (scaled by TILE_SIZE) to position the GIF correctly on the screen
        double pixelX = absolutePixelX - (cameraStartCol * 50); // Adjusted X position
        double pixelY = absolutePixelY - (cameraStartRow * 50); // Adjusted Y position
        effectView.setTranslateX(pixelX); // Set the X position of the GIF
        effectView.setTranslateY(pixelY); // Set the Y position of the GIF

        // Store the absolute coordinates in the ImageView's properties
        // This allows the game to update the GIF's position if the camera moves while the animation is playing
        effectView.getProperties().put("absoluteX", absolutePixelX);
        effectView.getProperties().put("absoluteY", absolutePixelY);

        // Add the GIF to the gameObjectsPane so it appears on the screen
        gameObjectsPane.getChildren().add(effectView);
        System.out.println("Monster at (" + x + ", " + y + ") showing hit effect");

        // Create a PauseTransition to control the duration of the hit effect animation (1 second)
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        // Define what happens when the animation finishes
        pause.setOnFinished(event -> {
            // Remove the GIF from the gameObjectsPane to stop displaying it
            gameObjectsPane.getChildren().remove(effectView);
            System.out.println("Monster at (" + x + ", " + y + ") hit effect finished");
            // Reset the flag to indicate the hit effect animation is no longer playing
            isHitEffectPlaying = false;
            // If there is a callback function, execute it now that the animation is complete
            if (onFinish != null) {
                onFinish.run();
            }
        });
        // Start the animation
        pause.play();
    }

    /**
     * Performs a melee attack on the player if they are within the monster's attack range.
     * The attack includes a visual effect (among-us.gif) and deals damage to the player after the animation finishes.
     * The monster can only attack again after a cooldown period (MELEE_ATTACK_COOLDOWN).
     *
     * @param player The Player object to attack.
     * @param gameObjectsPane The JavaFX Pane where the attack effect will be displayed.
     * @param cameraStartCol The starting column of the camera's viewport (used for positioning the effect).
     * @param cameraStartRow The starting row of the camera's viewport (used for positioning the effect).
     * @param currentTime The current time (in nanoseconds), used to enforce the attack cooldown.
     */
    public void meleeAttack(Player player, Pane gameObjectsPane, int cameraStartCol, int cameraStartRow, long currentTime) {
        // Check if the monster is already dead (health <= 0). If so, do not attack
        if (health <= 0) return;

        // Check if enough time has passed since the last attack (enforce the cooldown)
        // If the difference between the current time and the last attack time is less than the cooldown, do not attack
        if (currentTime - lastMeleeAttackTime < MELEE_ATTACK_COOLDOWN) return;

        // Check if the gameObjectsPane is null (i.e., the container for visual effects is not available)
        if (gameObjectsPane == null) {
            System.err.println("Error: gameObjectsPane is null in meleeAttack for Monster at (" + x + ", " + y + ")");
            // Reset the attacking flag since the attack cannot proceed
            isMeleeAttacking = false;
            return;
        }

        // Calculate the distance between the monster and the player:
        // - Math.abs(player.getX() - x) gives the absolute difference in X-coordinates
        // - Math.abs(player.getY() - y) gives the absolute difference in Y-coordinates
        int dx = Math.abs(player.getX() - x);
        int dy = Math.abs(player.getY() - y);

        // Check if the player is within the monster's attack range (e.g., if attackRange=1, the player must be 1 tile away or closer)
        if (dx <= attackRange && dy <= attackRange) {
            // Set the flag to indicate that the monster is performing a melee attack animation
            // This prevents the monster from moving or attacking again while the animation is active
            isMeleeAttacking = true;

            // Load the attack effect GIF (among-us.gif) to display during the attack
            // Objects.requireNonNull ensures the image resource exists, or it throws an exception
            Image attackEffectImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/gnome/gnome/effects/among-us.gif")));
            // Create an ImageView to display the attack effect GIF
            ImageView attackEffectView = new ImageView(attackEffectImage);
            attackEffectView.setFitWidth(TILE_SIZE); // Set the width of the GIF to match the tile size (50 pixels)
            attackEffectView.setFitHeight(TILE_SIZE); // // Set the height of the GIF to match the tile size (50 pixels)

            // Calculate the position of the attack effect relative to the camera's viewport:
            // - gridX and gridY are the monster's coordinates relative to the top-left corner of the camera's viewport
            int gridX = x - cameraStartCol;
            int gridY = y - cameraStartRow;
            // Convert the grid coordinates to pixel coordinates by multiplying by TILE_SIZE
            double pixelX = gridX * TILE_SIZE;
            double pixelY = gridY * TILE_SIZE;

            // Set the position of the attack effect GIF on the screen
            attackEffectView.setTranslateX(pixelX);
            attackEffectView.setTranslateY(pixelY);

            // Store the absolute coordinates in the ImageView's properties
            // This allows the game to update the GIF's position if the camera moves while the animation is playing
            attackEffectView.getProperties().put("absoluteX", pixelX + (cameraStartCol * TILE_SIZE));
            attackEffectView.getProperties().put("absoluteY", pixelY + (cameraStartRow * TILE_SIZE));

            // Add the attack effect GIF to the gameObjectsPane so it appears on the screen
            gameObjectsPane.getChildren().add(attackEffectView);

            // Create a PauseTransition to control the duration of the attack effect animation (1 second)
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            // Define what happens when the animation finishes
            pause.setOnFinished(event -> {
                // Remove the attack effect GIF from the gameObjectsPane to stop displaying it
                gameObjectsPane.getChildren().remove(attackEffectView);
                // Reset the flag to indicate the attack animation is no longer playing
                isMeleeAttacking = false;

                // Recalculate the distance between the monster and the player after the animation
                // This ensures the player is still within range before dealing damage (the player might have moved during the animation)
                int newDx = Math.abs(player.getX() - x);
                int newDy = Math.abs(player.getY() - y);
                // Check if the player is still within 1 tile of the monster (hardcoded range for dealing damage) and the monster is still alive
                if (newDx <= 1 && newDy <= 1 && health > 0) {
                    // Deal damage to the player (hardcoded to 5 for now, but the comment suggests using the monster's attack value)
                    player.takeDamage(5); // TODO: CHANGE TO MONSTER DAMAGE
                    System.out.println("Monster at (" + x + ", " + y + ") dealt " + attack + " damage to player at (" + player.getX() + ", " + player.getY() + ")");
                }
                // Update the timestamp of the last attack to enforce the cooldown for the next attack
                lastMeleeAttackTime = currentTime;
            });
            // Start the animation
            pause.play();
        }
    }
}