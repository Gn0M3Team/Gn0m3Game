package com.gnome.gnome.monsters.movements;

import com.gnome.gnome.monsters.Monster;

import java.util.Random;

public class StraightMovement implements MovementStrategy{
    private static final Random rand = new Random();

    @Override
    public void move(Monster monster) {
        if (rand.nextBoolean()) {
            int dx = rand.nextBoolean() ? 1 : -1;
            monster.setPosition(monster.getX() + dx, monster.getY());
        } else {
            int dy = rand.nextBoolean() ? 1 : -1;
            monster.setPosition(monster.getX(), monster.getY() + dy);
        }
    }

}
