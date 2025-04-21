package com.gnome.gnome.monsters.types.missels;

import com.gnome.gnome.monsters.types.Skeleton;
import com.gnome.gnome.player.Player;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.Pane;
import lombok.Getter;

import java.util.Random;
import java.util.UUID;

import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import lombok.Setter;

import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;

/**
 * Represents a projectile arrow shot by a {@link Skeleton} in the game.
 * <p>
 * The arrow moves toward a fixed target using a defined velocity and can interact with the {@link Player}.
 * This class handles rendering, animation, movement, and collision detection.
 * </p>
 */
public class Arrow {
    /**
     * The visual representation of the arrow as a colored rectangle.
     */
    @Getter
    private final Rectangle view;
    /**
     * Absolute coordinates of the arrow (in tiles).
     */
    private double x, y;
    /**
     * Velocity in the X, Y direction (tiles per frame).
     */
    private final double vx, vy;
    /**
     * Target X, Y coordinate of the arrow (in tiles).
     */
    private final double targetX, targetY;
    /**
     * The amount of damage this arrow inflicts on the player.
     */
    private static final int DAMAGE = 10;
    /**
     * Unique identifier for debugging purposes.
     */
    private final String arrowId = UUID.randomUUID().toString();
    /**
     * Whether the arrow is currently in motion.
     */
    private boolean isAnimating = false;

    /**
     * The skeleton that fired this arrow.
     */
    @Setter
    private Skeleton skeleton;
    private int cameraStartCol, cameraStartRow;

    /**
     * Constructs a new Arrow with the specified starting position, velocity, and target.
     *
     * @param startX   the initial X position (in tiles)
     * @param startY   the initial Y position (in tiles)
     * @param vx       the horizontal velocity (tiles per second)
     * @param vy       the vertical velocity (tiles per second)
     * @param targetX  the target X coordinate (in tiles)
     * @param targetY  the target Y coordinate (in tiles)
     */
    public Arrow(double startX, double startY, double vx, double vy, double targetX, double targetY) {
        this.x = startX;
        this.y = startY;
        this.vx = vx / 60.0;
        this.vy = vy / 60.0;
        this.targetX = targetX;
        this.targetY = targetY;
        System.out.println("Creating arrow " + arrowId + " at (" + startX + ", " + startY + ") with velocity (" + vx + ", " + vy + ") towards (" + targetX + ", " + targetY + ")");

        view = new Rectangle(30, 30);
        Random rand = new Random();
        view.setFill(Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
    }

    /**
     * Updates the camera offset so the arrow can be rendered correctly relative to the viewport.
     *
     * @param cameraStartCol the starting column of the camera
     * @param cameraStartRow the starting row of the camera
     */
    public void updateCameraOffset(int cameraStartCol, int cameraStartRow) {
        this.cameraStartCol = cameraStartCol;
        this.cameraStartRow = cameraStartRow;
        // Convert absolute position in tiles to pixels in viewport
        double pixelX = (x - cameraStartCol) * TILE_SIZE;
        double pixelY = (y - cameraStartRow) * TILE_SIZE;
        view.setTranslateX(pixelX);
        view.setTranslateY(pixelY);
    }

    /**
     * Adds the arrow to the scene and animates its movement until it goes out of bounds,
     * reaches the target, or collides with the player.
     *
     * @param layer  the JavaFX pane to which the arrow is added
     * @param player the player object to check for collisions
     */
    public void shoot(Pane layer, Player player) {
        if (isAnimating) {
            System.out.println("Arrow " + arrowId + " is already animating, skipping shoot");
            return;
        }
        isAnimating = true;

        System.out.println("Adding arrow " + arrowId + " to gameObjectsPane at (" + x + ", " + y + ")");
        layer.getChildren().add(view);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Update the absolute position in the tiles
                x += vx;
                y += vy;

                // Convert absolute position to pixels for display
                double pixelX = (x - cameraStartCol) * TILE_SIZE;
                double pixelY = (y - cameraStartRow) * TILE_SIZE;
                view.setTranslateX(pixelX);
                view.setTranslateY(pixelY);
                System.out.println("Arrow " + arrowId + " moving to (" + x + ", " + y + ") in tiles, (" + pixelX + ", " + pixelY + ") in pixels");

                double paneWidth = layer.getWidth();
                double paneHeight = layer.getHeight();
                if (paneWidth == 0 || paneHeight == 0) {
                    System.out.println("Warning: gameObjectsPane has invalid size: " + paneWidth + "x" + paneHeight);
                }

                // Check whether the arrow has gone beyond the viewport or reached the target point
                if (pixelX < 0 || pixelX > paneWidth || pixelY < 0 || pixelY > paneHeight ||
                        (vx > 0 && x >= targetX) || (vx < 0 && x <= targetX) ||
                        (vy > 0 && y >= targetY) || (vy < 0 && y <= targetY)) {
                    System.out.println("Arrow " + arrowId + " removed: reached bounds or target at (" + x + ", " + y + ")");
                    layer.getChildren().remove(view);
                    stop();
                    isAnimating = false;
                    if (skeleton != null) {
                        skeleton.clearActiveArrow();
                    }
                    return;
                }

                // Debug: drawing the boundaries of the arrow and the player
                System.out.println("Arrow bounds: " + view.getBoundsInParent());
                System.out.println("Player bounds: " + player.getBounds());

                if (view.getBoundsInParent().intersects(player.getBounds())) {
                    System.out.println("Arrow " + arrowId + " hit player at (" + x + ", " + y + ")");
                    player.takeDamage(DAMAGE);
                    layer.getChildren().remove(view);
                    stop();
                    isAnimating = false;
                    if (skeleton != null) {
                        skeleton.clearActiveArrow();
                    }
                }
            }
        }.start();
    }
}