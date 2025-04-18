package com.gnome.gnome.game_objects;

import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.player.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PlayerTest {
    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player(5, 5, 100);
    }

    @Test
    void testMovement() {
        player.moveLeft();
        assertEquals(4, player.getX());

        player.moveRight();
        player.moveRight();
        assertEquals(6, player.getX());

        player.moveUp();
        assertEquals(4, player.getY());

        player.moveDown();
        assertEquals(5, player.getY());
    }

    @Test
    void testTakeDamage() {
        player.takeDamage(30);
        assertEquals(70, player.getCurrentHealth());

        player.takeDamage(100);
        assertEquals(0, player.getCurrentHealth());
    }

    @Test
    void testReset() {
        player.moveLeft();
        player.takeDamage(50);
        player.reset(10, 10);
        assertEquals(10, player.getX());
        assertEquals(10, player.getY());
        assertEquals(100, player.getCurrentHealth());
    }

    @Test
    void testAttackKillsMonster() {
        Monster mockMonster = mock(Monster.class);
        when(mockMonster.getX()).thenReturn(5);
        when(mockMonster.getY()).thenReturn(6);
        when(mockMonster.getHealth()).thenReturn(0); // already dead
        doNothing().when(mockMonster).takeDamage(20);

        List<Monster> killed = player.attack(List.of(mockMonster), 1, 20);

        assertEquals(1, killed.size());
        verify(mockMonster).takeDamage(20);
    }

    @Test
    void testAttackOutOfRange() {
        Monster mockMonster = mock(Monster.class);
        when(mockMonster.getX()).thenReturn(10);
        when(mockMonster.getY()).thenReturn(10);
        when(mockMonster.getHealth()).thenReturn(100);

        List<Monster> killed = player.attack(List.of(mockMonster), 1, 20);

        assertEquals(0, killed.size());
        verify(mockMonster, never()).takeDamage(anyInt());
    }
}
