package com.gnome.gnome.game;

import com.gnome.gnome.player.Player;

public class EndlessStatsTracker {
    private static EndlessStatsTracker instance;

    private int totalCoins;
    private int totalScore;
    private int totalChests;
    private int totalKills;

    private EndlessStatsTracker() {
        reset();
    }

    public static EndlessStatsTracker getInstance() {
        if (instance == null) instance = new EndlessStatsTracker();
        return instance;
    }

    public void accumulateFrom(Player player) {
        totalCoins += (int) player.getPlayerCoins();
        totalScore += player.getScore();
        totalChests += player.getCountOfOpenedChest();
        totalKills += player.getCountOfKilledMonsters();
    }

    public void reset() {
        totalCoins = 0;
        totalScore = 0;
        totalChests = 0;
        totalKills = 0;
    }
}

