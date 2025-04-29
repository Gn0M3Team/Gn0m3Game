package com.gnome.gnome.shop.controllers;

import com.gnome.gnome.continueGame.ContinueGameController;
import com.gnome.gnome.shop.service.ShopItem;
import com.gnome.gnome.shop.service.ShopService;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * Controller class for the Shop Pop-Up view in the application.
 *
 * This class manages user interaction within the shop pop-up,
 * such as returning to the game. It utilizes a {@link PageSwitcherInterface}
 * to handle page transitions.
 */
public class ShopController {
    private static final Logger logger = Logger.getLogger(ShopController.class.getName());

    @FXML
    private Button ReturnGameButton;
    @FXML
    private BorderPane shopPopUpPage;
    @FXML
    private GridPane itemsContainer;

    private PageSwitcherInterface pageSwitch;

    private final ShopService shopService = new ShopService();
    /**
     * Initializes the controller.
     * This method is called automatically after the FXML file is loaded.
     * It sets up the page switcher implementation.
     */
    @FXML
    public void initialize() {
        pageSwitch = new SwitchPage();
        loadItems();
    }

    /**
     * Handles the action triggered by the "Return Game" button.
     * Switches the scene back to the game.
     *
     * @param e the action event triggered by the button click
     */
    @FXML
    public void onNewGame(ActionEvent e) {
        pageSwitch.goNewGame(shopPopUpPage);
    }

    @FXML
    public void onExit(ActionEvent e) {
        pageSwitch.goMainMenu(shopPopUpPage);
    }

    @FXML
    public void loadItems() {
        int cellIndex = 0;
        List<ShopItem> items = shopService.get_shop_items();

        for (var node : itemsContainer.getChildren()) {
            if (!(node instanceof VBox)) continue;

            VBox cell = (VBox)node;
            ShopItem item = items.get(cellIndex);
            System.out.println(item.getNameEng());
            cellIndex++;

            Label priceLabel = (Label)cell.getChildren().get(1);
            priceLabel.setText(String.valueOf(item.getCost()));

            StackPane iconContainer = (StackPane) cell.getChildren().get(0);
            iconContainer.getChildren().clear();

            String resourcePath = String.format(
                    "/com/gnome/gnome/images/items/%s/%s.png",
                    item.getCategory(),
                    item.getId()
            );

            Image image = null;
            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                if (is != null) {
                    image = new Image(is);
                    ImageView iv = new ImageView(image);
                    iv.setFitWidth(iconContainer.getPrefWidth());
                    iv.setFitHeight(iconContainer.getPrefHeight());
                    iv.setPreserveRatio(true);
                    iconContainer.getChildren().add(iv);
                }
            } catch(IOException ex) {
                logger.severe("Failed to load item's image: " + ex.getMessage());
                ex.printStackTrace();
            }

            Image itemImage = image;
            cell.setOnMouseClicked(e -> showPurchasePopup(item, itemImage));
        }
    }

    private void showPurchasePopup(ShopItem item, Image image) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gnome/gnome/pages/item-modal.fxml")
            );
            Parent root = loader.load();
            ItemController itemController = loader.getController();

            Stage popup = new Stage();
            popup.initOwner(shopPopUpPage.getScene().getWindow());
            popup.initModality(Modality.APPLICATION_MODAL);
            itemController.setItemData(item, new ImageView(image));

            popup.setScene(new Scene(root));
            popup.showAndWait();
        } catch (IOException e) {
            logger.severe("Failed to load item's data popup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
