package com.gnome.gnome.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.movements.OmnidirectionalMovement;

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
                TypeOfObjects.GOBLIN.getImagePathForMonsters(),
                "/com/gnome/gnome/images/monsters/hitGif/goblin_damaged.gif",
                "/com/gnome/gnome/images/monsters/attackGif/animated_goblin.gif",
                dbMonster.getScore_val());
    }
}
