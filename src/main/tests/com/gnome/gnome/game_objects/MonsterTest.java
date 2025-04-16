package com.gnome.gnome.game_objects;

import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.MonsterFactory;
import com.gnome.gnome.monsters.movements.MovementStrategy;
import com.gnome.gnome.monsters.types.*;
import javafx.scene.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MonsterTest {

    @Test
    void testTakeDamage() {
        Monster goblin = new Goblin(10, 10);
        int initialHealth = goblin.getHealth();
        goblin.takeDamage(30);
        assertEquals(initialHealth - 30, goblin.getHealth());
    }

    @Test
    void testSetPosition() {
        Monster demon = new Demon(0, 0);
        demon.setPosition(5, 7);
        assertEquals(5, demon.getX());
        assertEquals(7, demon.getY());
    }

    @Test
    void testFactoryCreatesCorrectMonsterTypes() {
        Monster demon = MonsterFactory.createMonster(MonsterFactory.MonsterType.DEMON, 1, 1);
        assertInstanceOf(Demon.class, demon);

        Monster skeleton = MonsterFactory.createMonster(MonsterFactory.MonsterType.SKELETON, 2, 2);
        assertInstanceOf(Skeleton.class, skeleton);

        Monster goblin = MonsterFactory.createMonster(MonsterFactory.MonsterType.GOBLIN, 3, 3);
        assertInstanceOf(Goblin.class, goblin);

        Monster scorpion = MonsterFactory.createMonster(MonsterFactory.MonsterType.SCORPION, 4, 4);
        assertInstanceOf(Scorpion.class, scorpion);

        Monster butterfly = MonsterFactory.createMonster(MonsterFactory.MonsterType.BUTTERFLY, 5, 5);
        assertInstanceOf(Butterfly.class, butterfly);
    }

    @Test
    void testMovementStrategyIsCalled() {
        Monster dummyMonster = new Goblin(0, 0);
        MovementStrategy mockStrategy = m -> m.setPosition(99, 88);
        dummyMonster.setMovementStrategy(mockStrategy);

        dummyMonster.move();

        assertEquals(99, dummyMonster.getX());
        assertEquals(88, dummyMonster.getY());
    }
}
