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
                new StraightMovement()
                ,TypeOfObjects.SCORPION.getImagePath(), "/com/gnome/gnome/effects/scorpion_damaged.gif");
    }

    @Override
    public Node attack(int cameraStartCol, int cameraStartRow, int playerGridX, int playerGridY) {
        System.out.println("The scorpion strikes with its venomous sting!");
        return null;
    }
}
