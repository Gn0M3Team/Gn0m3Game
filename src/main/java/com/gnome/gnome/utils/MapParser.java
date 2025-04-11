package com.gnome.gnome.utils;

/**
 * A utility class for parsing and converting between a 2D ArrayList of Integers (representing a game map)
 * and its string representation for database storage.
 * The string format uses ';' to separate rows and ',' to separate values within each row.
 */
public class MapParser {

    /**
     * Converts a 2D int array to a string representation.
     * Each row is separated by ';' and values within a row are separated by ','.
     *
     * @param mapData the 2D int array representing the game map
     * @return a string representation of the map, or an empty string if mapData is null/empty
     * @throws IllegalArgumentException if a row contains null values
     */
    public static String convertMapToString(int[][] mapData) {
        if (mapData == null || mapData.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mapData.length; i++) {
            if (mapData[i] == null) {
                throw new IllegalArgumentException("Row at index " + i + " is null");
            }

            for (int j = 0; j < mapData[i].length; j++) {
                sb.append(mapData[i][j]);
                if (j < mapData[i].length - 1) {
                    sb.append(",");
                }
            }
            if (i < mapData.length - 1) {
                sb.append(";");
            }
        }
        return sb.toString();
    }

    /**
     * Converts a string representation back to a 2D int array.
     * Assumes rows are separated by ';' and values within rows by ','.
     * Empty or invalid strings result in an empty array.
     *
     * @param mapString the string representation of the map (e.g., "0,1,2;3,4,5")
     * @return a 2D int array representing the game map
     * @throws NumberFormatException if a value cannot be parsed to an integer
     * @throws IllegalArgumentException if the string format is invalid
     */
    public static int[][] convertStringToMap(String mapString) {
        if (mapString == null || mapString.trim().isEmpty()) {
            return new int[0][0];
        }

        String[] rows = mapString.split(";");
        int[][] mapData = new int[rows.length][];
        for (int i = 0; i < rows.length; i++) {
            if (rows[i].trim().isEmpty()) {
                continue; // Skip empty rows, though this might need adjustment based on your needs
            }
            String[] values = rows[i].split(",");
            mapData[i] = new int[values.length];
            for (int j = 0; j < values.length; j++) {
                if (values[j].trim().isEmpty()) {
                    throw new IllegalArgumentException("Empty value found in row: " + rows[i]);
                }
                mapData[i][j] = Integer.parseInt(values[j].trim());
            }
        }
        return mapData;
    }
}