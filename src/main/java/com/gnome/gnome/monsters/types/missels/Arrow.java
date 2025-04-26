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

    @Setter
    private double dynamicTileSize;


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
        // The rectangle is 30x30 pixels for simplicity
        view = new Rectangle(30, 30);

        // Generate a random color for the rectangle (for debugging purposes)
        // In a real game, this would likely be an image of an arrow instead of a colored rectangle
        Random rand = new Random();
        // Set the fill color of the rectangle to a random RGB color
        view.setFill(Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
    }

    public void setDynamicTileSize(double dynamicTileSize) {
        this.dynamicTileSize = dynamicTileSize;
        view.setWidth(dynamicTileSize * 0.6);
        view.setHeight(dynamicTileSize * 0.6);
    }

    /**
     * Updates the camera offset for the arrow so it can be rendered correctly relative to the camera's viewport.
     * This method converts the arrow's absolute position (in tiles) to pixel coordinates on the screen, taking into account the camera's position.
     *
     * @param cameraStartCol The starting column of the camera's viewport (the leftmost column currently visible).
     * @param cameraStartRow The starting row of the camera's viewport (the topmost row currently visible).
     */
    public void updateCameraOffset(int cameraStartCol, int cameraStartRow) {
        this.cameraStartCol = cameraStartCol;
        this.cameraStartRow = cameraStartRow;

        if (dynamicTileSize == 0) return;

        double viewSize = dynamicTileSize * 0.6;
        double offset = (dynamicTileSize - viewSize) / 2.0;

        double pixelX = (x - cameraStartCol) * dynamicTileSize + offset;
        double pixelY = (y - cameraStartRow) * dynamicTileSize + offset;

        view.setTranslateX(pixelX);
        view.setTranslateY(pixelY);
        view.setWidth(viewSize);
        view.setHeight(viewSize);
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

        // Very important: set initial dynamic size BEFORE adding to layer
        double viewSize = dynamicTileSize * 0.6;
        view.setWidth(viewSize);
        view.setHeight(viewSize);

        // Correct starting pixel position:
        double offset = (dynamicTileSize - viewSize) / 2.0;
        double pixelX = (x - cameraStartCol) * dynamicTileSize + offset;
        double pixelY = (y - cameraStartRow) * dynamicTileSize + offset;
        view.setTranslateX(pixelX);
        view.setTranslateY(pixelY);

        // Now add the view to scene
        layer.getChildren().add(view);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                x += vx;
                y += vy;

                double pixelX = (x - cameraStartCol) * dynamicTileSize + offset;
                double pixelY = (y - cameraStartRow) * dynamicTileSize + offset;
                view.setTranslateX(pixelX);
                view.setTranslateY(pixelY);

                double paneWidth = layer.getWidth();
                double paneHeight = layer.getHeight();

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