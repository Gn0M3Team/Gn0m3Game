package com.gnome.gnome.shop.controllers;

import com.gnome.gnome.exceptions.BalanceException;
import com.gnome.gnome.shop.service.ShopItem;
import com.gnome.gnome.shop.service.ShopService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.logging.Logger;

/**
 * Controller class for the shop item's detail modal window
 */
public class ItemController {
    private static final Logger logger = Logger.getLogger(ShopController.class.getName());

    @FXML
    private Label itemName;
    @FXML
    private Label itemValue;
    @FXML
    private Label itemDetails;
    @FXML
    private Label itemPrice;
    @FXML
    private StackPane itemImageContainer;
    @FXML
    private Button cancelButton;

    private final ShopService shopService = new ShopService();
    private ShopItem item;

    /**
     * Loads data into the item's details modal window
     *
     * @param data item to load data
     * @param image ImgaeView for the item's image
     */
    public void setItemData(ShopItem data, ImageView image) {
        this.item = data;
        itemName.setText(item.getNameEng());
        itemValue.setText(item.getCharacteristics());
        itemDetails.setText("Details: " + item.getDetailsEng());
        itemPrice.setText(String.valueOf(item.getCost()));
        itemImageContainer.getChildren().clear();
        image.setFitWidth(itemImageContainer.getPrefWidth());
        image.setFitHeight(itemImageContainer.getPrefHeight());
        image.setPreserveRatio(true);
        itemImageContainer.getChildren().add(image);
    }

    /**
     * Cancel button click event listener
     * Close item's modal window
     */
    @FXML
    private void onCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Buy button click event listener
     * If player has enough money - buys item and close the window
     * If player does not have enough money, shows error message
     */
    public void onBuy() {
        try {
            shopService.buy(item);
            logger.severe(String.format("Player successfully bought item (%d, %s)", item.getId(), item.getNameEng()));
            // Automatically close the modal after buying success
            onCancel();
        } catch (BalanceException ex) {
            logger.severe("Failed to buy item: " + ex.getMessage());
            System.out.println("Not enough money");
        }
    }
}
