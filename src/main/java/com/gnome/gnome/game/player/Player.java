package com.gnome.gnome.game.player;

import com.gnome.gnome.game.GameController;
import com.gnome.gnome.game.monsters.GameMonster;
import javafx.animation.TranslateTransition;
import javafx.geometry.Bounds;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final ImageView representation;
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
    private static final long ATTACK_COOLDOWN_NS = 300_000_000L;

    /**
     * Creates a new player at the specified position with the given maximum health.
     *
     * @param startX     the starting X position
     * @param startY     the starting Y position
     * @param maxHealth  the maximum health of the player
     */
    private Player(int startX, int startY, int maxHealth, double damage, String imagePath) {
        this.x = startX; // Set the player's initial X-coordinate on the grid
        this.y = startY; // Set the player's initial Y-coordinate on the grid
        this.maxHealth = maxHealth; // Set the player's maximum health.
        this.currentHealth = maxHealth; // Set the player's current health to the maximum health at the start of the game.
        this.damage = damage;

        this.representation = new ImageView(new Image(Objects.requireNonNull(Player.class.getResourceAsStream(imagePath))));
        this.representation.setFitWidth(TILE_SIZE * 0.6);
        this.representation.setFitHeight(TILE_SIZE * 0.6);
    }

    public static Player getInstance(int startX, int startY, int maxHealth, double damage, String imagePath) {
        if (instance == null) {
            instance = new Player(startX, startY, maxHealth, damage, imagePath);
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

    public void heal(double amount) {
        currentHealth = Math.min(maxHealth, currentHealth + amount);
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

    public void updatePositionWithCamera(int cameraStartCol, int cameraStartRow, double tileWidth, double tileHeight, Runnable onAnimationFinished) {
        double sizeX = tileWidth * 0.6;
        double sizeY = tileHeight * 0.6;
        double offsetX = (tileWidth - sizeX) / 2;
        double offsetY = (tileHeight - sizeY) / 2;
        double px = (x - cameraStartCol) * tileWidth + offsetX;
        double py = (y - cameraStartRow) * tileHeight + offsetY;

        animateToPosition(px, py, onAnimationFinished);
        representation.setFitWidth(sizeX);
        representation.setFitHeight(sizeY);
    }

    private void animateToPosition(double toX, double toY, Runnable onFinished) {
        TranslateTransition transition = new TranslateTransition(Duration.millis(50), representation);
        transition.setToX(toX);
        transition.setToY(toY);
        transition.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });
        transition.play();
    }

    public List<GameMonster> attack(List<GameMonster> gameMonsters, int range, Consumer<GameMonster> onHitEffectFinished) {
        List<GameMonster> eliminated = new ArrayList<>();

        for (GameMonster gameMonster : gameMonsters) {
            if (gameMonster.getHealth() <= 0) continue;
            int dx = Math.abs(gameMonster.getX() - x);
            int dy = Math.abs(gameMonster.getY() - y);

            if (dx <= range && dy <= range && !GameController.getGameController().isLineOfSightClear(x, y, gameMonster.getX(), gameMonster.getY())) {
                gameMonster.takeDamage(damage);
                System.out.println("Damage: " + damage + " monster: " + gameMonster.getNameEng());
                if (gameMonster.getHealth() <= 0) eliminated.add(gameMonster);

                gameMonster.showHitEffect(() -> {
                    if (eliminated.contains(gameMonster)) onHitEffectFinished.accept(gameMonster);
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