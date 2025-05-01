package com.gnome.gnome.shop.service;
import com.gnome.gnome.shop.utils.ItemCategory;
import lombok.Getter;
import javafx.scene.image.Image;

import java.util.Objects;

/**
 * Represents shop item
 */
public class ShopItem {
    @Getter
    private int id;
    @Getter
    private float cost;
    @Getter
    private String name;
    @Getter
    private String details;
    private ItemCategory category;
    @Getter
    private String characteristics;
    @Getter
    private String imageName;

    public ShopItem(int id,
                    float cost,
                    String name,
                    String details,
                    String imageName,
                    ItemCategory category,
                    String characteristics) {
        this.id          = id;
        this.cost        = cost;
        this.name        = name;
        this.details     = details;
        this.category    = category;
        this.characteristics = characteristics;
        this.imageName = imageName != null ? "tiles/" + imageName + ".png" : "default-no-item.png";
    }

    /**
     * Assign the item to the corresponding field in UserState
     */
    public void buy() {
        category.applyBuy(this.id);
    }

    /**
     * Get an Image representation of the item
     * @return {@link Image}
     */
    public Image getImage() {
        return new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/gnome/gnome/images/" + imageName)));
    }
}
