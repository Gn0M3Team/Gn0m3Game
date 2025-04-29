package com.gnome.gnome.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserStatistics {
    private int id;
    private String username;
    private int totalMapsPlayed;
    private int totalDeaths;
    private int totalMonstersKilled;
    private int totalChestsOpened;

    public UserStatistics(String username, int totalMapsPlayed, int totalDeaths, int totalMonstersKilled, int totalChestsOpened) {
        this.username = username;
        this.totalMapsPlayed = totalMapsPlayed;
        this.totalDeaths = totalDeaths;
        this.totalMonstersKilled = totalMonstersKilled;
        this.totalChestsOpened = totalChestsOpened;
    }
}
