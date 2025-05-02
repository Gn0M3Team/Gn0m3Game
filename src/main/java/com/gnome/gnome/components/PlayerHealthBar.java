package com.gnome.gnome.components;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import lombok.Getter;

public class PlayerHealthBar extends StackPane {
    private final Rectangle background;
    private final Rectangle foreground;
    private final DoubleProperty healthFraction;

    /**
     * Creates a new health bar with the specified width and height.
     *
     * @param width  the total width of the health bar
     * @param height the total height of the health bar
     */
    public PlayerHealthBar(double width, double height) {
        setPrefSize(width, height);
        getStyleClass().add("player-health-bar"); // Apply the player-health-bar style class

        background = new Rectangle(width, height);
        background.getStyleClass().add("background"); // Apply CSS class

        foreground = new Rectangle(width, height);
        foreground.getStyleClass().add("foreground"); // Apply CSS class

        healthFraction = new SimpleDoubleProperty(1.0);
        foreground.widthProperty().bind(background.widthProperty().multiply(healthFraction));

        getChildren().addAll(background, foreground);
        getStylesheets().add(getClass().getResource("/com/gnome/gnome/pages/css/continueGame.css").toExternalForm());
    }

    /**
     * Updates the health fraction.
     * Ensures that the fraction remains between 0 and 1.
     *
     * @param fraction the new health fraction (0.0 to 1.0)
     */
    public void setHealthFraction(double fraction) {
        healthFraction.set(Math.max(0, Math.min(fraction, 1.0)));
    }

    /**
     * Returns the current health fraction.
     *
     * @return health fraction value.
     */
    public double getHealthFraction() {
        return healthFraction.get();
    }

    /**
     * Returns the health fraction property.
     * Useful if you want to bind it to other properties.
     *
     * @return the health fraction property.
     */
    public DoubleProperty healthFractionProperty() {
        return healthFraction;
    }
}