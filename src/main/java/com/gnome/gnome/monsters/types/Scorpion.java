package com.gnome.gnome.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.movements.StraightMovement;

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
                ,TypeOfObjects.SCORPION.getImagePathForMonsters(),
                "/com/gnome/gnome/images/monsters/hitGif/scorpion_damaged.gif",
                "/com/gnome/gnome/images/monsters/attackGif/animated_scorpion.gif",
                dbMonster.getScore_val());
    }
}
