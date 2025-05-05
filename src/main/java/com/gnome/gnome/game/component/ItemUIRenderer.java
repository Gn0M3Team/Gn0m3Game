package com.gnome.gnome.game.component;

import com.gnome.gnome.dao.ArmorDAO;
import com.gnome.gnome.dao.PotionDAO;
import com.gnome.gnome.dao.WeaponDAO;
import com.gnome.gnome.models.Armor;
import com.gnome.gnome.models.Potion;
import com.gnome.gnome.models.Weapon;
import com.gnome.gnome.userState.UserState;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

    public void render() {
        UserState userState = UserState.getInstance();
        WeaponDAO weaponDAO = new WeaponDAO();
        ArmorDAO armorDAO = new ArmorDAO();
        PotionDAO potionDAO = new PotionDAO();

        uiPane.getChildren().clear();

        Weapon weapon = null;
        Integer weaponId = userState.getWeaponId();
        if (weaponId != null) {
            weapon = weaponDAO.getWeaponById(weaponId);
        }

        Armor armor = null;
        Integer armorId = userState.getArmorId();
        if (armorId != null) {
            armor = armorDAO.getArmorById(armorId);
        }

        Potion potion = null;
        Integer potionId = userState.getPotionId();
        if (potionId != null) {
            potion = potionDAO.getPotionById(potionId);
        }

        weaponBox = createItemBox(
                weapon != null ? weapon.getImg() : null,
                weapon != null ? weapon.getName() : "None",
                weapon != null ? "DMG: " + weapon.getAtkValue() : ""
        );
        potionBox = createItemBox(
                potion != null ? (potion.getImg1() != null ? potion.getImg1() : potion.getImg2()) : null,
                potion != null ? potion.getName() : "None",
                potion != null ? "HP: +" + potion.getScoreVal() : ""
        );
        armorBox = createItemBox(
                armor != null ? armor.getImg() : null,
                armor != null ? armor.getName() : "None",
                armor != null ? "DEF: " + armor.getDefCof() : ""
        );

        uiPane.getChildren().addAll(weaponBox, potionBox, armorBox);
    }

    private VBox createItemBox(String imagePath, String name, String stat) {
        VBox box = new VBox();
        box.setAlignment(Pos.TOP_CENTER);
        box.setSpacing(5);

        double wSize = 70;
        double hSize = 30;

        ImageView icon = new ImageView(loadImageOrDefault(imagePath));
        icon.setFitWidth(wSize);
        icon.setFitHeight(hSize);
        icon.setPreserveRatio(true);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(100);

        Label statLabel = new Label(stat);
        statLabel.setFont(Font.font("Arial", 12));
        statLabel.setTextFill(Color.LIGHTGRAY);

        box.getChildren().addAll(icon, nameLabel, statLabel);
        box.setMaxWidth(120);
        box.setStyle(
                "-fx-background-color: rgba(0,0,0,0.4);" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12;"
        );

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
