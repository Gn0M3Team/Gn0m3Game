package com.gnome.gnome.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.movements.StraightMovement;
import javafx.scene.Node;

public class Scorpion extends Monster {
    public Scorpion(int startX, int startY) {
        super(40,
                400,
                200,
                1,
                "Scorpion",
                "Škorpión",
                startX,
                startY,
                TypeOfObjects.SCORPION.getValue(),
                new StraightMovement());
    }
    
    @Override
    public Node attack() {
        System.out.println("The scorpion strikes with its venomous sting!");
        return null;
    }
}