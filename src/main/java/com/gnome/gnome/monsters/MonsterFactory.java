package com.gnome.gnome.monsters;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.types.*;

public class MonsterFactory {

    /**
     * Creates a monster of the given type at the specified position.
     */
    // TODO: Implement two more columns for monster in db, and after that when we create monsters, we use for images data from db.
    public static Monster createMonster(TypeOfObjects type, int startX, int startY, com.gnome.gnome.models.Monster dbMonster) {
        return switch (type) {
            case DEMON -> new Demon(startX, startY, dbMonster);
            case SCORPION -> new Scorpion(startX, startY, dbMonster);
            case GOBLIN -> new Goblin(startX, startY, dbMonster);
            case BUTTERFLY -> new Butterfly(startX, startY, dbMonster);
            case SKELETON -> new Skeleton(startX, startY, dbMonster);
            default -> throw new IllegalArgumentException("Unknown monster type: " + type);
        };
    }
}
