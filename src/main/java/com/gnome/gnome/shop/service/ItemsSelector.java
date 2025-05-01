package com.gnome.gnome.shop.service;

import com.gnome.gnome.dao.ArmorDAO;
import com.gnome.gnome.dao.PotionDAO;
import com.gnome.gnome.dao.WeaponDAO;
import com.gnome.gnome.models.Armor;
import com.gnome.gnome.models.Potion;
import com.gnome.gnome.models.Weapon;
import com.gnome.gnome.shop.utils.ItemCategory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A generic selector for shop items of a given category.
 * This class loads all corresponding items from the database into a unified list of {@link ShopItem},
 * and allows selection of a random subset of those items.
 *
 * @param <T> the type of DAO used to load items ({@link ArmorDAO}, {@link WeaponDAO}, or {@link PotionDAO})
 */
public class ItemsSelector<T> {
    private final List<ShopItem> items = new ArrayList<>();

    public ItemsSelector(T dao) {
        if (dao instanceof ArmorDAO armorDao) {
            for (Armor a : armorDao.getAllArmors()) {
                items.add(new ShopItem(
                        a.getId(),
                        a.getCost(),
                        a.getNameEng(),
                        a.getDetailsEng(),
                        a.getImg(),
                        ItemCategory.ARMOR,
                        String.format("Def coef.: %.2f", a.getDefCof())
                ));
            }
            return;
        }

        if (dao instanceof WeaponDAO weaponDao) {
            for (Weapon w : weaponDao.getAllWeapons()) {
                System.out.println(w.getCost());
                items.add(new ShopItem(
                        w.getId(),
                        w.getCost(),
                        w.getNameEng(),
                        w.getDetailsEng(),
                        w.getImg(),
                        ItemCategory.WEAPON,
                        String.format("DMG: %.2f", w.getAtkValue())
                ));
            }
            return;
        }

        else if (dao instanceof PotionDAO potionDao) {
            for (Potion p : potionDao.getAllPotions()) {
                items.add(new ShopItem(
                        p.getId(),
                        p.getCost(),
                        p.getNameEng(),
                        p.getDetailsEng(),
                        p.getImg1(),
                        ItemCategory.POTION,
                        String.format("HP: %+d", p.getScoreVal())
                ));
            }
            return;
        }

        throw new RuntimeException("Unsupported DAO type: " + dao.getClass());
    }

    /**
     * Select n random items to render them in the shop
     * @param n number of randomly selected items
     * */
    List<ShopItem> randomSelect(int n) {
        Collections.shuffle(items);
        return items.subList(0, Math.min(n, items.size()));
    }
}
