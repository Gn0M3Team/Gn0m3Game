package com.gnome.gnome.game.component;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.Getter;

import java.util.Objects;

import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;
import static com.gnome.gnome.game.component.ObjectsConstants.COIN_IMAGE;

@Getter
public class Chest {
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
    private final String gifPath;

    public Chest(int gridX, int gridY, double value, String imagePath, String gifPath) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.value = value;

        this.imageView = new ImageView(
                new Image(
                        Objects.requireNonNull(
                                Coin.class.getResourceAsStream(imagePath)
                        )
                )
        );

        this.gifPath = gifPath;
    }

    public void setImageSize(int width, int height) {
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
    }

    // This function should change image to gif(/effects/animated_chest_1s.gif. And this animation should play only one time
    public void animate() {
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(gifPath)));

        imageView.setImage(image);
    }
}
