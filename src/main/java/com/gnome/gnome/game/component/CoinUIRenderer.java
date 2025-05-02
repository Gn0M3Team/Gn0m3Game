package com.gnome.gnome.game.component;

import com.gnome.gnome.camera.Camera;
import com.gnome.gnome.player.Player;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;

import static com.gnome.gnome.game.component.ObjectsConstants.COIN_IMAGE;

public class CoinUIRenderer {

    private final VBox uiPane;
    private final Player player;
    private final Camera camera;
    private HBox coinBox;
    private Label coinCountLabel;

    private static final String COIN_IMAGE_PATH = "/com/gnome/gnome/images/";

    public CoinUIRenderer(VBox uiPane, Player player, Camera camera) {
        this.uiPane = uiPane;
        this.player = player;
        this.camera = camera;
    }

    public void render() {
        if (coinBox != null) {
            uiPane.getChildren().remove(coinBox);
        }

        Image coinImage = loadImage(COIN_IMAGE_PATH + COIN_IMAGE);
        if (coinImage == null) {
            System.err.println("Coin image not found: " + COIN_IMAGE_PATH + COIN_IMAGE);
            return;
        }

        double boxSize = 80; // більший розмір
        double iconSize = 60;

        coinBox = new HBox();
        coinBox.setAlignment(Pos.CENTER_LEFT);
        coinBox.setSpacing(10);
        coinBox.setMouseTransparent(true);

        // Внутрішній VBox з іконкою та текстом
        VBox innerBox = new VBox();
        innerBox.setAlignment(Pos.CENTER);
        innerBox.setSpacing(4);

        ImageView coinIcon = new ImageView(coinImage);
        coinIcon.setFitWidth(iconSize);
        coinIcon.setFitHeight(iconSize);

        coinCountLabel = new Label("x" + player.getPlayerCoins());
        coinCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        coinCountLabel.setTextFill(Color.WHITE);

        innerBox.getChildren().addAll(coinIcon, coinCountLabel);

        StackPane iconContainer = new StackPane(innerBox);
        iconContainer.setPrefSize(boxSize, boxSize);
        iconContainer.setStyle("-fx-border-color: white; -fx-border-width: 2; -fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 6;");
        coinBox.getChildren().add(iconContainer);

        // Додаємо до UI
        if (uiPane.getChildren().isEmpty()) {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER);
            row.setSpacing(30);
            row.getChildren().add(coinBox);
            uiPane.getChildren().add(row);
        } else if (uiPane.getChildren().getFirst() instanceof HBox row) {
            row.getChildren().addFirst(coinBox);
        } else {
            uiPane.getChildren().add(coinBox);
        }
    }

    public void update() {
        if (coinCountLabel != null) {
            coinCountLabel.setText("x" + player.getPlayerCoins());
        } else {
            render();
        }
    }

    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        return stream != null ? new Image(stream) : null;
    }
}
