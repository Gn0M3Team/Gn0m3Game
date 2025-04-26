package com.gnome.gnome.models;

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
    private double health;
    private double attack;
    private double radius;
    private int score_val;
    private double cost;
    private String img;
}
