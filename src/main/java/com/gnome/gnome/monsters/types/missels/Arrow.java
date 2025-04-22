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
        this.x = startX; // Set the arrow's initial X position
        this.y = startY; // Set the arrow's initial Y position

        // Adjust the velocity from tiles per second to tiles per frame, assuming 60 frames per second (FPS)
        // Divide by 60 to convert the velocity for frame-by-frame movement
        this.vx = vx / 60.0; // Horizontal velocity in tiles per frame
        this.vy = vy / 60.0; // Vertical velocity in tiles per frame
        this.targetX = targetX; // Set the target X coordinate
        this.targetY = targetY; // Set the target Y coordinate
        System.out.println("Creating arrow " + arrowId + " at (" + startX + ", " + startY + ") with velocity (" + vx + ", " + vy + ") towards (" + targetX + ", " + targetY + ")");

        // Create a Rectangle to visually represent the arrow
        // The rectangle is 30x30 pixels for simplicity (smaller than a tile, which is typically 50x50 pixels)
        view = new Rectangle(30, 30);

        // Generate a random color for the rectangle (for debugging purposes)
        // In a real game, this would likely be an image of an arrow instead of a colored rectangle
        Random rand = new Random();
        // Set the fill color of the rectangle to a random RGB color
        view.setFill(Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
    }

    /**
     * Updates the camera offset for the arrow so it can be rendered correctly relative to the camera's viewport.
     * This method converts the arrow's absolute position (in tiles) to pixel coordinates on the screen, taking into account the camera's position.
     *
     * @param cameraStartCol The starting column of the camera's viewport (the leftmost column currently visible).
     * @param cameraStartRow The starting row of the camera's viewport (the topmost row currently visible).
     */
    public void updateCameraOffset(int cameraStartCol, int cameraStartRow) {
        this.cameraStartCol = cameraStartCol; // Store the camera's starting column
        this.cameraStartRow = cameraStartRow; // Store the camera's starting row

        // Convert the arrow's absolute position (in tiles) to pixel coordinates relative to the camera's viewport:
        // - Subtract the camera's starting column/row to get the position relative to the viewport
        // - Multiply by TILE_SIZE (e.g., 50 pixels) to convert from tiles to pixels
        double pixelX = (x - cameraStartCol) * TILE_SIZE; // X position in pixels
        double pixelY = (y - cameraStartRow) * TILE_SIZE; // Y position in pixels

        // Set the position of the arrow's visual representation (Rectangle) on the screen
        view.setTranslateX(pixelX); // Set the X position in pixels
        view.setTranslateY(pixelY); // Set the Y position in pixels
    }

    /**
     * Adds the arrow to the scene and animates its movement until it goes out of bounds,
     * reaches the target, or collides with the player.
     *
     * @param layer  the JavaFX pane to which the arrow is added
     * @param player the player object to check for collisions
     */
    public void shoot(Pane layer, Player player) {
        // Check if the arrow is already animating (moving)
        // If it is, skip the shoot operation to prevent multiple animations from running simultaneously
        if (isAnimating) {
            System.out.println("Arrow " + arrowId + " is already animating, skipping shoot");
            return;
        }

        // Set the flag to indicate the arrow is now animating
        isAnimating = true;

        System.out.println("Adding arrow " + arrowId + " to gameObjectsPane at (" + x + ", " + y + ")");

        // Add the arrow's visual representation (Rectangle) to the provided Pane so it appears on the screen
        layer.getChildren().add(view);

        // Create a new AnimationTimer to handle the arrow's movement and interactions frame by frame
        // AnimationTimer runs at approximately 60 FPS, updating the arrow's position each frame
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Update the arrow's absolute position (in tiles) by adding the velocity (tiles/frame)
                x += vx; // Update the X position
                y += vy; // Update the Y position

                // Convert the updated absolute position (in tiles) to pixel coordinates relative to the camera's viewport
                double pixelX = (x - cameraStartCol) * TILE_SIZE; // X position in pixels
                double pixelY = (y - cameraStartRow) * TILE_SIZE; // Y position in pixels

                // Update the position of the arrow's visual representation (Rectangle) on the screen
                view.setTranslateX(pixelX); // Set the new X position in pixels
                view.setTranslateY(pixelY); /// Set the new Y position in pixels
                System.out.println("Arrow " + arrowId + " moving to (" + x + ", " + y + ") in tiles, (" + pixelX + ", " + pixelY + ") in pixels");

                // Get the dimensions of the Pane (viewport) to check if the arrow has gone out of bounds
                double paneWidth = layer.getWidth(); // Width of the Pane in pixels
                double paneHeight = layer.getHeight(); // Height of the Pane in pixels

                // Check if the Pane has valid dimensions (non-zero width and height)
                // If the dimensions are 0, the Pane might not be properly initialized, which could cause issues
                if (paneWidth == 0 || paneHeight == 0) {
                    System.out.println("Warning: gameObjectsPane has invalid size: " + paneWidth + "x" + paneHeight);
                }

                // Check if the arrow should stop moving. The arrow stops if:
                // 1. It goes out of bounds (outside the viewport: pixelX < 0, pixelX > paneWidth, pixelY < 0, or pixelY > paneHeight)
                // 2. It reaches or passes its target position:
                //    - If moving right (vx > 0), stop if x >= targetX
                //    - If moving left (vx < 0), stop if x <= targetX
                //    - If moving down (vy > 0), stop if y >= targetY
                //    - If moving up (vy < 0), stop if y <= targetY
                if (pixelX < 0 || pixelX > paneWidth || pixelY < 0 || pixelY > paneHeight ||
                        (vx > 0 && x >= targetX) || (vx < 0 && x <= targetX) ||
                        (vy > 0 && y >= targetY) || (vy < 0 && y <= targetY)) {
                    System.out.println("Arrow " + arrowId + " removed: reached bounds or target at (" + x + ", " + y + ")");

                    // Remove the arrow's visual representation from the Pane to stop displaying it
                    layer.getChildren().remove(view);

                    // Stop the AnimationTimer to end the animation
                    stop();
                    // Reset the flag to indicate the arrow is no longer animating
                    isAnimating = false;

                    // If the arrow was shot by a skeleton, notify the skeleton that the arrow has been removed
                    if (skeleton != null) {
                        skeleton.clearActiveArrow(); // This allows the skeleton to shoot another arrow
                    }
                    return;
                }

                // Debug: drawing the boundaries of the arrow and the player
                System.out.println("Arrow bounds: " + view.getBoundsInParent());
                System.out.println("Player bounds: " + player.getBounds());

                // Check if the arrow collides with the player by checking if their bounding boxes intersect
                // - view.getBoundsInParent() returns the arrow's bounding box in the parent's coordinate system (the Pane)
                // - player.getBounds() returns the player's bounding box (a 50x50 square in this game)
                if (view.getBoundsInParent().intersects(player.getBounds())) {
                    System.out.println("Arrow " + arrowId + " hit player at (" + x + ", " + y + ")");

                    // Apply damage to the player (DAMAGE=10)
                    player.takeDamage(DAMAGE);
                    // Remove the arrow's visual representation from the Pane to stop displaying it
                    layer.getChildren().remove(view);
                    // Stop the AnimationTimer to end the animation
                    stop();
                    // Reset the flag to indicate the arrow is no longer animating
                    isAnimating = false;
                    // If the arrow was shot by a skeleton, notify the skeleton that the arrow has been removed
                    if (skeleton != null) {
                        skeleton.clearActiveArrow(); // This allows the skeleton to shoot another arrow
                    }
                }
            }
        }.start(); // Start the AnimationTimer to begin the arrow's animation
    }
}