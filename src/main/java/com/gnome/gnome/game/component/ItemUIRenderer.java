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
    private final Camera camera;

    private static final String DEFAULT_IMAGE_PATH = "/com/gnome/gnome/images/default-no-item.png";

    public ItemUIRenderer(VBox uiPane, Camera camera) {
        this.uiPane = uiPane;
        this.camera = camera;
    }

    public void render(Armor armor, Weapon weapon, Potion potion) {
        uiPane.getChildren().clear();

        HBox itemRow = new HBox();
        itemRow.setAlignment(Pos.CENTER);
        itemRow.setSpacing(50); // більше простору між елементами

        itemRow.getChildren().addAll(
                createItemBox(
                        weapon != null ? weapon.getImg() : null,
                        weapon != null ? weapon.getNameEng() : "None",
                        weapon != null ? "DMG: " + weapon.getAtkValue() : ""
                ),
                createItemBox(
                        potion != null ? potion.getImg1() : null,
                        potion != null ? potion.getNameEng() : "None",
                        potion != null ? "HP: +" + potion.getScoreVal() : ""
                ),
                createItemBox(
                        armor != null ? armor.getImg() : null,
                        armor != null ? armor.getNameEng() : "None",
                        armor != null ? "DEF: " + armor.getDefCof() : ""
                )
        );

        uiPane.getChildren().add(itemRow);
    }

    private VBox createItemBox(String imagePath, String name, String stat) {
        VBox box = new VBox();
        box.setAlignment(Pos.TOP_CENTER);
        box.setSpacing(8);

        double size = 80;

        ImageView icon = new ImageView(loadImageOrDefault(imagePath));
        icon.setFitWidth(size);
        icon.setFitHeight(size);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.WHITE);

        Label statLabel = new Label(stat);
        statLabel.setFont(Font.font("Arial", 12));
        statLabel.setTextFill(Color.LIGHTGRAY);

        box.getChildren().addAll(icon, nameLabel, statLabel);
        return box;
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
