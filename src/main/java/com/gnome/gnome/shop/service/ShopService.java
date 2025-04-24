package com.gnome.gnome.shop.service;

import com.gnome.gnome.dao.ArmorDAO;
import com.gnome.gnome.dao.PotionDAO;
import com.gnome.gnome.dao.WeaponDAO;

import java.util.ArrayList;
import java.util.List;

public class ShopService {
    private final WeaponDAO weaponDAO = new WeaponDAO();
    private final PotionDAO potionDAO = new PotionDAO();
    private final ArmorDAO armorDAO = new ArmorDAO();

    private List<ShopItem> items = new ArrayList<>();

    public List<ShopItem> get_shop_items() {
        ShopItems<WeaponDAO> weapons = new ShopItems<>(weaponDAO);
        ShopItems<PotionDAO> potions = new ShopItems<>(potionDAO);
        ShopItems<ArmorDAO> armors = new ShopItems<>(armorDAO);

        items.addAll(potions.randomSelect(1));
        items.addAll(armors.randomSelect(2));
        items.addAll(weapons.randomSelect(5));

        return items;
    }
}
