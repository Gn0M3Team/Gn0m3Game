package com.gnome.gnome.game;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.game.component.Chest;
import com.gnome.gnome.game.monsters.GameMonster;
import com.gnome.gnome.models.Armor;
import com.gnome.gnome.models.Monster;
import com.gnome.gnome.models.Weapon;
import com.gnome.gnome.game.monsters.MonsterFactory;
import com.gnome.gnome.game.player.Player;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class GameInitializer {

    public static final int PLAYER_MAX_HEALTH = 100;

    public static int[][] copyMap(int[][] src) {
        int[][] copy = new int[src.length][];
        for (int i = 0; i < src.length; i++) {
            copy[i] = Arrays.copyOf(src[i], src[i].length);
        }
        return copy;
    }

    public static void setupMap(int[][] fieldMap,
                                List<Monster> importedMonsterList,
                                List<GameMonster> gameMonsterList,
                                List<Chest> activeChests,
                                Armor armor,
                                Weapon weapon) {
        for (int row = 0; row < fieldMap.length; row++) {
            for (int col = 0; col < fieldMap[row].length; col++) {
                int tile = fieldMap[row][col];
                TypeOfObjects tileType = TypeOfObjects.fromValue(tile);

                if (tile == TypeOfObjects.START_POINT.getValue()) {
                    int health = armor == null ? PLAYER_MAX_HEALTH : armor.getHealth();
                    double damage = weapon == null ? 20.0 : weapon.getAtkValue();
                    Player.getInstance(col, row, health, damage, "/com/gnome/gnome/images/player.png");
                    fieldMap[row][col] = TypeOfObjects.START_POINT.getValue();
                }

                if (tileType != null && tileType.isChest()) {
                    double val = returnBasedChestTypeValue(tileType);
                    activeChests.add(new Chest(col, row, val, tileType.getImagePath(), "/com/gnome/gnome/effects/" + getGifChestPath(tileType)));
                    fieldMap[row][col] = TypeOfObjects.FLOOR.getValue();
                }

                if (tile < 0) {
                    com.gnome.gnome.models.Monster dbMonster = findMonsterById(importedMonsterList, tile);
                    if (dbMonster == null) throw new RuntimeException("Monster not found: " + tile);
                    GameMonster m = MonsterFactory.createMonster(tileType, col, row, dbMonster);
                    fieldMap[row][col] = TypeOfObjects.FLOOR.getValue();
                    gameMonsterList.add(m);
                }
            }
        }
    }

    public static String getGifChestPath(TypeOfObjects tileType) {
        return switch (tileType) {
            case CHEST_1 -> "animated_chest_302.gif";
            case CHEST_2 -> "animated_doors_348.gif";
            case CHEST_3 -> "animated_drawer_354.gif";
            case CHEST_4 -> "door_opening_355.gif";
            case CHEST_5 -> "preserved_background_451.gif";
            default -> null;
        };
    }

    private static double returnBasedChestTypeValue(TypeOfObjects type) {
        Random random = new Random();
        return switch (type) {
            case CHEST_1 -> 1 + random.nextDouble() * 10;
            case CHEST_2 -> 2 + random.nextDouble() * 10;
            case CHEST_3 -> 3 + random.nextDouble() * 10;
            case CHEST_4 -> 4 + random.nextDouble() * 10;
            case CHEST_5 -> 5 + random.nextDouble() * 10;
            default -> throw new IllegalStateException("Unexpected chest type: " + type);
        };
    }

    private static com.gnome.gnome.models.Monster findMonsterById(List<com.gnome.gnome.models.Monster> list, int id) {
        return list.stream().filter(monster -> monster.getId() == id).findFirst().orElse(null);
    }

    public static boolean loadProperties(String key) {
        try (InputStream inputStream = GameInitializer.class.getClassLoader().getResourceAsStream("app.properties")) {
            if (inputStream == null) throw new RuntimeException("Could not find app.properties");
            Properties props = new Properties();
            props.load(inputStream);
            return Boolean.parseBoolean(props.getProperty(key));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load app.properties", e);
        }
    }
}
