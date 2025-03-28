package com.gnome.gnome.editor.utils;

import javafx.scene.paint.Color;
import lombok.Getter;

@Getter
public enum TypeOfObjects {
    EMPTY(Color.GREEN, 0),
    MOUNTAIN(Color.RED, 1),
    GOBLIN(Color.BLUE, -1),
    DRAGON(Color.BEIGE, -2),
    TREE(Color.BISQUE, 2),
    ROCK(Color.GRAY, 3),
    RIVER(Color.BLANCHEDALMOND, 4),
    VILLAGER(Color.BLUEVIOLET, 5),
    MERCHANT(Color.BROWN, 6),
    FLOOR(Color.SADDLEBROWN, 7),
    WALL_ONE(Color.CHARTREUSE, 8),
    WALL_TWO(Color.CORNSILK, 9),
    HATCH(Color.ALICEBLUE, 10);

    private final Color color;
    private final int value;

    TypeOfObjects(Color color, int value) {
        this.color = color;
        this.value = value;
    }

    /** Finds a tile type by its integer value */
    public static TypeOfObjects fromValue(int value) {
        return switch (value) {
            case 0 -> EMPTY;
            case 1 -> MOUNTAIN;
            case -1 -> GOBLIN;
            case -2 -> DRAGON;
            case 2 -> TREE;
            case 3 -> ROCK;
            case 4 -> RIVER;
            case 5 -> VILLAGER;
            case 6 -> MERCHANT;
            case 7 -> FLOOR;
            case 8 -> WALL_ONE;
            case 9 -> WALL_TWO;
            case 10 -> HATCH;
            default -> EMPTY;
        };
    }
}
