package com.gnome.gnome.shop.service;
import com.gnome.gnome.shop.utils.ItemCategory;
import lombok.Getter;

/**
 * Represents shop item
 */
public class ShopItem {
    @Getter
    private int id;
    @Getter
    private float cost;
    @Getter
    private String nameEng;
    @Getter
    private String nameSk;
    @Getter
    private String detailsEng;
    @Getter
    private String detailsSk;
    private ItemCategory category;

    public ShopItem(int id,
                    float cost,
                    String nameEng,
                    String nameSk,
                    String detailsEng,
                    String detailsSk,
                    ItemCategory category) {
        this.id          = id;
        this.cost        = cost;
        this.nameEng     = nameEng;
        this.nameSk      = nameSk;
        this.detailsEng  = detailsEng;
        this.detailsSk   = detailsSk;
        this.category    = category;
    }

    public void buy() {
        category.applyBuy(this.id);
    }

    public String getImagePath() {
        String folderName = category.getFolder();
        return String.format(
                "/com/gnome/gnome/images/items/%s/%s.png",
                folderName,
                this.id
        );
    }
}
