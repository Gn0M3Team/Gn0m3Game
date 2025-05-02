package com.gnome.gnome.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.movements.RandomMovement;

public class Butterfly extends Monster {
    public Butterfly(int startX, int startY, com.gnome.gnome.models.Monster dbMonster) {
        super(dbMonster.getAttack(),
                dbMonster.getHealth(),
                dbMonster.getCost(),
                dbMonster.getRadius(),
                dbMonster.getName(),
                dbMonster.getName_sk(),
                startX,
                startY,
                TypeOfObjects.BUTTERFLY.getValue(),
                new RandomMovement(),
                TypeOfObjects.BUTTERFLY.getImagePathForMonsters(),
                "/com/gnome/gnome/images/monsters/hitGif/butterfly_damage.gif",
                "/com/gnome/gnome/images/monsters/attackGif/animated_butterfly.gif",
                dbMonster.getScore_val());
    }
}