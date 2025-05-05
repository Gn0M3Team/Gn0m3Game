package com.gnome.gnome.game.monsters.movements;

import com.gnome.gnome.game.monsters.GameMonster;

import java.util.Random;

public class RandomMovement implements MovementStrategy {
    private static final Random rand = new Random();

    @Override
    public void move(GameMonster gameMonster) {
        int direction = rand.nextInt(4);
        switch (direction) {
            case 0 -> gameMonster.setPosition(gameMonster.getX(), gameMonster.getY() - 1);
            case 1 -> gameMonster.setPosition(gameMonster.getX(), gameMonster.getY() + 1);
            case 2 -> gameMonster.setPosition(gameMonster.getX() - 1, gameMonster.getY());
            case 3 -> gameMonster.setPosition(gameMonster.getX() + 1, gameMonster.getY());
        }
    }

}
