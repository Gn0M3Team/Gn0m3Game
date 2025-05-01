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
    private int x, y;
    private final int maxHealth;
    private double damage;
    private double currentHealth;
    private final Node representation;
    private double playerCoins = 0;
    private int score = 0;
    private int countOfOpenedChest = 0;
    private int countOfKilledMonsters = 0;
    private static Player instance;
    @Setter
    private double dynamicTileSize;

    private long lastMoveTime = 0;
    private static final long MOVE_COOLDOWN_NS = 200_000_000L;

    private long lastAttackTime = 0;
    private static final long ATTACK_COOLDOWN_NS = 500_000_000L;

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


    /**
     * Resets the player's position and health to the initial state.
     * Useful when restarting the game.
     */
    public static void resetInstance() {
        instance = null;
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
        currentHealth = Math.max(0, currentHealth - damage);
        GameController ctrl = GameController.getGameController();
        if (ctrl != null) {
            ctrl.shakeCamera();
            ctrl.updatePlayerHealthBar();
        }
    }

    public void addCoin(double coin) {
        playerCoins += coin;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public void addCountOfOpenedChest() {
        countOfOpenedChest++;
    }

    public void addCountOfKilledMonsters() {
        countOfKilledMonsters++;
    }

    public Bounds getBounds() {
        return representation.getBoundsInParent();
    }

    public void updatePositionWithCamera(int cameraStartCol, int cameraStartRow, double tileWidth, double tileHeight) {
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

    public List<Monster> attack(List<Monster> monsters, int range, Consumer<Monster> onHitEffectFinished) {
        List<Monster> eliminated = new ArrayList<>();

        for (Monster monster : monsters) {
            if (monster.getHealth() <= 0) continue;
            int dx = Math.abs(monster.getX() - x);
            int dy = Math.abs(monster.getY() - y);

            if (dx <= range && dy <= range && !GameController.getGameController().isLineOfSightClear(x, y, monster.getX(), monster.getY())) {
                monster.takeDamage(damage);
                System.out.println("Damage: " + damage + " monster: " + monster.getNameEng());
                if (monster.getHealth() <= 0) eliminated.add(monster);

                monster.showHitEffect(() -> {
                    if (eliminated.contains(monster)) onHitEffectFinished.accept(monster);
                });
            }
        }

        return eliminated;
    }


    public boolean canMoveNow() {
        long now = System.nanoTime();
        return (now - lastMoveTime) >= MOVE_COOLDOWN_NS;
    }

    public void recordMoveTime() {
        lastMoveTime = System.nanoTime();
    }

    public boolean canAttackNow() {
        return (System.nanoTime() - lastAttackTime) >= ATTACK_COOLDOWN_NS;
    }

    public void recordAttackTime() {
        lastAttackTime = System.nanoTime();
    }
}