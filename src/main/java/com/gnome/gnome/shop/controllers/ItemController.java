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

public class ItemController {
    @FXML
    private Label itemName;
    @FXML
    private Label itemDetails;
    @FXML
    private Label itemPrice;
    @FXML
    private StackPane itemImageContainer;
    @FXML
    private Button cancelButton;

    private ShopService shopService = new ShopService();

    private ShopItem item;
    public void setItemData(ShopItem data, ImageView image) {
        this.item = data;
        itemName.setText(item.getNameEng());
        itemDetails.setText("Details: " + item.getDetailsEng());
        itemPrice.setText(String.valueOf(item.getCost()));
        itemImageContainer.getChildren().clear();
        image.setFitWidth(itemImageContainer.getPrefWidth());
        image.setFitHeight(itemImageContainer.getPrefHeight());
        image.setPreserveRatio(true);
        itemImageContainer.getChildren().add(image);
    }
    @FXML
    private void onCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    public void onBuy() {
        try {
            shopService.buy(item);
            // Automatically close the modal after successful buying
            onCancel();
        } catch (BalanceException ex) {
            System.out.println("Not enough money");
        }
    }
}
