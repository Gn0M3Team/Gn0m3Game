module com.gnome.gnome {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires static lombok;
    requires java.sql;

    opens com.gnome.gnome to javafx.fxml;
    exports com.gnome.gnome;
    exports com.gnome.gnome.db;
    opens com.gnome.gnome.db to javafx.fxml;
    exports com.gnome.gnome.dao;
    opens com.gnome.gnome.dao to javafx.fxml;
    exports com.gnome.gnome.models;
    opens com.gnome.gnome.models to javafx.fxml;
}