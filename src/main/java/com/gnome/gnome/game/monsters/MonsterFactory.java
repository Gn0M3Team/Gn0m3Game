package com.gnome.gnome.game.monsters;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.game.monsters.types.*;
import com.gnome.gnome.models.Monster;

public class MonsterFactory {

    /**
     * Creates a monster of the given type at the specified position.
     */
    public static GameMonster createMonster(TypeOfObjects type, int startX, int startY, Monster dbMonster) {
        return switch (type) {
            case DEMON -> new Demon(startX, startY, dbMonster);
            case SCORPION -> new Scorpion(startX, startY, dbMonster);
            case GOBLIN -> new Goblin(startX, startY, dbMonster);
            case BUTTERFLY -> new Butterfly(startX, startY, dbMonster);
            default -> throw new IllegalArgumentException("Unknown monster type: " + type);
        };
    }
}
