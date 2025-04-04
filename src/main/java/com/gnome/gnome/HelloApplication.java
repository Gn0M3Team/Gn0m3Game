package com.gnome.gnome;

import com.gnome.gnome.config.EditorLogger;
import com.gnome.gnome.dao.MonsterDAO;
import com.gnome.gnome.db.DatabaseWrapper;
import com.gnome.gnome.models.Monster;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.util.Objects;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("pages/hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/com/gnome/gnome/pages/css/style.css")).toExternalForm()
        );

        stage.setFullScreen(true);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        Connection conn = DatabaseWrapper.getInstance().getConnection();

        if (conn != null) {
            System.out.println("✅ Database connection successful!");
        } else {
            System.out.println("❌ Failed to connect to database.");
        }

        MonsterDAO monsterDAO = new MonsterDAO();

        Monster monster = monsterDAO.getMonsterById(1);
        System.out.println(monster);
    }

    @Override
    public void stop() {
        DatabaseWrapper.getInstance().close();
    }


    public static void main(String[] args) throws IOException {
        EditorLogger.configureLogger();
        launch();
    }
}