package com.gnome.gnome;

import com.gnome.gnome.config.EditorLogger;
import com.gnome.gnome.dao.MonsterDAO;
import com.gnome.gnome.db.DatabaseWrapper;
import com.gnome.gnome.loginRegistration.controller.LoginRegistrationController;
import com.gnome.gnome.music.MusicWizard;
import com.gnome.gnome.switcher.switcherPage.SwitchPage;
import com.gnome.gnome.switcher.switcherPage.component.SceneSwitch;
import com.gnome.gnome.utils.ImageParser;
import com.gnome.gnome.utils.InternetMonitor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

public class MainApplication extends Application {
    private boolean skip_db;

    public static boolean musicEnabled = true;
    public static boolean ambientEnabled = true;
    private InternetMonitor internetMonitor;

    @Getter
    private static char lang = 'S';

    @Getter
    private static ResourceBundle langBundle = ResourceBundle.getBundle("slovak");

    public void start(Stage stage) throws IOException {

        MusicWizard.start_music_loop();
//        MusicWizard.start_ambient();

        Map<String, Boolean> properties = getProperties();
        skip_db = properties.get("skip_db");
        System.out.println(skip_db);

        boolean skipLogging = properties.get("skip_logging");

//        skipLogging = true;

        FXMLLoader fxmlLoader = getFxmlLoader(skipLogging);
        Parent root = fxmlLoader.load();

        if (skipLogging) {
            MainController controller = fxmlLoader.getController();
            controller.setPrimaryStage(stage);
        }

        internetMonitor = new InternetMonitor(new SwitchPage(), 5000);
        internetMonitor.start();

        Scene scene = new Scene(root);
        stage.setFullScreen(true);
//        stage.setFullScreenExitHint("");
//        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setTitle("Dark Rifter");
        stage.setScene(scene);


        double fixedWidth = 1200;
        double fixedHeight = 900;

        stage.setMinWidth(fixedWidth);
        stage.setMinHeight(fixedHeight);
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

    public static void flipLanguage(){
        if (lang == 'S'){
            langBundle = ResourceBundle.getBundle("english");
            lang = 'E';
            return;
        }
        langBundle = ResourceBundle.getBundle("slovak");
        lang = 'S';
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
        if (skip_logging) {
            fxmlLoader = new FXMLLoader(MainApplication.class.getResource("pages/main-menu.fxml"));

            if (MainApplication.lang == 'S'){
                fxmlLoader.setResources(ResourceBundle.getBundle("slovak"));
            }
            else{
                fxmlLoader.setResources(ResourceBundle.getBundle("english"));
            }

        }
        else {
            connect_db();
            fxmlLoader = new FXMLLoader(MainApplication.class.getResource("pages/login-registration.fxml"));
        }
        return fxmlLoader;
    }


    private void connect_db() {
        if (!skip_db) {
            try {
                Connection conn = DatabaseWrapper.getInstance().getConnection();
                if (conn != null) {
                    System.out.println("✅ Database connection successful!");
                } else {
                    System.out.println("❌ Failed to connect to database.");
                }
            } catch (Exception e) {
                System.err.println("❌ Database error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    @Override
    public void stop() {
       if (internetMonitor != null)
           internetMonitor.stop();

       if (!skip_db)
           DatabaseWrapper.getInstance().close();

       MusicWizard.stop_music();
    }


    public static void main(String[] args) throws IOException {
        EditorLogger.configureLogger();
        launch(args);
    }
}