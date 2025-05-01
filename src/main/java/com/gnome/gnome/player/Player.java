package com.gnome.gnome.player;

import com.gnome.gnome.game.GameController;
import com.gnome.gnome.monsters.Monster;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    private double damage;

    /**
     * The current health of the player.
     */
    private double currentHealth;
    /**
     * Node representing the player visually on screen.
     */
    private final Node representation;
    /**
     * Total number of coins the player has collected.
     */
    private double playerCoins = 0;

    private static Player instance;

    @Setter
    private double dynamicTileSize;

    /**
     * Creates a new player at the specified position with the given maximum health.
     *
     * @param startX     the starting X position
     * @param startY     the starting Y position
     * @param maxHealth  the maximum health of the player
     */
    private Player(int startX, int startY, int maxHealth, double damage) {
        this.x = startX; // Set the player's initial X-coordinate on the grid
        this.y = startY; // Set the player's initial Y-coordinate on the grid
        this.maxHealth = maxHealth; // Set the player's maximum health.
        this.currentHealth = maxHealth; // Set the player's current health to the maximum health at the start of the game.
        this.damage = damage;

        // Create a yellow square to visually represent the player:
        Rectangle rect = new Rectangle(TILE_SIZE * 0.6, TILE_SIZE * 0.6, Color.YELLOW);
        rect.setArcWidth(10);
        rect.setArcHeight(10);
        this.representation = rect;
    }

    public static Player getInstance(int startX, int startY, int maxHealth, double damage) {
        if (instance == null) {
            instance = new Player(startX, startY, maxHealth, damage);
        }
        return instance;
    }

    public static Player getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Player has not been initialized");
        }
        return instance;
    }


    // Movement methods: These methods allow the player to move on the grid by changing their x and y coordinates
    // Each method adjusts the player's position by 1 tile in the specified direction
    public void moveLeft() { x--; }
    public void moveRight() { x++; }
    public void moveUp() { y--; }
    public void moveDown() { y++; }

    /**
     * Reduces player's health by a given damage amount.
     * Health cannot go below 0.
     */
    public void takeDamage(double damage) {
        currentHealth -= damage;
        if (currentHealth < 0) currentHealth = 0;

        GameController ctrl = GameController.getGameController();
        if (ctrl != null) {
            ctrl.shakeCamera(); // <-- трясіння камери при пошкодженні
            ctrl.updatePlayerHealthBar(); // оновити здоров'я на панелі
        }
    }

    /**
     * Allows the player to attack monsters within a specified range.
     * This method deals damage to monsters that are close enough to the player and returns a list of monsters that were eliminated.
     * It also triggers a visual hit effect for each monster that is attacked.
     *
     * @param monsters A list of all monsters currently in the game.
     * @param range The attack range (in tiles). For example, if range=1, the player can attack monsters 1 tile away in any direction.
     * @param onHitEffectFinished A callback function (Consumer) that is called when a monster's hit effect finishes.
     *                            This is used to handle actions after the hit effect (e.g., removing a dead monster).
     * @return A list of monsters that were eliminated (killed) by the attack.
     */
    public List<Monster> attack(List<Monster> monsters,
                                int range,
                                Consumer<Monster> onHitEffectFinished) {
        // Create a list to store monsters that are within the player's attack range
        List<Monster> hitMonsters = new ArrayList<>();

        // Iterate through all monsters in the game to check which ones are within attack range
        for (Monster monster : monsters) {
            // Calculate the distance between the player and the monster:
            // - Math.abs(monster.getX() - x) gives the absolute difference in X-coordinates
            // - Math.abs(monster.getY() - y) gives the absolute difference in Y-coordinates
            int dx = Math.abs(monster.getX() - x);
            int dy = Math.abs(monster.getY() - y);

            // Check if the monster is within the attack range (e.g., if range=1, the monster must be 1 tile away or closer)
            // This checks for monsters in a square area around the player, not just in a straight line
            if (dx <= range && dy <= range) {
                if (GameController.getGameController().isLineOfSightClear(this.x, this.y, monster.getX(), monster.getY())) {
                    continue; // монстр за об'єктом, пропустити
                }

                hitMonsters.add(monster);
            }
        }

        // Create a list to store monsters that are eliminated (killed) by the attack
        List<Monster> eliminated = new ArrayList<>();

        // Iterate through all monsters that were hit to apply damage and visual effects
        for (Monster monster : hitMonsters) {
            // Apply the specified damage to the monster (e.g., reduce its health by 20)
            monster.takeDamage(this.damage);

            // Check if the monster's health has dropped to 0 or below (i.e., the monster is dead)
            if (monster.getHealth() <= 0) {
                eliminated.add(monster); // Add the monster to the list of eliminated monsters
            }

            // Show a visual hit effect for the monster (animation)
            // The hit effect is displayed on the gameObjectsPane at the monster's position, adjusted for the camera's viewport
            // The lambda expression () -> {...} is a callback that runs when the hit effect finishes
            monster.showHitEffect(() -> {
                // Check if this monster was eliminated (killed) during the attack
                if (eliminated.contains(monster)) {
                    // If the monster was eliminated, call the callback function to handle post-elimination actions
                    // For example, this remove the monster from the game and replace it with a coin
                    onHitEffectFinished.accept(monster);
                }
            });
        }

        // Return the list of monsters that were eliminated by the attack
        // This list used by the game to remove those monsters and update the game state
        return eliminated;
    }

    /**
     * Resets the player's position and health to the initial state.
     * Useful when restarting the game.
     */
    public static void resetInstance() {
        instance = null;
    }


    /**
     * Adds a specified amount of coins to the player's total.
     *
     * @param coin the number of coins to add
     */
    public void addCoin(double coin) {
        playerCoins += coin; // Increase the player's coin count by the specified amount
    }

    /**
     * Returns the bounds of the player's visual representation on screen.
     * Useful for collision detection.
     *
     * @return the bounds of the player's representation node
     */
    public Bounds getBounds() {
        // representation.getBoundsInParent() returns the bounding box of the player's yellow square
        // This includes the square's position (adjusted for any transformations) and its size (TILE_SIZE x TILE_SIZE)
        return representation.getBoundsInParent();
    }

    /**
     * Updates the visual position of the player based on camera offset.
     * This method must be called after moving or scrolling.
     *
     * @param cameraStartCol the first visible column (leftmost)
     * @param cameraStartRow the first visible row (topmost)
     */
    /**
     * Center the player rectangle in its cell using the actual cell width/height.
     */
    public void updatePositionWithCamera(int cameraStartCol, int cameraStartRow,
                                         double tileWidth, double tileHeight) {
        double sizeX = tileWidth * 0.6;
        double sizeY = tileHeight * 0.6;

        double offsetX = (tileWidth - sizeX) / 2;
        double offsetY = (tileHeight - sizeY) / 2;

        double px = (x - cameraStartCol) * tileWidth + offsetX;
        double py = (y - cameraStartRow) * tileHeight + offsetY;

        animateToPosition(px, py);

        if (representation instanceof Rectangle r) {
            r.setWidth(sizeX);
            r.setHeight(sizeY);
        }
    }

    private void animateToPosition(double toX, double toY) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(50), representation);
        transition.setToX(toX);
        transition.setToY(toY);
        transition.play();
    }

}