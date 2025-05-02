package com.gnome.gnome.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.movements.RandomMovement;
import com.gnome.gnome.monsters.types.missels.Arrow;
import com.gnome.gnome.player.Player;

/**
 * Represents a skeleton monster that can attack the player by shooting arrows.
 * <p>
 * The skeleton has a cooldown between attacks and ensures that only one arrow is active at a time.
 * The attack is only initiated if the skeleton is within the camera viewport and has no active arrows.
 * </p>
 */
public class Skeleton extends Monster {
    private static final double ARROW_SPEED = 2;
    private static final long ATTACK_COOLDOWN_NS = 3_000_000_000L;
    private static final long POST_REMOVAL_DELAY_NS = 1_000_000_000L;
    private static final double MAX_ARROW_RANGE = 5;

    private long lastAttackTime = 0;
    private long lastArrowRemovedTime = 0;
    private Arrow activeArrow = null;

    /**
     * Constructs a new skeleton monster with predefined attributes and random movement.
     *
     * @param startX the X grid position of the skeleton
     * @param startY the Y grid position of the skeleton
     */
    public Skeleton(int startX, int startY, com.gnome.gnome.models.Monster dbMonster) {
        super(dbMonster.getAttack(),
                dbMonster.getHealth(),
                dbMonster.getCost(),
                dbMonster.getRadius(),
                dbMonster.getName(),
                dbMonster.getName_sk(),
                startX,
                startY,
                TypeOfObjects.SKELETON.getValue(),
                new RandomMovement(),
                TypeOfObjects.SKELETON.getImagePathForMonsters(),
                "/com/gnome/gnome/images/monsters/hitGif/demon_damaged.gif",
                "/com/gnome/gnome/effects/red_monster.gif",
                dbMonster.getScore_val());
    }

    public boolean canShootArrow() {
        long now = System.nanoTime();
        return (now - lastAttackTime >= ATTACK_COOLDOWN_NS) && (activeArrow == null) && (now - lastArrowRemovedTime >= POST_REMOVAL_DELAY_NS);
    }

    public boolean hasActiveArrow() {
        return activeArrow != null;
    }

    public void setActiveArrow(Arrow arrow) {
        this.activeArrow = arrow;
        this.lastAttackTime = System.nanoTime();
    }

    public Arrow shootArrowTowards(Player player) {
        double startX = getX() + 0.5;
        double startY = getY() + 0.5;
        double targetX = player.getX() + 0.5;
        double targetY = player.getY() + 0.5;

        double dx = targetX - startX;
        double dy = targetY - startY;
        double dist = Math.hypot(dx, dy);
        if (dist == 0) return null;

        double vx = (dx / dist) * ARROW_SPEED;
        double vy = (dy / dist) * ARROW_SPEED;

        double endX = startX + (dx / dist) * MAX_ARROW_RANGE;
        double endY = startY + (dy / dist) * MAX_ARROW_RANGE;

        Arrow arrow = new Arrow(startX, startY, vx, vy, endX, endY);
        arrow.setSkeleton(this);
        return arrow;
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