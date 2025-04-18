package com.gnome.gnome.game_objects;

import com.gnome.gnome.camera.Camera;
import com.gnome.gnome.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CameraTest {

    private int[][] testMap;
    private Player player;
    private Camera camera;

    @BeforeEach
    void setUp() {
        testMap = new int[30][30];
        player = new Player(15, 15, 100);
        camera = new Camera(testMap, 15, 15, player);
    }

    @Test
    void testCameraFollowsPlayer() {
        player.moveRight();
        player.moveDown();
        camera.updateCameraCenter();


        assertEquals(player.getX(), camera.getCameraCenterX());
        assertEquals(player.getY(), camera.getCameraCenterY());
    }


    @Test
    void testCameraClampBoundaries() {
        player.reset(0, 0); // top-left corner
        camera.updateCameraCenter();

        // Viewport should not go out of bounds
        assertTrue(camera.getCameraCenterX() >= camera.getViewportSize() / 2);
        assertTrue(camera.getCameraCenterY() >= camera.getViewportSize() / 2);
    }
}
