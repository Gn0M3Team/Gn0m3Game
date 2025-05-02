package com.gnome.gnome.game.component;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;

import java.util.Objects;

import static com.gnome.gnome.game.component.ObjectsConstants.*;
import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;

/**
 * Visual representation of a coin that appears on top of a tile.
 */
@Getter
public class Coin {
    /**
     * The X position of the coin on the grid.
     */
    private final int gridX;
    /**
     * The Y position of the coin on the grid.
     */
    private final int gridY;
    /**
     * The value of the coin (amount added to player's coin count).
     */
    private final double value;
    /**
     * The image used to visually represent the coin in the UI.
     */
    private final ImageView imageView;

    /**
     * Constructs a new coin at the specified grid position with the given value.
     *
     * @param gridX the X coordinate on the map grid
     * @param gridY the Y coordinate on the map grid
     * @param value the numeric value of the coin
     */
    public Coin(int gridX, int gridY, double value) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.value = value;

        this.imageView = new ImageView(
                new Image(
                        Objects.requireNonNull(
                            Coin.class.getResourceAsStream("/com/gnome/gnome/images/" + COIN_IMAGE)
                        )
                )
        );
        imageView.setFitWidth(TILE_SIZE * 0.5);
        imageView.setFitHeight(TILE_SIZE * 0.5);
    }
}