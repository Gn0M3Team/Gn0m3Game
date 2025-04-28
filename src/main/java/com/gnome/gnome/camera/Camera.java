package com.gnome.gnome.camera;
import com.gnome.gnome.continueGame.component.Coin;
import com.gnome.gnome.dao.ArmorDAO;
import com.gnome.gnome.dao.MapDAO;
import com.gnome.gnome.dao.PotionDAO;
import com.gnome.gnome.dao.WeaponDAO;
import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.models.Armor;
import com.gnome.gnome.models.Potion;
import com.gnome.gnome.models.Weapon;
import com.gnome.gnome.player.Player;
import com.gnome.gnome.userState.UserState;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;

/**
 * The Camera class is responsible for rendering a 15x15 viewport of the map,
 * centered around a specific point, typically the player's position.
 * It only shows a portion of the full map to simulate a camera view.
 */
@Data
public class Camera {
    private int[][] mapGrid; // A two-dimensional array representing the game card. Each element of the array is an integer,
                             // corresponding to a particular type of tile (for example, 0 for floor, -1 for goblin, etc.)
    private int cameraCenterX; //    X-coordinate of the camera centre (in tiles, not pixels). This is usually the player's X position
    private int cameraCenterY; // Y coordinate of the camera centre (in tiles). This is usually the player's Y position
    private final int viewportSize = 20; // The size of the viewport in tiles. The camera always displays a 15x15 tile square
                                         // This means that we see 15 tiles in width and 15 in height
    private int startRow; // The starting line (Y) of the map, from which we start drawing tiles in the preview window
    private int startCol; // The initial column (X) of the map from which we start drawing tiles in the preview window
    private Player player; // A reference to the Player object. We use it to know where the player is,
                           // and centre the camera on his position, as well as highlight his tile on the map

    // A static variable for caching images:
    private static final Map<String, Image> imageCache = new HashMap<>();   // Cache for images. We store tile images
                                                                            // (e.g. floor texture) in this dictionary (Map),
                                                                            // so that we don't have to load them from files every time we draw a map.
                                                                            // The key is the path to the image (String), the value is the Image object itself

    private static Camera instance;
    private double dynamicTileSize;


    private Weapon weapon;
    private Armor armor;
    private Potion potion;

    private Image weaponImage;
    private Image armorImage;
    private Image potionImage;


    private double tileWidth, tileHeight;


    /**
     * Constructor of the Camera class. Used to create a new Camera object.
     *
     * @param fieldMap A two-dimensional array representing the game map (with tiles, monsters, etc.).
     * @param cameraCenterX The initial X coordinate of the camera centre (usually the player's position).
     * @param cameraCenterY Initial Y coordinate of the camera centre (usually the player's position).
     * @param player A player object so we can keep track of the player's position.
     */
    private Camera(int[][] fieldMap, int cameraCenterX, int cameraCenterY, Player player, Armor armor, Weapon weapon, Potion potion) {
        this.mapGrid = fieldMap; // Initialise the map passed as a parameter
        this.cameraCenterX = cameraCenterX; // Set the initial X-coordinate of the camera centre
        this.cameraCenterY = cameraCenterY; // Set the initial Y-coordinate of the camera center
        this.player = player; // Save the reference to the player object
        this.armor = armor;
        this.weapon = weapon;
        this.potion = potion;
    }

    /**
     * Singleton access method for the Camera.
     */
    public static Camera getInstance(int[][] fieldMap, int cameraCenterX, int cameraCenterY, Player player, Armor armor, Weapon weapon, Potion potion) {
        if (instance == null) {
            instance = new Camera(fieldMap, cameraCenterX, cameraCenterY, player, armor, weapon, potion);
        }
        return instance;
    }

    /**
     * Reset instance
     */
    public static void resetInstance() {
        instance = null;
    }


    /**
     * The drawViewport method is responsible for drawing the visible part of the map (the 15x15 tile viewport) on the Canvas.
     * It also draws the coins that are in the visible area and highlights the player's tile with a yellow border.
     *
     * @param canvas The Canvas object on which to draw the map (this is the area in JavaFX where you can draw graphics).
     * @param coins A list of coins to display if they are in the visible area.
     */
    public void drawViewport(Canvas canvas, List<Coin> coins) {
        // Підрахунок ширини і висоти одного тайла
        double tileWidth = canvas.getWidth() / viewportSize;
        double tileHeight = canvas.getHeight() / viewportSize;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.dynamicTileSize = Math.min(tileWidth, tileHeight); // Для гравця

        GraphicsContext gc = canvas.getGraphicsContext2D();

        int totalRows = mapGrid.length;
        int totalCols = mapGrid[0].length;

        int half = viewportSize / 2;

        startRow = Math.max(0, Math.min(cameraCenterY - half, totalRows - viewportSize));
        startCol = Math.max(0, Math.min(cameraCenterX - half, totalCols - viewportSize));

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int i = 0; i < viewportSize; i++) {
            for (int j = 0; j < viewportSize; j++) {
                int row = startRow + i;
                int col = startCol + j;

                double x = j * tileWidth;
                double y = i * tileHeight;

                if (row >= 0 && row < totalRows && col >= 0 && col < totalCols) {
                    TypeOfObjects type = TypeOfObjects.fromValue(mapGrid[row][col]);
                    Image tileImage = getCachedImage(type.getImagePath());

                    if (tileImage != null) {
                        gc.drawImage(tileImage, x, y, tileWidth, tileHeight); // ⚡ правильні пропорції
                    } else {
                        gc.setFill(Color.GRAY);
                        gc.fillRect(x, y, tileWidth, tileHeight);
                    }
                } else {
                    Image outOfMapImage = getCachedImage(TypeOfObjects.MOUNTAIN.getImagePath());
                    if (outOfMapImage != null) {
                        gc.drawImage(outOfMapImage, x, y, tileWidth, tileHeight);
                    } else {
                        gc.setFill(Color.DARKGRAY);
                        gc.fillRect(x, y, tileWidth, tileHeight);
                    }
                }

                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
                gc.strokeRect(x, y, tileWidth, tileHeight);
            }
        }

