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

    /** Start/Finish category */
    START_POINT("tile_538.png", 1), //tile_676
    FINISH_POINT("tile_297.png", 2),

    /** Monsters category */
    SKELETON("tile_80.png", -1),
    DEMON("tile_125.png", -2),
    GOBLIN("tile_127.png", -3),
    SCORPION("tile_269.png", -4),
    BUTTERFLY("tile_276.png", -5),


    /** Props category */
    FLOOR("tile_60.png", 11),
    BLOCKED("tile_121.png", 12),
    BOOKSHELF("tile_299.png", 13),
    TABLE("tile_344.png", 14),


    /** Environment category */
    TREE("tile_54.png", 21),
    ROCK("tile_103.png", 22),
    RIVER("tile_253.png", 23),
    CACTUS("tile_56.png", 24),
    WEB("tile_737.png", 25),
    STUMP("tile_312.png", 26),
    MOUNTAIN("tile_2.png", 27),


    /** Walls category */
    WALL_1("tile_741.png", 30),
    WALL_2("tile_692.png", 31),
    WALL_3("tile_798.png", 32),
    WALL_4("tile_797.png", 33),
    WALL_5("tile_149.png", 34),
    WALL_6("tile_794.png", 35),
    WALL_7("tile_699.png", 36),
    WALL_8("tile_637.png", 37),
    WALL_9("tile_843.png", 38),
    WALL_10("tile_844.png", 38),
    WALL_11("tile_845.png", 38),


    /** Chest category */
    CHEST_1("tile_302.png", 60),
    CHEST_3("tile_348.png", 61),
    CHEST_2("tile_701.png", 62),
    CHEST_4("tile_354.png", 63),
    CHEST_5("tile_355.png", 64),
    CHEST_6("tile_451.png", 65),
    CHEST_7("tile_513.png", 66),


    /** Doors category */
    DOOR_1("tile_444.png", 80),
    DOOR_2("tile_445.png", 81),
    DOOR_3("tile_446.png", 82),
    DOOR_4("tile_447.png", 83),
    DOOR_5("tile_448.png", 84),
    DOOR_6("tile_449.png", 85),
    DOOR_7("tile_450.png", 86),
    DOOR_8("tile_451.png", 87),
    DOOR_9("tile_799.png", 88),
    DOOR_10("tile_641.png", 89),
    DOOR_11("tile_793.png", 90),
    DOOR_12("tile_639.png", 91),
    DOOR_13("tile_839.png", 92);

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
            // Special tiles
            case 0   -> EMPTY;
            case 1   -> START_POINT;
            case 2   -> FINISH_POINT;

            // Monsters
            case -1  -> SKELETON;
            case -2  -> DEMON;
            case -3  -> GOBLIN;
            case -4  -> SCORPION;
            case -5  -> BUTTERFLY;

            // Props
            case 11  -> FLOOR;
            case 12  -> BLOCKED;
            case 13  -> BOOKSHELF;
            case 14  -> TABLE;

            // Environment
            case 21  -> TREE;
            case 22  -> ROCK;
            case 23  -> RIVER;
            case 24  -> CACTUS;
            case 25  -> WEB;
            case 26  -> STUMP;
            case 27  -> MOUNTAIN;

            // Walls
            case 30  -> WALL_1;
            case 31  -> WALL_2;
            case 32  -> WALL_3;
            case 33  -> WALL_4;
            case 34  -> WALL_5;
            case 35  -> WALL_6;
            case 36  -> WALL_7;
            case 37  -> WALL_8;
            case 38  -> WALL_9;
            case 39  -> WALL_10;
            case 40  -> WALL_11;

            // Chests
            case 60  -> CHEST_1;
            case 61  -> CHEST_3;
            case 62  -> CHEST_2;
            case 63  -> CHEST_4;
            case 64  -> CHEST_5;
            case 65  -> CHEST_6;
            case 66  -> CHEST_7;

            // Doors
            case 80  -> DOOR_1;
            case 81  -> DOOR_2;
            case 82  -> DOOR_3;
            case 83  -> DOOR_4;
            case 84  -> DOOR_5;
            case 85  -> DOOR_6;
            case 86  -> DOOR_7;
            case 87  -> DOOR_8;
            case 88  -> DOOR_9;
            case 89  -> DOOR_10;
            case 90  -> DOOR_11;
            case 91  -> DOOR_12;
            case 92  -> DOOR_13;

            // Default fallback
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
