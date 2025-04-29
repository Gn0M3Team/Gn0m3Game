package com.gnome.gnome.shop.service;
import com.gnome.gnome.dao.ArmorDAO;
import com.gnome.gnome.dao.PotionDAO;
import com.gnome.gnome.dao.WeaponDAO;
import com.gnome.gnome.exceptions.BalanceException;
import com.gnome.gnome.player.Player;

import java.util.ArrayList;
import java.util.List;

public class ShopService {
    private final WeaponDAO weaponDAO = new WeaponDAO();
    private final PotionDAO potionDAO = new PotionDAO();
    private final ArmorDAO armorDAO = new ArmorDAO();
    private final Player player;

    private List<ShopItem> items = new ArrayList<>();

    public ShopService() {
        this.player = Player.getInstance(0, 0, 0);
    }

    public List<ShopItem> get_shop_items() {
        ShopItems<WeaponDAO> weapons = new ShopItems<>(weaponDAO);
        ShopItems<PotionDAO> potions = new ShopItems<>(potionDAO);
        ShopItems<ArmorDAO> armors = new ShopItems<>(armorDAO);

        items.addAll(potions.randomSelect(1));
        items.addAll(armors.randomSelect(2));
        items.addAll(weapons.randomSelect(5));

        return items;
    }

    public void buy(ShopItem item) {
        float playerCoinsRemainder = player.getPlayerCoins() - item.getCost();
        if (playerCoinsRemainder < 0) {
            throw new BalanceException("Not enough money");
        }

        System.out.println("playerCoinsRemainder" + playerCoinsRemainder);
        player.setPlayerCoins((int) playerCoinsRemainder);
        item.buy(player);

        System.out.println();
        System.out.println("ArmorId: " + player.getArmorId());
        System.out.println("Weapon: " + player.getWeaponId());
        System.out.println("PotionId: " + player.getPotionId());
    }
}
