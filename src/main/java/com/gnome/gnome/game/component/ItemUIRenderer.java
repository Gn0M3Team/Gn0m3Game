package com.gnome.gnome.game.component;

import com.gnome.gnome.camera.Camera;
import com.gnome.gnome.models.Armor;
import com.gnome.gnome.models.Potion;
import com.gnome.gnome.models.Weapon;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.util.Objects;

public class ItemUIRenderer {

    private final VBox uiPane;
    private VBox weaponBox;
    private VBox armorBox;
    private VBox potionBox;

    private static final String DEFAULT_IMAGE_PATH = "/com/gnome/gnome/images/default-no-item.png";

    public ItemUIRenderer(VBox uiPane) {
        this.uiPane = uiPane;
    }

    public void render(Armor armor, Weapon weapon, Potion potion) {
        uiPane.getChildren().clear();

        weaponBox = createItemBox(
                weapon != null ? weapon.getImg() : null,
                weapon != null ? weapon.getNameEng() : "None",
                weapon != null ? "DMG: " + weapon.getAtkValue() : ""
        );
        potionBox = createItemBox(
                potion != null ? (potion.getImg1() != null ? potion.getImg1() : potion.getImg2()) : null,
                potion != null ? potion.getNameEng() : "None",
                potion != null ? "HP: +" + potion.getScoreVal() : ""
        );
        armorBox = createItemBox(
                armor != null ? armor.getImg() : null,
                armor != null ? armor.getNameEng() : "None",
                armor != null ? "DEF: " + armor.getDefCof() : ""
        );

        uiPane.getChildren().addAll(weaponBox, potionBox, armorBox);
    }

    private VBox createItemBox(String imagePath, String name, String stat) {
        VBox box = new VBox();
        box.setAlignment(Pos.TOP_CENTER);
        box.setSpacing(10);

        double size = 100;

        ImageView icon = new ImageView(loadImageOrDefault(imagePath));
        icon.setFitWidth(size);
        icon.setFitHeight(size);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(140);

        Label statLabel = new Label(stat);
        statLabel.setFont(Font.font("Arial", 14));
        statLabel.setTextFill(Color.LIGHTGRAY);

        box.getChildren().addAll(icon, nameLabel, statLabel);

        box.setStyle(
                "-fx-background-color: rgba(0,0,0,0.4);" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12;"
        );

        box.setMaxWidth(160);
        return box;
    }

    public void updatePotion(Potion potion) {
        int index = uiPane.getChildren().indexOf(potionBox);
        if (index >= 0) {
            potionBox = createItemBox(
                    potion != null ? (potion.getImg1() != null ? potion.getImg1() : potion.getImg2()) : null,
                    potion != null ? potion.getNameEng() : "None",
                    potion != null ? "HP: +" + potion.getScoreVal() : ""
            );
            uiPane.getChildren().set(index, potionBox);
        }
    }


    private Image loadImageOrDefault(String imagePath) {
        InputStream stream;
        System.out.println(imagePath);
        if (imagePath != null && !imagePath.isEmpty()) {
            stream = getClass().getResourceAsStream("/com/gnome/gnome/images/tiles/" + imagePath + ".png");
            if (stream != null) {
                return new Image(stream);
            }
        }
        InputStream defaultStream = getClass().getResourceAsStream(DEFAULT_IMAGE_PATH);
        return new Image(Objects.requireNonNull(defaultStream));
    }
}
