package com.gnome.gnome.models;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Map {
    private int id;
    private String username;
    private int[][] mapData;
    private int scoreVal;
    private String mapNameEng;
    private String mapNameSk;
    private int level;

    public Map(String username, int[][] mapData, int scoreVal, String mapNameEng, String mapNameSk, int level) {
        this.username = username;
        this.mapData = mapData;
        this.scoreVal = scoreVal;
        this.mapNameEng = mapNameEng;
        this.mapNameSk = mapNameSk;
        this.level = level;
    }
}
