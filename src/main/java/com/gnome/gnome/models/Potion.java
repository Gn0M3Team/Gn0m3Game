package com.gnome.gnome.models;

import com.gnome.gnome.MainApplication;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents a Potion entity in the game, mirroring the structure of the "Potion" table in the database.
 * The id is auto-generated by the database and updated after insertion.
 */
@Data
@AllArgsConstructor
public class Potion {
    private int id;
    private int scoreVal;
    private float cost;
    private String nameEng;
    private String nameSk;
    private String detailsEng;
    private String detailsSk;
    private String img1;
    private String img2;

    public String getDetails() {
        if (MainApplication.getLang() == 'E')
            return detailsEng;
        else
            return detailsSk;
    }
    public String getName() {
        if (MainApplication.getLang() == 'E')
            return nameEng;
        else
            return nameSk;
    }
}
