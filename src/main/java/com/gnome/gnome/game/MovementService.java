package com.gnome.gnome.game;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.player.Player;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.List;

public class MovementService {

    private final GameController controller;

    public MovementService(GameController controller) {
        this.controller = controller;
    }

    public void handleKeyPress(KeyEvent event) {
        if (controller.isGameOver()) return;
        switch (event.getCode()) {
            case LEFT, A, RIGHT, D, UP, W, DOWN, S -> handleMovement(event);
            case E -> handleInteraction();
            case SPACE -> handleAttack();
            default -> {}
        }
    }

    private void handleMovement(KeyEvent event) {
        Player player = controller.getPlayer();
        int oldX = player.getX();
        int oldY = player.getY();
        int newX = oldX;
        int newY = oldY;

        int[][] baseMap = controller.getBaseMap();
        int[][] fieldMap = controller.getFieldMap();

        switch (event.getCode()) {
            case LEFT, A -> newX = Math.max(0, oldX - 1);
            case RIGHT, D -> newX = Math.min(baseMap[0].length - 1, oldX + 1);
            case UP, W -> newY = Math.max(0, oldY - 1);
            case DOWN, S -> newY = Math.min(baseMap.length - 1, oldY + 1);
        }

        if (newX == oldX && newY == oldY) return;
        if (controller.isBlocked(newX, newY, null)) return;

        int tileVal = fieldMap[newY][newX] < 0 ? baseMap[newY][newX] : fieldMap[newY][newX];
        TypeOfObjects tileType = TypeOfObjects.fromValue(tileVal);

        if (!tileType.isWalkable()) {
            if (controller.isDebugModGame()) System.out.println("Blocked tile: " + tileType);
            return;
        }

        controller.movePlayer(oldX, oldY, newX, newY, tileType);
    }

    private void handleInteraction() {
        Player player = controller.getPlayer();
        int px = player.getX();
        int py = player.getY();

        if (controller.isNearTable(px, py)) controller.showTablePopup();
        else if (controller.isNearChest(px, py)) controller.openNearbyChest();
    }

    private void handleAttack() {
        Player player = controller.getPlayer();
        List<Monster> monsters = controller.getMonsterList();

        List<Monster> eliminated = new ArrayList<>();
        player.attack(monsters, 1, eliminated::add);

        if (!eliminated.isEmpty()) {
            controller.removeMonsters(eliminated);
        }
    }
}