package com.gnome.gnome.shop.controllers;

import com.gnome.gnome.shop.service.ShopItem;

public class ItemController {
    private ShopItem item;
    public void setItemData(ShopItem data) {
        this.item = data;
    }
    public void onCancel() {
    }

    public void onBuy() {}
}
