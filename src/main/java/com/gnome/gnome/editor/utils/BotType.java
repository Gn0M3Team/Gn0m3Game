package com.gnome.gnome.editor.utils;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BotType {
    private String name;
    private String imagePath;
    private TypeOfObjects monsterType;

    public BotType(String name, String imagePath, TypeOfObjects monsterType) {
        this.name = name;
        this.imagePath = imagePath;
        this.monsterType = monsterType;
    }
}
