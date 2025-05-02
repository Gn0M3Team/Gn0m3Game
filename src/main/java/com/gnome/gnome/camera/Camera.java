package com.gnome.gnome.camera;
import com.gnome.gnome.game.component.Chest;
import com.gnome.gnome.game.component.Coin;
import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.models.Armor;
import com.gnome.gnome.models.Potion;
import com.gnome.gnome.models.Weapon;
import com.gnome.gnome.player.Player;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import lombok.Data;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The Camera class is responsible for rendering a 15x15 viewport of the map,
 * centered around a specific point, typically the player's position.
 * It only shows a portion of the full map to simulate a camera view.
 */
@Data
public class Camera {
    private static final int VIEWPORT_SIZE = 20;
    private static final double ITEM_BOX_EXTRA_HEIGHT = 40;
    private static final double COIN_BOX_WIDTH_MULTIPLIER = 2.5;

    private static final Map<String, Image> imageCache = new HashMap<>();
    private static Camera instance;

    private final int[][] mapGrid;
    private final Player player;
    private final Armor armor;
    private final Weapon weapon;
    private final Potion potion;

    private Image armorImage;
    private Image weaponImage;
    private Image potionImage;

    private int cameraCenterX, cameraCenterY, startRow, startCol;
    private double tileWidth, tileHeight, dynamicTileSize;

    private Camera(int[][] map, int centerX, int centerY, Player player, Armor armor, Weapon weapon, Potion potion) {
        this.mapGrid = map;
        this.cameraCenterX = centerX;
        this.cameraCenterY = centerY;
        this.player = player;
        this.armor = armor;
        this.weapon = weapon;
        this.potion = potion;
    }

    public static Camera getInstance(int[][] map, int centerX, int centerY, Player player, Armor armor, Weapon weapon, Potion potion) {
        if (instance == null) {
            instance = new Camera(map, centerX, centerY, player, armor, weapon, potion);
        }
        return instance;
    }

    public static Camera getInstance() {
        if (instance == null) throw new IllegalStateException("Camera not initialized");
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    public void updateCameraCenter() {
        cameraCenterX = player.getX();
        cameraCenterY = player.getY();
        clampCameraCenter();
    }

    private void clampCameraCenter() {
        int half = VIEWPORT_SIZE / 2;
        int maxX = mapGrid[0].length - half;
        int maxY = mapGrid.length - half;
        cameraCenterX = Math.max(half, Math.min(cameraCenterX, maxX));
        cameraCenterY = Math.max(half, Math.min(cameraCenterY, maxY));
    }

    public void drawPressEHints(GraphicsContext gc, int [][] baseMap, int px, int py, List<Chest> activeChests) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        double tw = getTileWidth();
        double th = getTileHeight();

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.setFill(Color.WHITE);

        for (int[] d : directions) {
            int nx = px + d[0];
            int ny = py + d[1];

            if (nx >= 0 && nx < baseMap[0].length && ny >= 0 && ny < baseMap.length) {
                TypeOfObjects tileType = TypeOfObjects.fromValue(baseMap[ny][nx]);
                boolean isTable = tileType == TypeOfObjects.TABLE;
                boolean isChest = activeChests.stream().anyMatch(c -> c.getGridX() == nx && c.getGridY() == ny && !c.isOpened());

                if (isTable || isChest) {
                    int dx = nx - getStartCol();
                    int dy = ny - getStartRow();

                    if (dx >= 0 && dx < VIEWPORT_SIZE && dy >= 0 && dy < VIEWPORT_SIZE) {
                        double x = dx * tw + tw / 2;
                        double y = dy * th - th * 0.4;

                        gc.setFill(Color.rgb(0, 0, 0, 0.7));
                        gc.fillRoundRect(x - 30, y + 14, 60, 20, 5, 5);

                        gc.setFill(Color.WHITE);
                        gc.fillText("Press E", x - 20, y + 22);
                    }
                }
            }
        }
    }

    public void drawViewport(Canvas canvas, List<Coin> coins) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        this.tileWidth = canvas.getWidth() / VIEWPORT_SIZE;
        this.tileHeight = canvas.getHeight() / VIEWPORT_SIZE;
        this.dynamicTileSize = Math.min(tileWidth, tileHeight);

        int half = VIEWPORT_SIZE / 2;
        startRow = Math.max(0, Math.min(cameraCenterY - half, mapGrid.length - VIEWPORT_SIZE));
        startCol = Math.max(0, Math.min(cameraCenterX - half, mapGrid[0].length - VIEWPORT_SIZE));

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int row = 0; row < VIEWPORT_SIZE; row++) {
            for (int col = 0; col < VIEWPORT_SIZE; col++) {
                int mapRow = startRow + row;
                int mapCol = startCol + col;

                double x = col * tileWidth;
                double y = row * tileHeight;

                TypeOfObjects tileType = (mapRow >= 0 && mapRow < mapGrid.length && mapCol >= 0 && mapCol < mapGrid[0].length)
                        ? TypeOfObjects.fromValue(mapGrid[mapRow][mapCol])
                        : TypeOfObjects.MOUNTAIN;

                Image img = getCachedTileImage(tileType.getImagePath());
                if (img != null) {
                    gc.drawImage(img, x, y, tileWidth, tileHeight);
                } else {
                    gc.setFill(Color.GRAY);
                    gc.fillRect(x, y, tileWidth, tileHeight);
                }

                gc.setStroke(Color.BLACK);
                gc.strokeRect(x, y, tileWidth, tileHeight);
            }
        }

        drawCoins(gc, coins);
    }

    private void drawCoins(GraphicsContext gc, List<Coin> coins) {
        for (Coin coin : coins) {
            int x = coin.getGridX() - startCol;
            int y = coin.getGridY() - startRow;
            if (x >= 0 && x < VIEWPORT_SIZE && y >= 0 && y < VIEWPORT_SIZE) {
                double imgX = x * tileWidth + (tileWidth - coin.getImageView().getFitWidth()) / 2;
                double imgY = y * tileHeight + (tileHeight - coin.getImageView().getFitHeight()) / 2;
                gc.drawImage(coin.getImageView().getImage(), imgX, imgY,
                        coin.getImageView().getFitWidth(), coin.getImageView().getFitHeight());
            }
        }
    }

    public Image getCachedItemImage(String path) {
        return imageCache.computeIfAbsent(path, key -> {
            InputStream is = Camera.class.getResourceAsStream("/" + path);
            if (is == null) {
                System.err.println("Item image not found: " + path);
                return null;
            }
            return new Image(is);
        });
    }

    // Для тайлів (tile images)
    private Image getCachedTileImage(String path) {
        return imageCache.computeIfAbsent(path, key -> {
            InputStream is = TypeOfObjects.class.getResourceAsStream(path);
            if (is == null) {
                System.err.println("Tile image not found: " + path);
                return null;
            }
            return new Image(is);
        });
    }

}
