package com.gnome.gnome.monsters.types.missels;

import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;

/**
 * Represents an arrow projectile that travels in (dx, dy) for 'range' cells.
 * Once the animation finishes, the arrow node is removed from its parent.
 */
public class Arrow {
    // Graphical representation of the arrow (can be replaced with another Node if needed)
    private Rectangle arrowShape;
    // Direction of movement (normalized vector)
    private double dx, dy;
    // Shooting range (in number of tiles)
    private int range;

    /**
     * Constructor for the arrow.
     * @param startX X-coordinate of the start position (in pixels)
     * @param startY Y-coordinate of the start position (in pixels)
     * @param targetX X-coordinate of the target (in pixels)
     * @param targetY Y-coordinate of the target (in pixels)
     * @param range Maximum flight distance (in tiles)
     */
    public Arrow(double startX, double startY, double targetX, double targetY, int range) {
        // Create rectangle as the arrow's visual representation
        arrowShape = new Rectangle(10, 3); // Width and height of the arrow
        arrowShape.setFill(Color.BROWN);
        // Place the arrow at the start coordinates
        arrowShape.setTranslateX(startX);
        arrowShape.setTranslateY(startY);

        // Compute normalized direction vector
        double diffX = targetX - startX;
        double diffY = targetY - startY;
        double distance = Math.sqrt(diffX * diffX + diffY * diffY);
        this.dx = diffX / distance;
        this.dy = diffY / distance;
        this.range = range;
    }

    /**
     * Returns the Node representing the arrow.
     */
    public Node getNode() {
        return arrowShape;
    }

    /**
     * Launches the arrow flight animation.
     * Once the animation is complete, the arrow is removed from its parent pane.
     */
    public void launch() {
        // Calculate flight distance in pixels
        double distanceInPixels = range * TILE_SIZE;

        // Configure animation
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), arrowShape);
        tt.setByX(dx * distanceInPixels);
        tt.setByY(dy * distanceInPixels);

        tt.setOnFinished(event -> {
            if (arrowShape.getParent() instanceof Pane) {
                ((Pane) arrowShape.getParent()).getChildren().remove(arrowShape);
            }
        });

        tt.play();
    }
}
