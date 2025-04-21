package com.gnome.gnome.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.movements.RandomMovement;
import com.gnome.gnome.monsters.types.missels.Arrow;

/**
 * Represents a skeleton monster that can attack the player by shooting arrows.
 * <p>
 * The skeleton has a cooldown between attacks and ensures that only one arrow is active at a time.
 * The attack is only initiated if the skeleton is within the camera viewport and has no active arrows.
 * </p>
 */
public class Skeleton extends Monster {
    /**
     * The speed of the arrow in tiles per second.
     */
    private static final double ARROW_SPEED = 2;
    /**
     * Time interval (in nanoseconds) required between attacks.
     */
    private static final long ATTACK_COOLDOWN_NS = 3_000_000_000L;
    /**
     * The timestamp of the last attack.
     */
    private long lastAttackTime = 0;
    /**
     * Currently active arrow shot by the skeleton.
     */
    private Arrow activeArrow = null;

    /**
     * Time when the last arrow was removed.
     */
    private long lastArrowRemovedTime = 0;
    /**
     * Delay after removing the arrow before the skeleton can attack again.
     */
    private static final long POST_REMOVAL_DELAY_NS = 1_000_000_000L;
    /**
     * Maximum distance the arrow can travel (in tiles).
     */
    private static final double MAX_ARROW_RANGE = 5;

    /**
     * Constructs a new skeleton monster with predefined attributes and random movement.
     *
     * @param startX the X grid position of the skeleton
     * @param startY the Y grid position of the skeleton
     */
    public Skeleton(int startX, int startY) {
        super(30, 80, 50, 4, "Skeleton", "Skeleton", startX, startY, TypeOfObjects.SKELETON.getValue(), new RandomMovement());
    }

    /**
     * Attempts to perform a ranged attack by launching an arrow toward the player.
     * The arrow will only be fired if the cooldown has expired, no active arrow exists,
     * and the skeleton is within the camera's viewport.
     *
     * @param cameraStartCol the first visible column of the viewport
     * @param cameraStartRow the first visible row of the viewport
     * @param playerGridX    the player's X grid position
     * @param playerGridY    the player's Y grid position
     * @return a new {@link Arrow} object if the attack was successful, or {@code null} otherwise
     */
    public Arrow attack(int cameraStartCol, int cameraStartRow, int playerGridX, int playerGridY) {
        long now = System.nanoTime();
        if (now - lastAttackTime < ATTACK_COOLDOWN_NS) {
            System.out.println("Skeleton at (" + getX() + ", " + getY() + ") attack on cooldown");
            return null;
        }

        if (activeArrow != null) {
            System.out.println("Skeleton at (" + getX() + ", " + getY() + ") has an active arrow, skipping attack");
            return null;
        }

        if (now - lastAttackTime < POST_REMOVAL_DELAY_NS) {
            System.out.println("Skeleton at (" + getX() + ", " + getY() + ") waiting for post-removal delay");
            return null;
        }

        int relativeX = getX() - cameraStartCol;
        int relativeY = getY() - cameraStartRow;
        if (relativeX < 0 || relativeX >= 15 || relativeY < 0 || relativeY >= 15) {
            System.out.println("Skeleton at (" + getX() + ", " + getY() + ") is outside viewport, skipping attack");
            return null;
        }

        // Starting position in tiles (absolute coordinates)
        double startX = getX() + 0.5;
        double startY = getY() + 0.5;

        // Player position in absolute coordinates
        double targetX = playerGridX + 0.5;
        double targetY = playerGridY + 0.5;

        // Calculate the direction in tiles
        double dx = targetX - startX;
        double dy = targetY - startY;
        double dist = Math.hypot(dx, dy);
        if (dist == 0) {
            System.out.println("Player is at the same position as skeleton, skipping attack");
            return null;
        }

        // Calculate the speed in tiles/second
        double vx = (dx / dist) * ARROW_SPEED;
        double vy = (dy / dist) * ARROW_SPEED;

        // End point - at a distance of 5 tiles from the starting position
        double endX = startX + (dx / dist) * MAX_ARROW_RANGE;
        double endY = startY + (dy / dist) * MAX_ARROW_RANGE;

        System.out.println("Skeleton at (" + getX() + ", " + getY() + ") shooting arrow from (" + startX + ", " + startY + ") towards (" + endX + ", " + endY + ")");
        lastAttackTime = now;
        activeArrow = new Arrow(startX, startY, vx, vy, endX, endY);
        activeArrow.updateCameraOffset(cameraStartCol, cameraStartRow);
        return activeArrow;
    }

    /**
     * Clears the reference to the currently active arrow after it is removed from the scene.
     * This method also updates the internal timestamp to enforce the post-removal delay.
     */
    public void clearActiveArrow() {
        activeArrow = null;
        lastArrowRemovedTime = System.nanoTime();
    }
}
