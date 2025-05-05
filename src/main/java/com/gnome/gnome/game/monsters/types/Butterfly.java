package com.gnome.gnome.game.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.game.monsters.GameMonster;
import com.gnome.gnome.game.monsters.movements.RandomMovement;

public class Butterfly extends GameMonster {
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