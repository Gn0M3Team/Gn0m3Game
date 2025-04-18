package com.gnome.gnome.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.movements.RandomMovement;
import javafx.scene.Node;

public class Butterfly extends Monster {
    public Butterfly(int startX, int startY) {
        super(10,
                50,
                20,
                2,
                "Butterfly",
                "motýľ",
                startX,
                startY,
                TypeOfObjects.BUTTERFLY.getValue(),
                new RandomMovement());
    }

    @Override
    public Node attack() {
        System.out.println("The butterfly flutters around in a dazzling display!");
        return null;
    }
}