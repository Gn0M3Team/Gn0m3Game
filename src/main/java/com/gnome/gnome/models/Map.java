package com.gnome.gnome.models;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Map {
    private String username;
    private int[][] mapData;
    private int scoreVal;
    private String mapNameEng;
    private String mapNameSk;
    private int level;
}
