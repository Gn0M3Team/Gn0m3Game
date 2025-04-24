package com.gnome.gnome.monsters.movements;

import com.gnome.gnome.monsters.Monster;

import java.util.Random;

public class RandomMovement implements MovementStrategy {
    private static final Random rand = new Random();

    @Override
    public void move(Monster monster) {
        int direction = rand.nextInt(4);
        switch (direction) {
            case 0 -> monster.setPosition(monster.getX(), monster.getY() - 1);
            case 1 -> monster.setPosition(monster.getX(), monster.getY() + 1);
            case 2 -> monster.setPosition(monster.getX() - 1, monster.getY());
            case 3 -> monster.setPosition(monster.getX() + 1, monster.getY());
        }
    }

}
