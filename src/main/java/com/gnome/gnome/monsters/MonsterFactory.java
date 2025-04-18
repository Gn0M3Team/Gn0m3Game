package com.gnome.gnome.monsters;

import com.gnome.gnome.monsters.types.*;

public class MonsterFactory {
    public enum MonsterType {
        DEMON,
        SCORPION,
        GOBLIN,
        BUTTERFLY,
        SKELETON,
    }

    /**
     * Creates a monster of the given type at the specified position.
     */
    public static Monster createMonster(MonsterType type, int startX, int startY) {
        return switch (type) {
            case DEMON -> new Demon(startX, startY);
            case SCORPION -> new Scorpion(startX, startY);
            case GOBLIN -> new Goblin(startX, startY);
            case BUTTERFLY -> new Butterfly(startX, startY);
            case SKELETON -> new Skeleton(startX, startY);
            default -> throw new IllegalArgumentException("Unknown monster type: " + type);
        };
    }
}
