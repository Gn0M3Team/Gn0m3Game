package com.gnome.gnome.monsters.types;

import com.gnome.gnome.editor.utils.TypeOfObjects;
import com.gnome.gnome.monsters.Monster;
import com.gnome.gnome.monsters.movements.RandomMovement;
import com.gnome.gnome.monsters.types.missels.Arrow;
import javafx.scene.Node;

import static com.gnome.gnome.editor.utils.EditorConstants.TILE_SIZE;

public class Skeleton extends Monster {

    public Skeleton(int startX, int startY) {
        super(30,
                80,
                50,
                4,
                "Skeleton",
                "Skeleton",
                startX,
                startY,
                TypeOfObjects.SKELETON.getValue(),
                new RandomMovement());
    }

    /**
     * Коли скелет атакує, він стріляє стрілою.
     * У цьому прикладі ми задаємо напрямок стрільби просто як приклад (наприклад, праворуч),
     * проте ти можеш розрахувати ціль (напр., позицію гравця) та використовувати її.
     *
     * @return Node, що представляє стрілу
     */
    @Override
    public Node attack() {
        // Координати скелета в «глобальних» пікселях
        double globalX = this.getX() * TILE_SIZE;
        double globalY = this.getY() * TILE_SIZE;

        // Розміри самої стріли (з прикладу Arrow: 10×3)
        double arrowWidth = 10;
        double arrowHeight = 3;

        // Зміщення, щоб стріла була по центру клітинки
        double offsetX = (TILE_SIZE - arrowWidth) / 2.0;
        double offsetY = (TILE_SIZE - arrowHeight) / 2.0;

        // Тепер стартова координата (верхній лівий кут стріли) буде посередині блоку
        double startX = globalX + offsetX;
        double startY = globalY + offsetY;

        // Напрямок польоту стріли: припустимо, летить праворуч на 5 клітин.
        double targetX = globalX + 5 * TILE_SIZE + offsetX;
        double targetY = globalY + offsetY; // залишаємось по центру по вертикалі

        // Створюємо стрілу і запускаємо анімацію
        Arrow arrow = new Arrow(startX, startY, targetX, targetY, 5);
        arrow.launch();

        return arrow.getNode();
    }
}