        // Малюємо монети
        for (Coin coin : coins) {
            int gx = coin.getGridX();
            int gy = coin.getGridY();

            if (gx >= startCol && gx < startCol + viewportSize &&
                    gy >= startRow && gy < startRow + viewportSize) {

                Image img = coin.getImageView().getImage();
                double w = coin.getImageView().getFitWidth();
                double h = coin.getImageView().getFitHeight();

                double ox = (tileWidth - w) / 2;
                double oy = (tileHeight - h) / 2;
                double px = (gx - startCol) * tileWidth + ox;
                double py = (gy - startRow) * tileHeight + oy;

                gc.drawImage(img, px, py, w, h);
            }
        }

//        System.out.println("/com/gnome/gnome/images/tiles/" + armor.getImg() );
//        System.out.println("/com/gnome/gnome/images/tiles/" + weapon.getImg());
//        System.exit(1);

        armorImage = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/com/gnome/gnome/images/tiles/" + armor.getImg() + ".png")
                )
        );

        weaponImage = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/com/gnome/gnome/images/tiles/" + weapon.getImg() + ".png")
                )
        );

        potionImage = new Image(
                Objects.requireNonNull(
                        getClass().getResourceAsStream("/com/gnome/gnome/images/tiles/" + weapon.getImg() + ".png")
                )
        );



        // Малюємо кнопки Armor / Sword
        double boxSize = canvas.getWidth() * 0.08;
        double padding = canvas.getWidth() * 0.02;
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        gc.setFill(Color.color(0, 0, 0, 0.5));
        gc.fillRect(
                canvasWidth - boxSize - padding - 10,
                canvasHeight - boxSize * 2 - padding - 30,
                boxSize + 20,
                boxSize * 2 + 40
        );

        gc.drawImage(
                armorImage,
                canvasWidth - boxSize - padding,
                canvasHeight - boxSize * 2 - padding - 20,
                boxSize,
                boxSize
        );

        gc.drawImage(
                weaponImage,
                canvasWidth - boxSize - padding,
                canvasHeight - boxSize - padding,
                boxSize,
                boxSize
        );

        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(16));

        gc.fillText(
                armor.getNameEng(),
                canvasWidth - boxSize - padding + 10,
                canvasHeight - boxSize * 2 - padding + 10
        );
        gc.fillText(
                weapon.getNameEng(),
                canvasWidth - boxSize - padding + 10,
                canvasHeight - boxSize - padding + 10
        );
    }



    /**
     * Returns a cached image; if it’s not already loaded, loads it from the resource.
     *
     * @param imagePath the path to the image
     * @return the cached Image
     */
    private Image getCachedImage(String imagePath) {
        // Use the computeIfAbsent method for caching:
        // - If the image with this path is already in the imageCache, we return it
        // - If the image is not available, we download it from resources and add it to the cache
        return imageCache.computeIfAbsent(imagePath, path ->
                // Load the image from resources (the file must be in the resources folder)
                // Objects.requireNonNull throws an exception if the image is not found
                new Image(Objects.requireNonNull(
                        TypeOfObjects.class.getResourceAsStream(path)
                )));
    }


    /**
     * Updates the camera center to follow the player's position.
     */
    public void updateCameraCenter() {
        // Update the coordinates of the camera centre, setting them equal to the player's position
        cameraCenterX = player.getX(); // X-coordinate of the player
        cameraCenterY = player.getY(); // Y-coordinate of the player

        // Call the clampCameraCentre method to make sure that the camera does not extend beyond the map
        clampCameraCenter();
    }

    /**
     * Ensures the camera's center stays within the valid map area
     * so that the viewport doesn't go out of bounds.
     */
    private void clampCameraCenter() {
        int halfViewport = viewportSize / 2; // Calculate half the size of the viewport (viewportSize / 2)

        int totalCols = mapGrid[0].length; // Number of columns
        int totalRows = mapGrid.length; // Number of lines

        // Bound cameraCenterX so that the viewport does not extend beyond the map:
        // - Math.max(half, ...) ensures that the centre is not too close to the left edge (so as not to show negative coordinates)
        // - Math.min(..., totalCols - viewportSize + half) ensures that the centre is not too close to the right edge
        cameraCenterX = Math.max(halfViewport, Math.min(cameraCenterX, totalCols - halfViewport));
        cameraCenterY = Math.max(halfViewport, Math.min(cameraCenterY, totalRows - halfViewport));
    }

}
