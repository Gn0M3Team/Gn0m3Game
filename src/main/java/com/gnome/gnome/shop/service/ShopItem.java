package com.gnome.gnome.shop.service;
import com.gnome.gnome.player.Player;
import lombok.Getter;
import java.util.function.BiConsumer;

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
    @Getter
    private String category;
    private final BiConsumer<Player, Integer> assignItemToPlayer;

    public ShopItem(int id,
                    float cost,
                    String nameEng,
                    String nameSk,
                    String detailsEng,
                    String detailsSk,
                    String category,
                    BiConsumer<Player, Integer> assignItemToPlayer) {
        this.id          = id;
        this.cost        = cost;
        this.nameEng     = nameEng;
        this.nameSk      = nameSk;
        this.detailsEng  = detailsEng;
        this.detailsSk   = detailsSk;
        this.category    = category;
        this.assignItemToPlayer = assignItemToPlayer;
    }

    public void buy(Player player) {
        assignItemToPlayer.accept(player, this.id);
    }
}
