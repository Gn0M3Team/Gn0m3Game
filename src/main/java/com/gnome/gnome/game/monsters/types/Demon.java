package com.gnome.gnome.game.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.game.monsters.GameMonster;
import com.gnome.gnome.game.monsters.movements.OmnidirectionalMovement;

public class Demon extends GameMonster {
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