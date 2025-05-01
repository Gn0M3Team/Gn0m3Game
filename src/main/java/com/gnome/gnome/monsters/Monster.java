package com.gnome.gnome.monsters;


import com.gnome.gnome.camera.Camera;
import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.game.GameController;
import com.gnome.gnome.monsters.movements.MovementStrategy;
import com.gnome.gnome.player.Player;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.InputStream;
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
    protected double attack;
    /**
     * The current health of the monster.
     */
    protected double health;
    /**
     * The coin value awarded to the player upon defeating the monster.
     */
    protected double cost;
    /**
     * The attack range of the monster (in tiles).
     */
    protected double attackRange;
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
    protected String hitGifPath; // The file path to gif monster hit
    protected String attackImagePath;

    protected boolean isHitEffectPlaying = false; // // A flag indicating whether the monster's hit effect animation is currently playing. Used to prevent the monster from moving while the animation is active

    protected boolean isMeleeAttacking = false; // A flag indicating whether the monster is currently performing a melee attack animation. Used to prevent the monster from moving or attacking again while the animation is active
    protected long lastMeleeAttackTime = 0; // The timestamp (in nanoseconds) of the monster's last melee attack. Used to enforce a cooldown between attacks
    protected static final long MELEE_ATTACK_COOLDOWN = 3_000_000_000L; // The cooldown period (in nanoseconds) between melee attacks. 3 billion nanoseconds = 3 seconds. The monster cannot attack again until this time has passed

    protected boolean debug_mode = false; // A flag for enabling debug mode. If true, the monster might print additional debug information (though not used in this code)

    protected ImageView representation;

    protected Timeline activeAttackAnimation; // для зупинки атаки вручну

    private boolean firstUpdateDone = false;
    private int countUpdates = 0;

    private long lastMoveTime = 0;
    private static final long MOVE_COOLDOWN = 500_000_000L; // 0.5 seconds

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
    public Monster(double attack, double health, double cost, double attackRange, String nameEng, String nameSk, int startX, int startY, int value, MovementStrategy movementStrategy, String  imagePath, String hitGifPath, String attackGifPath) {
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
        this.hitGifPath = hitGifPath;
        this.attackImagePath = attackGifPath;

        initRepresentation();
    }

    /**
     * Updates the visual position of the monster based on camera offset.
     * This method must be called after moving or scrolling.
     *
     * @param cameraStartCol the first visible column (leftmost)
     * @param cameraStartRow the first visible row (topmost)
     * @param tileWidth width of one tile
     * @param tileHeight height of one tile
     */
    public void updatePositionWithCamera(int cameraStartCol, int cameraStartRow,
                                         double tileWidth, double tileHeight, boolean isTransit) {
        if (representation == null) {
            System.out.println("representation is null");
            return;
        }

        double sizeX = tileWidth * 0.6;
        double sizeY = tileHeight * 0.6;

        double offsetX = (tileWidth - sizeX) / 2;
        double offsetY = (tileHeight - sizeY) / 2;

        double px = (x - cameraStartCol) * tileWidth + offsetX;
        double py = (y - cameraStartRow) * tileHeight + offsetY;

        boolean shouldAnimate = isTransit && firstUpdateDone && countUpdates >= 5;
        if (shouldAnimate) {
            TranslateTransition transition = new TranslateTransition(Duration.millis(50), representation);
            transition.setToX(px);
            transition.setToY(py);
            transition.play();
        } else {
            representation.setTranslateX(px);
            representation.setTranslateY(py);
        }

        representation.setFitWidth(sizeX);
        representation.setFitHeight(sizeY);

        firstUpdateDone = true;
        if (countUpdates != 5)
            countUpdates++;

    }


    /**
     * Moves the monster according to its defined movement strategy.
     * This method delegates the movement logic to the MovementStrategy object assigned to the monster
     */
    public void move() {
        if (movementStrategy != null) {
            System.out.println("Monster " + nameEng + " moving from (" + x + ", " + y + ")");
            movementStrategy.move(this);
            if (representation != null) {
                representation.getProperties().put("gridX", x);
                representation.getProperties().put("gridY", y);
            }
        } else {
            System.out.println("Monster " + nameEng + " has NO movement strategy!");
        }
    }

    public void initRepresentation() {
        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        if (imageStream == null) {
            throw new RuntimeException("Missing monster image: " + imagePath);
        }
        Image img = new Image(imageStream);
        representation = new ImageView(img);
        representation.setFitWidth(TILE_SIZE * 0.6);
        representation.setFitHeight(TILE_SIZE * 0.6);

        representation.getProperties().put("gridX", x);
        representation.getProperties().put("gridY", y);
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
        if (representation != null) {
            representation.getProperties().put("gridX", newX);
            representation.getProperties().put("gridY", newY);
        }
    }

    /**
     * Reduces the monster's health by a given amount.
     *
     * @param damage the amount of damage taken
     */
    public void takeDamage(double damage) {
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
     * @param onFinish A callback function (Runnable) that is executed when the hit effect animation finishes.
     */
    public void showHitEffect(Runnable onFinish) {
        if (representation == null) {
            if (onFinish != null) onFinish.run();
            return;
        }

        isHitEffectPlaying = true;

        InputStream gifStream = getClass().getResourceAsStream(hitGifPath);
        if (gifStream == null) {
            System.err.println("Missing hit GIF: " + hitGifPath);
            isHitEffectPlaying = false;
            if (onFinish != null) onFinish.run();
            return;
        }

        Image gifImage = new Image(gifStream);
        Image originalImage = representation.getImage();

        representation.setImage(gifImage);

        PauseTransition delay = new PauseTransition(Duration.seconds(1));
        delay.setOnFinished(evt -> {
            representation.setImage(originalImage);
            isHitEffectPlaying = false;
            if (onFinish != null) onFinish.run();
        });
        delay.play();
    }




    /**
     * Performs a melee attack on the player if they are within the monster's attack range.
     * The attack includes a visual effect (among-us.gif) and deals damage to the player after the animation finishes.
     * The monster can only attack again after a cooldown period (MELEE_ATTACK_COOLDOWN).
     *
     * @param player The Player object to attack.
     * @param gameObjectsPane The JavaFX Pane where the attack effect will be displayed.
     * @param currentTime The current time (in nanoseconds), used to enforce the attack cooldown.
     */
    public void meleeAttack(Player player, Pane gameObjectsPane, long currentTime) {
        if (health <= 0) return;
        if (currentTime - lastMeleeAttackTime < MELEE_ATTACK_COOLDOWN) return;
        if (gameObjectsPane == null) {
            System.err.println("Error: gameObjectsPane is null in meleeAttack for Monster at (" + x + ", " + y + ")");
            isMeleeAttacking = false;
            return;
        }

        if (GameController.getGameController().isLineOfSightClear(x, y, player.getX(), player.getY())) {
            return;
        }

        int dx = Math.abs(player.getX() - x);
        int dy = Math.abs(player.getY() - y);
        if (dx <= attackRange && dy <= attackRange) {
            isMeleeAttacking = true;

            Image originalImage = representation.getImage();
            Image attackImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(attackImagePath)));
            representation.setImage(attackImage);

            activeAttackAnimation = new Timeline(
                    new KeyFrame(Duration.seconds(1), e -> {
                        representation.setImage(originalImage);
                        isMeleeAttacking = false;

                        int newDx = Math.abs(player.getX() - x);
                        int newDy = Math.abs(player.getY() - y);

                        if (newDx <= attackRange && newDy <= attackRange && health > 0) {
                            player.takeDamage(attack);
                        }
                        lastMeleeAttackTime = currentTime;
                        activeAttackAnimation = null;
                    })
            );
            activeAttackAnimation.setCycleCount(1);
            activeAttackAnimation.play();
        }
    }


    public void cancelMeleeAttackIfPlayerOutOfRange(Player player) {
        int dx = Math.abs(player.getX() - getX());
        int dy = Math.abs(player.getY() - getY());

        if ((dx > getAttackRange() || dy > getAttackRange()) && isMeleeAttacking) {
            setMeleeAttacking(false);

            if (activeAttackAnimation != null) {
                activeAttackAnimation.stop();
                activeAttackAnimation = null;
            }

            Image originalImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
            representation.setImage(originalImage);
        }
    }

    public void updateLogic(Player player, double delta, int [][] fieldMap, int [][] baseMap) {
        if (isHitEffectPlaying || isMeleeAttacking) return;

        long now = System.nanoTime();
        if (now - lastMoveTime < MOVE_COOLDOWN) return;
        lastMoveTime = now;

        int dx = Math.abs(player.getX() - x);
        int dy = Math.abs(player.getY() - y);
        if (dx <= attackRange && dy <= attackRange) return;

        int oldX = x, oldY = y;
        move();

        int newX = getX(), newY = getY();
        if (newX < 0 || newY < 0 || newY >= fieldMap.length || newX >= fieldMap[0].length) {
            setPosition(oldX, oldY);
        } else {
            int tile = fieldMap[newY][newX];
            if (tile < 0) tile = baseMap[newY][newX];

            TypeOfObjects type = TypeOfObjects.fromValue(tile);
            boolean chestOnTile = GameController.getGameController().isBlocked(newX, newY, this);

            if (type.isObstacle() || chestOnTile) {
                setPosition(oldX, oldY);
            }
        }

        updateVisual(Camera.getInstance());
    }

    public void updateVisual(Camera camera) {
        updatePositionWithCamera(camera.getStartCol(), camera.getStartRow(), camera.getTileWidth(), camera.getTileHeight(), true);
    }
}