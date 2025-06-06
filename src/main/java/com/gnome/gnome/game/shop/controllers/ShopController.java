package com.gnome.gnome.game.shop.controllers;

import com.gnome.gnome.MainApplication;
import com.gnome.gnome.game.GameController;
import com.gnome.gnome.game.component.CoinUIRenderer;
import com.gnome.gnome.game.component.ItemUIRenderer;
import com.gnome.gnome.game.shop.service.ShopItem;
import com.gnome.gnome.game.shop.service.ShopService;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import com.gnome.gnome.userState.UserState;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Controller class for the Shop view in the application.
 * This class manages user interaction within the shop,
 * such as returning to the game. It utilizes a {@link PageSwitcherInterface}
 * to handle page transitions.
 */
public class ShopController {
    @Setter
    @Getter
    private GameController gameController;
    private static final Logger logger = Logger.getLogger(ShopController.class.getName());

    @Getter @Setter
    private Runnable onExit;

    @Getter @Setter
    private Runnable onContinue;

    @FXML
    private Button ReturnGameButton;
    @FXML
    private Button exitButton;
    @FXML
    private BorderPane shopContainer;
    @FXML
    private GridPane itemsContainer;
    @FXML
    private HBox inventoryContainer;

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
        if (onContinue != null) onContinue.run();
    }

    @FXML
    public void onExit(ActionEvent e) {
        if (onExit != null) onExit.run();
    }

    /**
     * Loads random items for shop
     * Render items into the UI
     */
    @FXML
    public void loadItems() {
        int cellIndex = 0;
        List<ShopItem> items = shopService.get_shop_items();

        renderHUD();

        for (var node : itemsContainer.getChildren()) {
            if (!(node instanceof VBox cell)) continue;

            ShopItem item = items.get(cellIndex);
            cellIndex++;

            Label priceLabel = (Label)cell.getChildren().get(1);
            priceLabel.setText(String.valueOf(item.getCost()));

            StackPane iconContainer = (StackPane) cell.getChildren().get(0);
            iconContainer.getChildren().clear();

            Image itemImage = item.getImage();
            ImageView iv = new ImageView(itemImage);
            iv.setFitWidth(iconContainer.getPrefWidth());
            iv.setFitHeight(iconContainer.getPrefHeight());
            iv.setPreserveRatio(true);
            iconContainer.getChildren().add(iv);

            cell.setOnMouseClicked(e -> showItemModal(item));
        }
    }

    /**
     * Render the HUD including inventory and coins of the player
     */
    protected void renderHUD() {
        UserState userState = UserState.getInstance();
        VBox itemBoxContent = new VBox(20);
        itemBoxContent.setAlignment(Pos.CENTER);
        itemBoxContent.setPickOnBounds(false);
        itemBoxContent.setMouseTransparent(true);

        ItemUIRenderer itemUIRenderer = new ItemUIRenderer(itemBoxContent);
        itemUIRenderer.render();

        CoinUIRenderer coinUIRenderer = new CoinUIRenderer(itemBoxContent, userState);
        coinUIRenderer.render();

        StackPane itemBox = new StackPane(itemBoxContent);
        itemBox.setAlignment(Pos.CENTER);
        itemBox.setPrefWidth(200);
        itemBox.setStyle("-fx-padding: 0 20 0 0;");

        shopContainer.setRight(itemBox);
    }

    /**
     * Displays a modal dialog for the selected item.
     * The dialog shows the item's name and description,
     * a “Buy” button for purchasing the item.
     * a "Cancel" to close the modal window.
     *
     * @param item  the {@link ShopItem} whose details are to be displayed
     * @throws RuntimeException if the FXML resource or controller cannot be loaded
     */
    private void showItemModal(ShopItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/gnome/gnome/pages/item-modal.fxml")
            );
            loader.setResources(MainApplication.getLangBundle());

            Parent root = loader.load();
            ItemController itemController = loader.getController();

            Stage popup = new Stage();
            popup.initOwner(shopContainer.getScene().getWindow());
            popup.initModality(Modality.APPLICATION_MODAL);
            Image image = item.getImage();
            itemController.setItemData(item, new ImageView(image));

            popup.setScene(new Scene(root));
            popup.showAndWait();
            renderHUD();
        } catch (IOException e) {
            logger.severe("Failed to load item's data popup: " + e.getMessage());
            throw new RuntimeException("Failed to load item's data popup: " + e.getMessage());
        }
    }
}
