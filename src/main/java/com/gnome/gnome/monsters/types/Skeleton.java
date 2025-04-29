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
        super(30,
                80,
                50,
                4,
                "Skeleton",
                "Skeleton",
                startX,
                startY,
                TypeOfObjects.SKELETON.getValue(),
                new RandomMovement(),
                TypeOfObjects.SKELETON.getImagePath(),
                "/com/gnome/gnome/effects/demon_damaged.gif",
                "/com/gnome/gnome/effects/red_monster.gif");
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
        // Get the current time in nanoseconds to check cooldowns
        long now = System.nanoTime();

        // Check if the attack cooldown has expired
        // If less than 3 seconds (ATTACK_COOLDOWN_NS) have passed since the last attack, skip the attack
        if (now - lastAttackTime < ATTACK_COOLDOWN_NS) {
            System.out.println("Skeleton at (" + getX() + ", " + getY() + ") attack on cooldown");
            return null;
        }

        // Check if there is already an active arrow in flight
        // The skeleton can only have one arrow active at a time, so skip the attack if an arrow exists
        if (activeArrow != null) {
            System.out.println("Skeleton at (" + getX() + ", " + getY() + ") has an active arrow, skipping attack");
            return null;
        }

        // Check if the post-removal delay has expired
        // If less than 1 second (POST_REMOVAL_DELAY_NS) has passed since the last arrow was removed, skip the attack
        if (now - lastArrowRemovedTime < POST_REMOVAL_DELAY_NS) {
            System.out.println("Skeleton at (" + getX() + ", " + getY() + ") waiting for post-removal delay");
            return null;
        }

        // Check if the skeleton is within the camera's viewport (15x15 tiles)
        // Calculate the skeleton's position relative to the camera's viewport:
        // - relativeX = skeleton's X position - camera's starting column
        // - relativeY = skeleton's Y position - camera's starting row
        int relativeX = getX() - cameraStartCol;
        int relativeY = getY() - cameraStartRow;
        // If the skeleton's relative position is outside the viewport (0 to 14 tiles), skip the attack
        if (relativeX < 0 || relativeX >= 15 || relativeY < 0 || relativeY >= 15) {
            System.out.println("Skeleton at (" + getX() + ", " + getY() + ") is outside viewport, skipping attack");
            return null;
        }

        // Calculate the starting position of the arrow in absolute coordinates (in tiles)
        // Add 0.5 to the skeleton's position to place the arrow at the center of the tile
        double startX = getX() + 0.5; // Center of the skeleton's tile on the X-axis
        double startY = getY() + 0.5; // Center of the skeleton's tile on the Y-axis

        // Calculate the target position (the player's position) in absolute coordinates
        // Add 0.5 to the player's position to target the center of the player's tile
        double targetX = playerGridX + 0.5; // Center of the player's tile on the X-axis
        double targetY = playerGridY + 0.5; // Center of the player's tile on the Y-axis

        // Calculate the direction vector from the skeleton to the player:
        // - dx = difference in X coordinates (targetX - startX)
        // - dy = difference in Y coordinates (targetY - startY)
        double dx = targetX - startX;
        double dy = targetY - startY;
        // Calculate the distance between the skeleton and the player using the Pythagorean theorem (hypotenuse)
        double dist = Math.hypot(dx, dy);
        //  If the distance is 0 (i.e., the player is on the same tile as the skeleton), skip the attack to avoid division by zero
        if (dist == 0) {
            System.out.println("Player is at the same position as skeleton, skipping attack");
            return null;
        }

        // Calculate the velocity of the arrow in tiles per second:
        // - Normalize the direction vector (dx/dist, dy/dist) to get a unit vector (length 1)
        // - Multiply by ARROW_SPEED (2 tiles/second) to set the arrow's speed in the correct direction
        double vx = (dx / dist) * ARROW_SPEED; // X-component of the velocity
        double vy = (dy / dist) * ARROW_SPEED; // Y-component of the velocity

        // Calculate the endpoint of the arrow's trajectory:
        // - The arrow travels a maximum distance of MAX_ARROW_RANGE (5 tiles) in the direction of the player
        // - Scale the direction vector by MAX_ARROW_RANGE to find the endpoint
        double endX = startX + (dx / dist) * MAX_ARROW_RANGE; // X-coordinate of the endpoint
        double endY = startY + (dy / dist) * MAX_ARROW_RANGE; // Y-coordinate of the endpoint

        System.out.println("Skeleton at (" + getX() + ", " + getY() + ") shooting arrow from (" + startX + ", " + startY + ") towards (" + endX + ", " + endY + ")");

        // Update the timestamp of the last attack to enforce the cooldown for the next attack
        lastAttackTime = now;

        // Create a new Arrow object with the calculated trajectory:
        // - startX, startY: Starting position of the arrow
        // - vx, vy: Velocity of the arrow (in tiles per second)
        // - endX, endY: Endpoint of the arrow's trajectory
        activeArrow = new Arrow(startX, startY, vx, vy, endX, endY);

        // Adjust the arrow's position relative to the camera's viewport
        activeArrow.updateCameraOffset(cameraStartCol, cameraStartRow);

        // Return the Arrow object to indicate the attack was successful
        // The game will use this Arrow object to animate its movement and check for collisions with the player
        return activeArrow;
    }

    /**
     * Clears the reference to the currently active arrow after it is removed from the scene.
     * This method also updates the internal timestamp to enforce the post-removal delay.
     */
    public void clearActiveArrow() {
        // Set the activeArrow to null to indicate there is no active arrow in flight
        activeArrow = null;
        // Update the timestamp of when the arrow was removed to enforce the post-removal delay
        lastArrowRemovedTime = System.nanoTime();
    }
}