package com.gnome.gnome.game;

import com.gnome.gnome.dao.*;

import com.gnome.gnome.models.*;

import com.gnome.gnome.userState.UserState;
import com.gnome.gnome.utils.QuadroConsumer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MapLoaderService {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MonsterDAO monsterDAO;
    private final ArmorDAO armorDAO;
    private final WeaponDAO weaponDAO;
    private final PotionDAO potionDAO;

    public MapLoaderService(MonsterDAO monsterDAO, ArmorDAO armorDAO, WeaponDAO weaponDAO, PotionDAO potionDAO) {
        this.monsterDAO = monsterDAO;
        this.armorDAO = armorDAO;
        this.weaponDAO = weaponDAO;
        this.potionDAO = potionDAO;
    }

    public void loadMapAsync(QuadroConsumer<List<Monster>, Armor, Weapon, Potion> onSuccess,
                             Consumer<Exception> onError) {
        executor.submit(() -> {
            try {
                List<Monster> monsters = monsterDAO.getAllMonsters().stream().peek((value) -> value.setId(value.getId() * -1)).toList();

                Integer armorId = UserState.getInstance().getArmorId();
                Armor armor = null;
                if (armorId != null)
                    armor = armorDAO.getArmorById(armorId);

                Integer weaponId = UserState.getInstance().getWeaponId();
                Weapon weapon = null;
                if (weaponId != null)
                    weapon = weaponDAO.getWeaponById(weaponId);

                Integer potionId = UserState.getInstance().getPotionId();
                Potion potion = null;
                if (potionId != null)
                    potion = potionDAO.getPotionById(potionId);

                if (monsters != null) {
                    onSuccess.accept(monsters, armor, weapon, potion);
                } else {
                    onError.accept(new RuntimeException("Something went wrong"));
                }
            } catch (Exception e) {
                onError.accept(e);
            }
        });
    }


}
