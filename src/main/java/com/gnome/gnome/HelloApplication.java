package com.gnome.gnome;

import com.gnome.gnome.config.EditorLogger;
import com.gnome.gnome.db.DatabaseWrapper;
import com.gnome.gnome.utils.ImageParser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;

public class HelloApplication extends Application {
    private boolean skip_db;
    @Override
    public void start(Stage stage) throws IOException {
        // If parameter skip_log is true then start with menu page, otherwise start with logging
        Map<String, Boolean> properties = getProperties();
        skip_db = properties.get("skip_db");

        FXMLLoader fxmlLoader = getFxmlLoader(properties.get("skip_logging"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setFullScreen(true);
        stage.setTitle("Dark Rifter");
        stage.setScene(scene);
        stage.show();
    }


    public static void startImageParser() {
        try {
            ImageParser.splitImage("com/gnome/gnome/images/colored_pagit cked.png");
            System.out.println("✅ Image parsed successfully!");
        } catch (IOException e) {
            System.out.println("❌ Failed to parse image: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private Map<String, Boolean> getProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("app.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Could not find app.properties in classpath");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Could not load properties file", e);
        }
        return Map.of("skip_logging", Boolean.parseBoolean(properties.getProperty("app.skip_login")),
                "skip_db", Boolean.parseBoolean(properties.getProperty("app.skip_db")));
    }


    private FXMLLoader getFxmlLoader(boolean skip_logging){
        FXMLLoader fxmlLoader;
        if (skip_logging)
            fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("pages/main-menu.fxml"));
        else {
            connect_db();
            fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("pages/login-registration.fxml"));
        }
        return fxmlLoader;
    }


    private void connect_db() {
        if (skip_db) {
            Connection conn = DatabaseWrapper.getInstance().getConnection();

            if (conn != null) {
                System.out.println("✅ Database connection successful!");
            } else {
                System.out.println("❌ Failed to connect to database.");
            }
        }
    }


    @Override
    public void stop() {
       if (!skip_db)
           DatabaseWrapper.getInstance().close();
    }


    public static void main(String[] args) throws IOException {
        EditorLogger.configureLogger();
        launch(args);
    }
}