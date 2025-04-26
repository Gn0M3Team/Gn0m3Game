package com.gnome.gnome.monsters.movements;

import com.gnome.gnome.monsters.Monster;

import java.util.Random;

public class OmnidirectionalMovement implements MovementStrategy {
    private static final Random rand = new Random();

    @Override
    public void move(Monster monster) {
        int dx = 0, dy = 0;
        while(dx == 0 && dy == 0) {
            dx = rand.nextInt(3) - 1;
            dy = rand.nextInt(3) - 1;
        }
        monster.setPosition(monster.getX() + dx, monster.getY() + dy);
    }


}
