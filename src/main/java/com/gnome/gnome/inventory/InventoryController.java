package com.gnome.gnome.inventory;

import com.gnome.gnome.MainApplication;
import com.gnome.gnome.dao.ArmorDAO;
import com.gnome.gnome.dao.PotionDAO;
import com.gnome.gnome.dao.WeaponDAO;
import com.gnome.gnome.models.Armor;
import com.gnome.gnome.models.Potion;
import com.gnome.gnome.models.Weapon;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import com.gnome.gnome.userState.UserState;
import com.gnome.gnome.utils.CustomPopupUtil;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.Logger;

public class InventoryController {

    private static final Logger logger = Logger.getLogger(InventoryController.class.getName());

    @FXML private BorderPane mainBorderPane;
    @FXML private VBox weaponBox;
    @FXML private ImageView weaponImage;
    @FXML private Label weaponName;
    @FXML private Label weaponDescription;
    @FXML private Label weaponCost;
    @FXML private Label weaponBoost;
    @FXML private Button sellWeaponButton;

    @FXML private VBox armorBox;
    @FXML private ImageView armorImage;
    @FXML private Label armorName;
    @FXML private Label armorDescription;
    @FXML private Label armorCost;
    @FXML private Label armorBoost;
    @FXML private Button sellArmorButton;

    @FXML private VBox potionBox;
    @FXML private ImageView potionImage;
    @FXML private Label potionName;
    @FXML private Label potionDescription;
    @FXML private Label potionCost;
    @FXML private Label potionBoost;
    @FXML private Button sellPotionButton;

    @FXML private Button backButton;

    private ResourceBundle bundle;

    private WeaponDAO weaponDAO;
    private ArmorDAO armorDAO;
    private PotionDAO potionDAO;

    private Weapon weapon;
    private Armor armor;
    private Potion potion;

    private UserState userState;
    private PageSwitcherInterface pageSwitch;

    @FXML
    public void initialize() {
        this.bundle = MainApplication.getLangBundle();

        userState = UserState.getInstance();
        weaponDAO = new WeaponDAO();
        armorDAO = new ArmorDAO();
        potionDAO = new PotionDAO();
        pageSwitch = new SwitchPage();

        refreshItems();
    }

    private void refreshItems() {
        // Load items based on user state
        weapon = userState.getWeaponId() != null ? weaponDAO.getWeaponById(userState.getWeaponId()) : null;
        armor = userState.getArmorId() != null ? armorDAO.getArmorById(userState.getArmorId()) : null;
        potion = userState.getPotionId() != null ? potionDAO.getPotionById(userState.getPotionId()) : null;

        // Update Weapon UI
        if (weapon != null && userState.getWeaponId() != 0) {
            weaponImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/gnome/gnome/images/tiles/" + weapon.getImg() + ".png"))));
            weaponName.setText(weapon.getName());
            weaponDescription.setText(weapon.getDetails() );
            weaponCost.setText(bundle.getString("label.cost") + weapon.getCost());
            weaponBoost.setText(bundle.getString("label.attack") + weapon.getAtkValue());
            sellWeaponButton.setDisable(false);
        } else {
            weaponImage.setImage(null); // Clear image
            weaponName.setText(bundle.getString("equipment.weapon.none"));
            weaponDescription.setText(bundle.getString("equipment.weapon.purchase.hint"));
            weaponCost.setText("");
            weaponBoost.setText("");
            sellWeaponButton.setDisable(true);
        }

        // Update Armor UI
        if (armor != null && userState.getArmorId() != 0) {
            armorImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/gnome/gnome/images/tiles/" + armor.getImg() + ".png"))));
            armorName.setText(MainApplication.getLang() == 'E' ? armor.getNameEng() : armor.getNameSk());
            armorDescription.setText(MainApplication.getLang() == 'E' ? armor.getDetailsEng() : armor.getDetailsSk());
            armorCost.setText(bundle.getString("label.cost") + armor.getCost());
            armorBoost.setText(bundle.getString("label.defense") + armor.getDefCof() + " | " + bundle.getString("label.health")+ armor.getHealth());
            sellArmorButton.setDisable(false);
        } else {
            armorImage.setImage(null); // Clear image
            armorName.setText(bundle.getString("equipment.armor.none"));
            armorDescription.setText(bundle.getString("equipment.armor.purchase.hint"));
            armorCost.setText("");
            armorBoost.setText("");
            sellArmorButton.setDisable(true);
        }

        // Update Potion UI
        if (potion != null && userState.getPotionId() != 0) {
            potionImage.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/gnome/gnome/images/tiles/" + potion.getImg1() + ".png"))));
            potionName.setText(potion.getName() );
            potionDescription.setText(potion.getDetails());
            potionCost.setText(bundle.getString("label.cost") + potion.getCost());
            potionBoost.setText(bundle.getString("label.heal") + potion.getScoreVal());
            sellPotionButton.setDisable(false);
        } else {
            potionImage.setImage(null); // Clear image
            potionName.setText(bundle.getString("equipment.potion.none"));
            potionDescription.setText(bundle.getString("equipment.potion.unlock.hint"));
            potionCost.setText("");
            potionBoost.setText("");
            sellPotionButton.setDisable(true);
        }
    }

