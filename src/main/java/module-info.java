module com.gnome.gnome {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires static lombok;

    opens com.gnome.gnome to javafx.fxml;
    exports com.gnome.gnome;
}