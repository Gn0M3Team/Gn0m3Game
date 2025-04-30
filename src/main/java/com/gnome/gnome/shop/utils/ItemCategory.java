package com.gnome.gnome.shop.utils;

import com.gnome.gnome.player.Player;
import lombok.Getter;

import java.util.function.Consumer;

public enum ItemCategory {
    ARMOR("armors", Player.getInstance(0, 0, 0)::setArmorId),
    WEAPON("weapons", Player.getInstance(0, 0, 0)::setWeaponId),
    POTION("potions", Player.getInstance(0, 0, 0)::setPotionId);

    @Getter
    private final String folder;
    private final Consumer<Integer> assigner;

    ItemCategory(String folder, Consumer<Integer> assigner) {
        this.folder = folder;
        this.assigner = assigner;
    }

    public void applyBuy(int itemId) {
        assigner.accept(itemId);
    }
}
