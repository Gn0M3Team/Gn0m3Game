package com.gnome.gnome.utils;

import com.gnome.gnome.MainApplication;
import com.gnome.gnome.db.DatabaseWrapper;
import com.gnome.gnome.game.GameController;
import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ResourceBundle;

public class InternetMonitor {
    private final PageSwitcherInterface pageSwitcher;
    private final long checkIntervalMs;
    private volatile boolean running = true;
    private volatile boolean isEnd = false;

    private final HttpClient httpClient;
    private ResourceBundle bundle;

    public InternetMonitor(PageSwitcherInterface pageSwitcher, long checkIntervalMs) {
        this.pageSwitcher = pageSwitcher;
        this.checkIntervalMs = checkIntervalMs;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        this.bundle = MainApplication.getLangBundle();
    }

    public void start() {
        new Thread(() -> {
            while (!isEnd) {
                boolean connected = hasInternetConnection();
                if (!connected) {
                    if (running) {
                        Platform.runLater(() -> {
                            DatabaseWrapper.getInstance().close();
                            if (GameController.getGameController() != null) {
                                GameController.getGameController().onSceneExit(false);
                            }
                            // Get the current stage
                            Stage primaryStage = MainApplication.getPrimaryStage();
                            if (primaryStage == null) return;

                            // Redirect to the main menu
                            pageSwitcher.goToBeginning();

                            // Create the popup
                            Popup popup = new Popup();
                            popup.setAutoHide(true);

                            VBox popupContent = new VBox(20);
                            popupContent.getStylesheets().add(getClass().getResource("/com/gnome/gnome/pages/css/dialogs/internet-error-popup.css").toExternalForm());
                            popupContent.getStyleClass().add("error-popup");
                            popupContent.setAlignment(Pos.CENTER);
                            popupContent.setMaxWidth(400);

                            Label title = new Label(bundle.getString("internet.lost.alert.title"));
                            title.getStyleClass().add("popup-title");

                            Label messageLabel = new Label(bundle.getString("internet.lost.alert.text"));
                            messageLabel.getStyleClass().add("popup-message");

                            Button okButton = new Button("OK");
                            okButton.getStyleClass().add("popup-button");
                            okButton.setOnAction(e -> popup.hide());

                            popupContent.getChildren().addAll(title, messageLabel, okButton);
                            popup.getContent().add(popupContent);

                            // Center the popup in the stage
                            Scene scene = primaryStage.getScene();
                            if (scene != null) {
                                javafx.geometry.Bounds bounds = scene.getRoot().localToScreen(scene.getRoot().getBoundsInLocal());
                                double centerX = bounds.getMinX() + bounds.getWidth() / 2;
                                double centerY = bounds.getMinY() + bounds.getHeight() / 2;
                                double popupWidth = popupContent.getWidth() > 0 ? popupContent.getWidth() : 400;
                                double popupHeight = popupContent.getHeight() > 0 ? popupContent.getHeight() : 200;
                                popup.setX(centerX - popupWidth / 2);
                                popup.setY(centerY - popupHeight / 2);
                                popup.show(primaryStage);
                            }
                        });
                    }
                    running = false;
                } else {
                    try {
                        if (DatabaseWrapper.getInstance().getConnection().isClosed()) {
                            running = true;
                            DatabaseWrapper.getInstance().reconnect();
                            System.out.println("Internet connection established");
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }

                try {
                    Thread.sleep(checkIntervalMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Internet-Monitor-Thread").start();
    }

    public void stop() {
        isEnd = true;
    }

    private boolean hasInternetConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://www.google.com"))
                    .timeout(Duration.ofSeconds(4))
                    .GET()
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}