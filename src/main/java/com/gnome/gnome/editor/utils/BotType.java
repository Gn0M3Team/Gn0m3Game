package com.gnome.gnome.editor.utils;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BotType {
    private String name;
    private String imagePath;

    public BotType(String name, String imagePath) {
        this.name = name;
        this.imagePath = imagePath;
    }
}
