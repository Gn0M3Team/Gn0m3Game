package com.gnome.gnome.models;

import com.gnome.gnome.MainApplication;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Monster {
    private int id;
    private String name;
    private String name_sk;
    private String details;
    private String details_sk;
    private double attack;
    private double health;
    private double radius;
    private int score_val;
    private double cost;
    private String img;

    public String getDetails() {
        if (MainApplication.lang == 'E')
            return details;
        else
            return details_sk;
    }
    public String getName() {
        if (MainApplication.lang == 'E')
            return name;
        else
            return name_sk;
    }
}
