package com.gnome.gnome.game;

import com.gnome.gnome.dao.ArmorDAO;
import com.gnome.gnome.dao.MapDAO;

import com.gnome.gnome.dao.PotionDAO;
import com.gnome.gnome.dao.WeaponDAO;
import com.gnome.gnome.models.Armor;
import com.gnome.gnome.models.Map;

import com.gnome.gnome.models.Potion;
import com.gnome.gnome.models.Weapon;
import com.gnome.gnome.userState.UserState;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapLoader {

    private ExecutorService executor;

    private Popup loadingPopup;
    private Stage primaryStage;

    private Weapon weapon;
    private Armor armor;
    private Potion potion;

    private final WeaponDAO weaponDAO = new WeaponDAO();
    private final ArmorDAO armorDAO = new ArmorDAO();
    private final PotionDAO potionDAO = new PotionDAO();

    public MapLoader() {
    }


    public MapLoader(Stage primaryStage) {
        this.executor = Executors.newSingleThreadExecutor();
        this.primaryStage = primaryStage;
    }



    public void showStartMap(Map selectedMap) {
        showLoadingPopup();

        Integer armorId = UserState.getInstance().getArmorId();
        Integer weaponId = UserState.getInstance().getWeaponId();
        Integer potionId = UserState.getInstance().getPotionId();

        armor = (armorId != null) ? armorDAO.getArmorById(armorId) : null;
        weapon = (weaponId != null) ? weaponDAO.getWeaponById(weaponId) : null;
        potion = (potionId != null) ? potionDAO.getPotionById(potionId) : null;

        executor.submit(() -> {
            try {
                if (selectedMap != null) {
                    Platform.runLater(() -> loadGamePage(selectedMap, armor, weapon, potion));
                } else {
                    Platform.runLater(() -> {
                        hideLoadingPopup();
                        showError("No maps found in database!");
                    });
                }

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    hideLoadingPopup();
                    showError("Failed to load map: " + ex.getMessage());
                });
            }
        });
    }

    private void showLoadingPopup() {
        if (loadingPopup == null) {
            loadingPopup = new Popup();
            Label loadingLabel = new Label("Loading... Please wait");
            loadingLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white; -fx-background-color: #222; -fx-padding: 20px; -fx-background-radius: 10;");
            VBox box = new VBox(loadingLabel);
            box.setAlignment(Pos.CENTER);
            box.setStyle("-fx-background-color: transparent;");
            loadingPopup.getContent().add(box);
            loadingPopup.setAutoHide(false);
        }

        if (primaryStage != null) {
            loadingPopup.show(primaryStage);
            loadingPopup.setX(primaryStage.getX() + primaryStage.getWidth() / 2 - 100);
            loadingPopup.setY(primaryStage.getY() + primaryStage.getHeight() / 2 - 50);
        }
    }

    private void hideLoadingPopup() {
        if (loadingPopup != null) {
            loadingPopup.hide();
        }
    }

    private void loadGamePage(Map selectedMap, Armor armor, Weapon weapon, Potion potion) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/gnome/gnome/pages/game.fxml"));
            Parent root = loader.load();
            GameController controller = loader.getController();
            controller.initializeWithLoadedMap(selectedMap.getMapData(), armor, weapon, potion);

            primaryStage.getScene().setRoot(root);

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Failed to load Continue Game page.");
        } finally {
            hideLoadingPopup();
        }
    }


    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
