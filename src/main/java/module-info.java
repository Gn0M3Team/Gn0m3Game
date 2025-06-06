module com.gnome.gnome {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires static lombok;
    requires java.sql;
    requires java.desktop;
    requires java.logging;
    requires jbcrypt;
    requires java.net.http;
    requires jdk.compiler;

    opens com.gnome.gnome.game to javafx.fxml;

    opens com.gnome.gnome to javafx.fxml;
    exports com.gnome.gnome;
    exports com.gnome.gnome.db;
    opens com.gnome.gnome.db to javafx.fxml;
    exports com.gnome.gnome.dao;
    opens com.gnome.gnome.dao to javafx.fxml;
    exports com.gnome.gnome.models;
    opens com.gnome.gnome.models to javafx.fxml;

    opens com.gnome.gnome.newGame to javafx.fxml;
    exports com.gnome.gnome.newGame;

    opens com.gnome.gnome.editor.controller to javafx.fxml;
    exports com.gnome.gnome.editor.controller;
    opens com.gnome.gnome.editor.javafxobj to javafx.fxml;
    exports com.gnome.gnome.editor.javafxobj;
    opens com.gnome.gnome.editor.utils to javafx.fxml;
    exports com.gnome.gnome.editor.utils;

    opens com.gnome.gnome.profile to javafx.fxml;
    exports com.gnome.gnome.profile;

    exports com.gnome.gnome.dao.userDAO;
    opens com.gnome.gnome.dao.userDAO to javafx.fxml;

    opens com.gnome.gnome.loginRegistration.controller to javafx.fxml;
    exports com.gnome.gnome.loginRegistration.controller;

    opens com.gnome.gnome.setting to javafx.fxml;
    exports com.gnome.gnome.setting;

    opens com.gnome.gnome.storyMaps to javafx.fxml;
    exports com.gnome.gnome.storyMaps;

    opens com.gnome.gnome.inventory to javafx.fxml;
    exports com.gnome.gnome.inventory;

    exports com.gnome.gnome.game.shop.service;
    opens com.gnome.gnome.game.shop.service to javafx.fxml;
    exports com.gnome.gnome.game.shop.controllers;
    opens com.gnome.gnome.game.shop.controllers to javafx.fxml;
}