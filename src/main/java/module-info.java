module com.gnome.gnome {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires static lombok;
    requires java.sql;
    requires java.desktop;
    requires java.logging;
    requires org.aspectj.runtime;
    requires software.amazon.awssdk.services.s3;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.http;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.services.omics;
    requires software.amazon.awssdk.http.nio.netty;
    requires software.amazon.awssdk.awscore;
    requires jbcrypt;

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

    opens com.gnome.gnome.shopPopUp to javafx.fxml;
    exports com.gnome.gnome.shopPopUp;

    opens com.gnome.gnome.storyMaps to javafx.fxml;
    exports com.gnome.gnome.storyMaps;

}