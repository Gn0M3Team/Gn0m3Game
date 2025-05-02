package com.gnome.gnome.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.movements.OmnidirectionalMovement;

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
                TypeOfObjects.DEMON.getImagePathForMonsters(),
                "/com/gnome/gnome/images/monsters/hitGif/demon_damaged.gif",
                "/com/gnome/gnome/images/monsters/attackGif/animated_demon.gif",
                dbMonster.getScore_val());
    }
}