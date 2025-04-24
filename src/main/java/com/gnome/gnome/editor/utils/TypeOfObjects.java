package com.gnome.gnome.editor.utils;

import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Enum representing different types of objects that can appear on the map grid.
 * Each type is associated with a tile image and an integer value.
 */
@Getter
//@AllArgsConstructor
public enum TypeOfObjects {
    /** Empty tile */
    EMPTY("tile_0.png", 0),

    /** Mountain tile */
    MOUNTAIN("tile_2.png", -1),

    /** Demon enemy tile (uses negative ID) */
    DEMON("tile_125.png", 1),

    /** Butterfly enemy tile (uses negative ID) */
    BUTTERFLY("tile_276.png", 4),

    /** Goblin enemy tile (uses negative ID) */
    GOBLIN("tile_127.png", 3),

    /** Scorpion enemy tile (uses negative ID) */
    SCORPION("tile_269.png", 2),

    /** Skeleton enemy tile (uses negative ID) */
    SKELETON("tile_80.png", 5),

    /** Tree tile */
    TREE("tile_54.png", -2),


    /** Rock tile (uses same image as mountain) */
    ROCK("tile_103.png", -3),

    /** River tile */
    RIVER("tile_253.png", -4),

    /** Bookshelf */
    BOOKSHELF("tile_299.png", -5),

    /** Cactus tile */
    CACTUS("tile_56.png", -6),

    /** Cactus tile */
    BLOCKED("tile_121.png", -7),

    /** Table tile */
    TABLE("tile_344.png", -8),

    /** Web tile */
    WEB("tile_737.png", -9),

    /** Dungeon floor tile */
    FLOOR("tile_60.png", 7),

    /** Hatch or exit tile */
    HATCH("tile_297.png", 8),

    /** First variant of dungeon wall */
    WALL_1("tile_893.png", 9),

    /** First variant of dungeon wall */
    WALL_2("tile_692.png", 10),

    /** First variant of dungeon wall */
    WALL_3("tile_699.png", 11),

    /** First variant of dungeon wall */
    WALL_4("tile_798.png", 12),

    /** First variant of dungeon wall */
    WALL_5("tile_893.png", 13);

    /** File name of the image representing the object */
    private final String imageName;

    /** Integer value associated with the object type */
    private final int value;

     /* Constructor for object type.
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
            case -1 -> MOUNTAIN;
            case -2 -> TREE;
            case -3 -> ROCK;
            case -4 -> RIVER;
            case -5 -> BOOKSHELF;
            case 0 -> EMPTY;
            case 1 -> DEMON;
            case 4 -> BUTTERFLY;
            case 3 -> GOBLIN;
            case 2 -> SCORPION;
            case 5 -> SKELETON;
            case 7 -> FLOOR;
            case 8 -> HATCH;
            case 9 -> WALL_1;
            case 10 -> WALL_2;
            case 11 -> WALL_3;
            case 12 -> WALL_4;
            case 13 -> WALL_5;
            default -> EMPTY;
        };
    }

    /**
     * Converts a string to its corresponding {@link TypeOfObjects} enum constant, ignoring case.
     * Returns {@link TypeOfObjects#EMPTY} if the input does not match any constant.
     *
     * @param typeName the name of the type (case-insensitive)
     * @return the matching TypeOfObjects, or EMPTY if not found
     */
    public static TypeOfObjects getTypeFromString(String typeName) {
        try {
            return TypeOfObjects.valueOf(typeName.toUpperCase()); // Преобразуем в верхний регистр для поиска
        } catch (IllegalArgumentException e) {
            return TypeOfObjects.EMPTY;
        }
    }

}
