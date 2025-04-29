package com.gnome.gnome.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.movements.OmnidirectionalMovement;
import javafx.scene.Node;

public class Goblin extends Monster {
    public Goblin(int startX, int startY, com.gnome.gnome.models.Monster dbMonster) {
        super(dbMonster.getAttack(),
                dbMonster.getHealth(),
                dbMonster.getCost(),
                dbMonster.getRadius(),
                dbMonster.getName(),
                dbMonster.getName_sk(),
                startX,
                startY,
                TypeOfObjects.GOBLIN.getValue(),
                new OmnidirectionalMovement(),
                TypeOfObjects.GOBLIN.getImagePath(),
                "/com/gnome/gnome/effects/goblin_damaged.gif",
                "/com/gnome/gnome/effects/red_monster.gif");
    }

    @Override
    public Node attack(int cameraStartCol, int cameraStartRow, int playerGridX, int playerGridY) {
        System.out.println("The goblin attacks with mischievous ferocity!");
        return null;
    }
}
