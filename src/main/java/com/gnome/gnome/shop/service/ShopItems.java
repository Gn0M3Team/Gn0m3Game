package com.gnome.gnome.shop.service;

import com.gnome.gnome.dao.ArmorDAO;
import com.gnome.gnome.dao.PotionDAO;
import com.gnome.gnome.dao.WeaponDAO;
import com.gnome.gnome.models.Armor;
import com.gnome.gnome.models.Potion;
import com.gnome.gnome.models.Weapon;
import com.gnome.gnome.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopItems<T> {
    private final List<ShopItem> items = new ArrayList<>();

    public ShopItems(T dao) {
        if (dao instanceof ArmorDAO) {
            ArmorDAO armorDao = (ArmorDAO) dao;
            for (Armor a : armorDao.getAllArmors()) {
                items.add(new ShopItem(
                        a.getId(),
                        a.getCost(),
                        a.getNameEng(),
                        a.getNameSk(),
                        a.getDetailsEng(),
                        a.getDetailsSk(),
                        "armors",
                        Player::setArmorId
                ));
            }
            return;
        }

        if (dao instanceof WeaponDAO) {
            WeaponDAO weaponDao = (WeaponDAO) dao;
            for (Weapon w : weaponDao.getAllWeapons()) {
                System.out.println(w.getCost());
                items.add(new ShopItem(
                        w.getId(),
                        w.getCost(),
                        w.getNameEng(),
                        w.getNameSk(),
                        w.getDetailsEng(),
                        w.getDetailsSk(),
                        "weapons",
                        Player::setWeaponId
                ));
            }
            return;
        }

        else if (dao instanceof PotionDAO) {
            PotionDAO potionDao = (PotionDAO) dao;
            for (Potion p : potionDao.getAllPotions()) {
                items.add(new ShopItem(
                        p.getId(),
                        p.getCost(),
                        p.getNameEng(),
                        p.getNameSk(),
                        p.getDetailsEng(),
                        p.getDetailsSk(),
                        "potions",
                        Player::setPotionId
                ));
            }
            return;
        }

        throw new RuntimeException("Unsupported DAO type: " + dao.getClass());
    }

    /**
     * Select n random items to render them in the shop
     * */
    List<ShopItem> randomSelect(int n) {
        for (ShopItem item: items) {
            System.out.println("fgcwifwdfwdf" + item.getNameEng() + " " + item.getCost());
        }

        Collections.shuffle(items);
        return items.subList(0, Math.min(n, items.size()));
    }
}
