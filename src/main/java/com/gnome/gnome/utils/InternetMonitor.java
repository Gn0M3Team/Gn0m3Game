package com.gnome.gnome.utils;

import com.gnome.gnome.switcher.switcherPage.PageSwitcherInterface;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class InternetMonitor {
    private final PageSwitcherInterface pageSwitcher;
    private final long checkIntervalMs;
    private volatile boolean running = true;

    private final HttpClient httpClient;

    public InternetMonitor(PageSwitcherInterface pageSwitcher, long checkIntervalMs) {
        this.pageSwitcher = pageSwitcher;
        this.checkIntervalMs = checkIntervalMs;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    public void start() {
        new Thread(() -> {
            while (running) {
                boolean connected = hasInternetConnection();
                if (!connected) {
                    running = false;
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Connection Lost");
                        alert.setHeaderText(null);
                        alert.setContentText("Internet connection lost. You will be redirected to login.");
                        alert.setOnHidden(e -> pageSwitcher.goToBeginning());
                        alert.show();
                    });
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
        running = false;
    }

    private boolean hasInternetConnection() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://www.google.com"))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
