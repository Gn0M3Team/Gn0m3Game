package com.gnome.gnome.editor.utils;

import com.gnome.gnome.editor.utils.TypeOfObjects;

public class MapValidator {

    public enum ValidationStatus {
        SUCCESS, EMPTY_MAP, MISSING_FINISH, INVALID_START
    }

    public static class ValidationResult {
        public final ValidationStatus status;
        public final String message;

        public ValidationResult(ValidationStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        public boolean isSuccess() {
            return status == ValidationStatus.SUCCESS;
        }
    }

    /**
     * Validates the map grid for required conditions:
     * - Not empty
     * - Contains at least one finish
     * - Contains exactly one start
     *
     * @param mapGrid the 2D map array
     * @return ValidationResult with success or specific failure reason
     */
    public static ValidationResult validate(int[][] mapGrid) {
        if (mapGrid == null || mapGrid.length == 0) {
            return new ValidationResult(ValidationStatus.EMPTY_MAP, "Map grid is empty.");
        }

        boolean hasObjects = false;
        boolean hasFinish = false;
        int startCount = 0;

        for (int[] row : mapGrid) {
            for (int cell : row) {
                if (cell != 0) {
                    hasObjects = true;
                }
                if (cell == TypeOfObjects.FINISH_POINT.getValue()) {
                    hasFinish = true;
                }
                if (cell == TypeOfObjects.START_POINT.getValue()) {
                    startCount++;
                }
            }
        }

        if (!hasObjects) {
            return new ValidationResult(ValidationStatus.EMPTY_MAP, "Map must contain at least one object.");
        }

        if (!hasFinish) {
            return new ValidationResult(ValidationStatus.MISSING_FINISH, "Map must contain a finish point.");
        }

        if (startCount != 1) {
            return new ValidationResult(ValidationStatus.INVALID_START,
                    "Map must contain exactly one start point (found: " + startCount + ").");
        }

        return new ValidationResult(ValidationStatus.SUCCESS, "Map is valid.");
    }
}
