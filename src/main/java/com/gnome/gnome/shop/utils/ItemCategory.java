package com.gnome.gnome.shop.utils;

import com.gnome.gnome.userState.UserState;
import lombok.Getter;

import java.util.function.Consumer;

/**
 * This enum represents category of items that are available in the shop
 *
 * Each category contains the folder name where its image is stored
 * Each category contains Consumer to assign bought item to user with particular function
 * (setArmorId, setWeaponId, setPotionId)
 */
public enum ItemCategory {
    ARMOR("armors", UserState.getInstance()::setArmorId),
    WEAPON("weapons", UserState.getInstance()::setWeaponId),
    POTION("potions", UserState.getInstance()::setPotionId);

    @Getter
    private final String folder;
    /**
     * Function to assign the bought item to the user:
     * accepts the item ID and calls the corresponding method
     * for weapon -> setWeaponId,
     * for potion -> setPotionId,
     * for armor -> setArmorId
     */
    private final Consumer<Integer> assigner;

    ItemCategory(String folder, Consumer<Integer> assigner) {
        this.folder = folder;
        this.assigner = assigner;
    }

    /**
     * Call category consumer to assign an item for the user
     *
     * @param itemId the identifier of the bought item
     */
    public void applyBuy(int itemId) {
        assigner.accept(itemId);
    }
}
