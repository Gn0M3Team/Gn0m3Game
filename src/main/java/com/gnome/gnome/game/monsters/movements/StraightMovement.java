package com.gnome.gnome.game.monsters.movements;

import com.gnome.gnome.game.monsters.GameMonster;

import java.util.Random;

public class StraightMovement implements MovementStrategy{
    private static final Random rand = new Random();

    @Override
    public void move(GameMonster gameMonster) {
        if (rand.nextBoolean()) {
            int dx = rand.nextBoolean() ? 1 : -1;
            gameMonster.setPosition(gameMonster.getX() + dx, gameMonster.getY());
        } else {
            int dy = rand.nextBoolean() ? 1 : -1;
            gameMonster.setPosition(gameMonster.getX(), gameMonster.getY() + dy);
        }
    }

}
