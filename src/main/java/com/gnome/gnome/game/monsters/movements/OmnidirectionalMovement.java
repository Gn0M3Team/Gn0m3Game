package com.gnome.gnome.game.monsters.movements;

import com.gnome.gnome.game.monsters.GameMonster;

import java.util.Random;

public class OmnidirectionalMovement implements MovementStrategy {
    private static final Random rand = new Random();

    @Override
    public void move(GameMonster gameMonster) {
        int dx = 0, dy = 0;
        while(dx == 0 && dy == 0) {
            dx = rand.nextInt(3) - 1;
            dy = rand.nextInt(3) - 1;
        }
        gameMonster.setPosition(gameMonster.getX() + dx, gameMonster.getY() + dy);
    }


}