    @FXML
    public void sellWeapon() {
        showSellConfirmation("weapon", weapon.getCost());
    }

    @FXML
    public void sellArmor() {
        showSellConfirmation("armor", armor.getCost());
    }

    @FXML
    public void sellPotion() {
        showSellConfirmation("potion", potion.getCost());
    }


    private void showSellConfirmation(String itemType, float cost) {
        String message = MainApplication.getLang() == 'E'
                ? "Are you sure you want to sell this " + itemType + " for " + cost + " coins?"
                : "Ste si istý, že chcete predať tento " + itemType + " za " + cost + " mincí?";

        // Create the confirmation popup
        Popup confirmPopup = new Popup();
        confirmPopup.setAutoHide(true);

        VBox menuBox = new VBox(20);
        menuBox.getStylesheets().add(getClass().getResource("/com/gnome/gnome/pages/css/dialogs/inventory-sell-conf.css").toExternalForm());
        menuBox.setAlignment(Pos.CENTER);
        menuBox.getStyleClass().add("menu-popup");

        Label title = new Label(MainApplication.getLang() == 'E' ? "Sell Confirmation" : "Potvrdenie predaja");
        title.getStyleClass().add("popup-title");

        Label userLabel = new Label(message);
        userLabel.getStyleClass().add("popup-label");

        Button yesButton = new Button(MainApplication.getLang() == 'E' ? "Yes" : "Áno");
        yesButton.getStyleClass().add("menu-button");
        Button noButton = new Button(MainApplication.getLang() == 'E' ? "No" : "Nie");
        noButton.getStyleClass().add("menu-button");

        yesButton.setOnAction(event -> {
            switch (itemType) {
                case "weapon":
                    userState.setBalance(userState.getBalance() + weapon.getCost());
                    userState.setWeaponId(null);
                    logger.info("Sold weapon for " + weapon.getCost() + ". New balance: " + userState.getBalance());
                    break;
                case "armor":
                    userState.setBalance(userState.getBalance() + armor.getCost());
                    userState.setArmorId(null);
                    logger.info("Sold armor for " + armor.getCost() + ". New balance: " + userState.getBalance());
                    break;
                case "potion":
                    userState.setBalance(userState.getBalance() + potion.getCost());
                    userState.setPotionId(null);
                    logger.info("Sold potion for " + potion.getCost() + ". New balance: " + userState.getBalance());
                    break;
            }
            refreshItems();
            confirmPopup.hide();
        });

        noButton.setOnAction(e -> confirmPopup.hide());

        menuBox.getChildren().addAll(title, userLabel, yesButton, noButton);
        confirmPopup.getContent().add(menuBox);

        Scene scene = mainBorderPane.getScene();
        if (scene != null) {
            Bounds bounds = scene.getRoot().localToScreen(scene.getRoot().getBoundsInLocal());
            double centerX = bounds.getMinX() + bounds.getWidth() / 2;
            double centerY = bounds.getMinY() + bounds.getHeight() / 2;

            confirmPopup.show(scene.getWindow());

            double popupWidth = menuBox.getWidth() > 0 ? menuBox.getWidth() : 400; // Default width if not yet calculated
            double popupHeight = menuBox.getHeight() > 0 ? menuBox.getHeight() : 200; // Default height if not yet calculated
            confirmPopup.setX(centerX - popupWidth / 2);
            confirmPopup.setY(centerY - popupHeight / 2);
        }
    }

    @FXML
    public void goBack() {
        pageSwitch.goMainMenu(mainBorderPane); // Adjust based on where you want to navigate
    }
}