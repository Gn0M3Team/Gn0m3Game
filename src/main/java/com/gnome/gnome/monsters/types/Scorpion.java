package com.gnome.gnome.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.movements.StraightMovement;
import javafx.scene.Node;

public class Scorpion extends Monster {
    public Scorpion(int startX, int startY, com.gnome.gnome.models.Monster dbMonster) {
        super(dbMonster.getAttack(),
                dbMonster.getHealth(),
                dbMonster.getCost(),
                dbMonster.getRadius(),
                dbMonster.getName(),
                dbMonster.getName_sk(),
                startX,
                startY,
                TypeOfObjects.SCORPION.getValue(),
                new StraightMovement()
                ,TypeOfObjects.SCORPION.getImagePath(),
                "/com/gnome/gnome/effects/scorpion_damaged.gif",
                "/com/gnome/gnome/effects/red_monster.gif");
    }
}
