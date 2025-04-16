package com.gnome.gnome.continueGame.component;

import com.gnome.gnome.editor.utils.GenerateGrid;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;

import java.util.Objects;

import static com.gnome.gnome.continueGame.component.ObjectsConstants.*;
import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;

/**
 * Visual representation of a coin that appears on top of a tile.
 */
@Getter
public class Coin {
    private final int gridX;
    private final int gridY;
    private final int value;
    private final ImageView imageView;

    public Coin(int gridX, int gridY, int value) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.value = value;
        this.imageView = new ImageView(
                new Image(
                        Objects.requireNonNull(
                            Coin.class.getResourceAsStream("/com/gnome/gnome/images/tiles/" + COIN_IMAGE)
                        )
                )
        );
        this.imageView.setFitWidth(20);
        this.imageView.setFitHeight(20);
    }

    public void updateScreenPosition(int startCol, int startRow) {
        double centerOfTileX = ((gridX - startCol) + 0.5) * TILE_SIZE;
        double centerOfTileY = ((gridY - startRow) + 0.5) * TILE_SIZE;

        // Place coin so its center matches the tile center
        double coinCenterOffsetX = imageView.getFitWidth() / 2.0;
        double coinCenterOffsetY = imageView.getFitHeight() / 2.0;

        imageView.setTranslateX(centerOfTileX - coinCenterOffsetX);
        imageView.setTranslateY(centerOfTileY - coinCenterOffsetY);
    }
}