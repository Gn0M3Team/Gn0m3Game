package com.gnome.gnome.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.movements.OmnidirectionalMovement;
import javafx.scene.Node;

public class Demon extends Monster {
    public Demon(int startX, int startY, com.gnome.gnome.models.Monster dbMonster) {
        super(dbMonster.getAttack(),
                dbMonster.getHealth(),
                dbMonster.getCost(),
                dbMonster.getRadius(),
                dbMonster.getName(),
                dbMonster.getName_sk(),
                startX,
                startY,
                TypeOfObjects.DEMON.getValue(),
                new OmnidirectionalMovement(),
                TypeOfObjects.DEMON.getImagePath(),
                "/com/gnome/gnome/effects/demon_damaged.gif",
                "/com/gnome/gnome/effects/red_monster.gif");
    }
}