package com.gnome.gnome.game.component;

import com.gnome.gnome.camera.Camera;
import com.gnome.gnome.player.Player;
import com.gnome.gnome.userState.UserState;
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
    private final UserState userState;
    private VBox coinBox;
    private Label coinCountLabel;

    private static final String COIN_IMAGE_PATH = "/com/gnome/gnome/images/";

    public CoinUIRenderer(VBox uiPane, Player player) {
        this.uiPane = uiPane;
        this.player = player;
        this.userState = null;
    }

    public CoinUIRenderer(VBox uiPane, UserState userState) {
        this.uiPane = uiPane;
        this.userState = userState;
        this.player = null;
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

        double size = 100;

        ImageView coinIcon = new ImageView(coinImage);
        coinIcon.setFitWidth(size);
        coinIcon.setFitHeight(size);

        coinCountLabel = new Label("x" + getCoins());
        coinCountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        coinCountLabel.setTextFill(Color.WHITE);

        VBox box = new VBox();
        box.setAlignment(Pos.CENTER);
        box.setSpacing(10);
        box.getChildren().addAll(coinIcon, coinCountLabel);
        box.setStyle(
                "-fx-background-color: rgba(0,0,0,0.4);" +
                        "-fx-border-color: white;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12;"
        );
        box.setMaxWidth(160);

        coinBox = box;
        uiPane.getChildren().add(coinBox);
    }

    public void update() {
        if (coinCountLabel != null) {
            coinCountLabel.setText("x" + getCoins());
        } else {
            render();
        }
    }

    public double getCoins() {
        if (player != null) {
            return player.getPlayerCoins();
        }
        if (userState != null) {
            return userState.getBalance();
        }

        throw new RuntimeException("Unable to render coins. Neither Player not UserState do not exist");
    }

    private Image loadImage(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        return stream != null ? new Image(stream) : null;
    }
}
