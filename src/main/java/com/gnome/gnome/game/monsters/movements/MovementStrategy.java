package com.gnome.gnome.game.monsters.movements;

import com.gnome.gnome.game.monsters.GameMonster;


public interface MovementStrategy {
    /**
     * Moves the monster by updating its position.
     */
    void move(GameMonster gameMonster);

}
