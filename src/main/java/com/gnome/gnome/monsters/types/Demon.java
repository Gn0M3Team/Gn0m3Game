package com.gnome.gnome.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.movements.OmnidirectionalMovement;
import javafx.scene.Node;

public class Demon extends Monster {
    public Demon(int startX, int startY) {
        super(100,
                600,
                1000,
                3,
                "Demon",
                "Démon",
                startX,
                startY,
                TypeOfObjects.DEMON.getValue(),
                new OmnidirectionalMovement()
                );
    }

    @Override
    public Node attack() {
        System.out.println("The demon unleashes a fiery blast!");
        return null;
    }
}
