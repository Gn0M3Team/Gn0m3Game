package com.gnome.gnome.models;


import com.gnome.gnome.MainApplication;
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
    private int timesPlayed;
    private int timesCompleted;

    public Map(String username, int[][] mapData, int scoreVal, String mapNameEng, String mapNameSk, int level) {
        this.username = username;
        this.mapData = mapData;
        this.scoreVal = scoreVal;
        this.mapNameEng = mapNameEng;
        this.mapNameSk = mapNameSk;
        this.level = level;
        this.timesPlayed = 0;
        this.timesCompleted = 0;
    }

    public String getName(){
        if (MainApplication.lang == 'E') {
            return mapNameEng;
        }
        else{
            return mapNameSk;
        }
    }
}
