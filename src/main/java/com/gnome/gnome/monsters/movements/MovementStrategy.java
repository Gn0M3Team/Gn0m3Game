package com.gnome.gnome.monsters.movements;

import com.gnome.gnome.monsters.Monster;


public interface MovementStrategy {
    /**
     * Moves the monster by updating its position.
     */
    void move(Monster monster);
}
