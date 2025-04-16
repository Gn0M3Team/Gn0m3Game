package com.gnome.gnome.editor.utils;

import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Enum representing different types of objects that can appear on the map grid.
 * Each type is associated with a tile image and an integer value.
 */
@Getter
@AllArgsConstructor
public enum TypeOfObjects {
    /** Empty tile */
    EMPTY("tile_0.png", 0),

    /** Mountain tile */
    MOUNTAIN("tile_2.png", 1),

    /** Demon enemy tile (uses negative ID) */
    DEMON("tile_125.png", -1),

    /** Butterfly enemy tile (uses negative ID) */
    BUTTERFLY("tile_125.png", -2),

    /** Goblin enemy tile (uses negative ID) */
    GOBLIN("tile_123.png", -3),

    /** Scorpion enemy tile (uses negative ID) */
    SCORPION("tile_125.png", -4),

    /** Skeleton enemy tile (uses negative ID) */
    SKELETON("tile_125.png", -5),

    /** Tree tile */
    TREE("tile_54.png", 2),

    /** Rock tile (uses same image as mountain) */
    ROCK("tile_2.png", 3),

    /** River tile */
    RIVER("tile_207.png", 4),

    /** Villager character tile */
    VILLAGER("tile_126.png", 5),

    /** Merchant character tile */
    MERCHANT("tile_129.png", 6),

    /** Dungeon floor tile */
    FLOOR("tile_60.png", 7),

    /** First variant of dungeon wall */
    WALL_ONE("tile_742.png", 8),

    /** Second variant of dungeon wall */
    WALL_TWO("tile_797.png", 9),

    /** Hatch or exit tile */
    HATCH("tile_297.png", 10);

    /** File name of the image representing the object */
    private final String imageName;

    /** Integer value associated with the object type */
    private final int value;

     * Constructor for object type.
     *
     * @param imageName the filename of the tile image
     * @param value     the numeric value representing this type in the map grid
     */
    TypeOfObjects(String imageName, int value) {
        this.imageName = imageName;
        this.value = value;
    }

    /**
     * Returns the full image path of the tile image for this object type.
     *
     * @return the path to the image resource
     */
    public String getImagePath() {
        return "/com/gnome/gnome/images/tiles/" + imageName;
    }

    /**
     * Returns the corresponding TypeOfObjects based on the given integer value.
     * Defaults to {@link #EMPTY} if the value does not match any known type.
     *
     * @param value the integer value from the map grid
     * @return the matching TypeOfObjects enum constant
     */
    public static TypeOfObjects fromValue(int value) {
        return switch (value) {
            case 0 -> EMPTY;
            case 1 -> MOUNTAIN;
            case -1 -> DEMON;
            case -2 -> BUTTERFLY;
            case -3 -> GOBLIN;
            case -4 -> SCORPION;
            case -5 -> SKELETON;
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
