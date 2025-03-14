package com.gnome.gnome.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Monster {
    private int id;
    private String name;
    private String details;
    private double health;
    private double attack;
    private double radius;
    private int score_val;
    private double cost;
}
