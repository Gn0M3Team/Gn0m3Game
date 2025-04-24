package com.gnome.gnome.shop.service;

import lombok.Getter;

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
    @Getter
    private String folder;

    public ShopItem(int id,
                    float cost,
                    String nameEng,
                    String nameSk,
                    String detailsEng,
                    String detailsSk,
                    String folder) {
        this.id          = id;
        this.cost        = cost;
        this.nameEng     = nameEng;
        this.nameSk      = nameSk;
        this.detailsEng  = detailsEng;
        this.detailsSk   = detailsSk;
        this.folder      = folder;
    }
}
