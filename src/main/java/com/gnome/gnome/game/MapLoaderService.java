package com.gnome.gnome.game;

import com.gnome.gnome.dao.MapDAO;

import com.gnome.gnome.dao.MonsterDAO;
import com.gnome.gnome.models.Map;

import com.gnome.gnome.models.Monster;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MapLoaderService {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MapDAO mapDAO;
    private final MonsterDAO monsterDAO;

    public MapLoaderService(MapDAO mapDAO, MonsterDAO monsterDAO) {
        this.mapDAO = mapDAO;
        this.monsterDAO = monsterDAO;
    }

    public void loadMapAsync(int mapId,
                             BiConsumer<Map, List<Monster>> onSuccess,
                             Consumer<Exception> onError) {
        executor.submit(() -> {
            try {
                Map map = mapDAO.getMapById(mapId);
                List<Monster> monsters = monsterDAO.getAllMonsters();

                if (map != null) {
                    onSuccess.accept(map, monsters);
                } else {
                    onError.accept(new RuntimeException("No map found with ID: " + mapId));
                }
            } catch (Exception e) {
                onError.accept(e);
            }
        });
    }

}
