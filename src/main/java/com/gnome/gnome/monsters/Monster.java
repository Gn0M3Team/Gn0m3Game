package com.gnome.gnome.monsters;


import com.gnome.gnome.monsters.movements.MovementStrategy;
import javafx.scene.Node;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class Monster {
    protected int attack;
    protected int health;
    protected int cost;
    protected int attackRange;
    protected String nameEng;
    protected String nameSk;

    protected int x;
    protected int y;

    protected int value;

    protected MovementStrategy movementStrategy;

    public abstract Node attack();

    public void move() {
        if (movementStrategy != null)
            movementStrategy.move(this);
    }

    public void setPosition(int newX, int newY) {
        this.x = newX;
        this.y = newY;
    }

    public int getMonsterValue() {
        return value;
    }

    public void takeDamage(int damage) {
        health -= damage;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Attack: %d, Health: %d, Cost: %d, Attack Range: %d, Position: (%d, %d)",
                nameEng, nameSk, attack, health, cost, attackRange, x, y);
    }
}
