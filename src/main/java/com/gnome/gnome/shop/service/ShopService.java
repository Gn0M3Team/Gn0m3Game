package com.gnome.gnome.shop.service;
import com.gnome.gnome.dao.ArmorDAO;
import com.gnome.gnome.dao.PotionDAO;
import com.gnome.gnome.dao.WeaponDAO;
import com.gnome.gnome.exceptions.BalanceException;
import com.gnome.gnome.userState.UserState;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle shop functionality: items loading, item purchasing and so on
 */
public class ShopService {
    private final WeaponDAO weaponDAO = new WeaponDAO();
    private final PotionDAO potionDAO = new PotionDAO();
    private final ArmorDAO armorDAO = new ArmorDAO();
    private final UserState user;

    private final List<ShopItem> items = new ArrayList<>();

    public ShopService() { this.user = UserState.getInstance(); }

    /**
     * Retrieves a randomized list of shop items composed of:
     * <ul>
     *   <li>1 random potion</li>
     *   <li>2 random armors</li>
     *   <li>5 random weapons</li>
     * </ul>
     *
     * @return a {@link List} of {@link ShopItem} objects to display in the shop
     */
    public List<ShopItem> get_shop_items() {
        ItemsSelector<WeaponDAO> weapons = new ItemsSelector<>(weaponDAO);
        ItemsSelector<PotionDAO> potions = new ItemsSelector<>(potionDAO);
        ItemsSelector<ArmorDAO> armors = new ItemsSelector<>(armorDAO);

        items.addAll(potions.randomSelect(1));
        items.addAll(armors.randomSelect(2));
        items.addAll(weapons.randomSelect(5));

        return items;
    }

    /**
     * Attempts to buy particular item for the player
     * If the player does not have enough coins, throws {@link BalanceException}
     * Otherwise, deducts the item cost from the player's balance
     * and assign bought item for the character
     *
     * @param item the {@link ShopItem} to buy
     * @throws BalanceException if the player's coin balance is insufficient
     */
    public void buy(ShopItem item) {
        float coinsRemainder = user.getBalance() - item.getCost();
        if (coinsRemainder < 0) {
            throw new BalanceException("Not enough money");
        }

        user.setBalance(coinsRemainder);
        item.buy();
    }
}
