package com.gnome.gnome.dao;

import com.gnome.gnome.models.Monster;
import com.gnome.gnome.db.DatabaseWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MonsterDAO extends BaseDAO {

    private final DatabaseWrapper db = DatabaseWrapper.getInstance();

    @Override
    protected Monster mapResultSet(ResultSet rs) throws SQLException {
        return new Monster(
                rs.getInt("id"),
                rs.getString("name_eng"),
                rs.getString("details_eng"),
                rs.getFloat("attack"),
                rs.getFloat("health"),
                rs.getFloat("radius"),
                rs.getInt("score_val"),
                rs.getFloat("cost")
        );
    }

//    public void insertMonster(Monster monster) {
//        String sql = "INSERT INTO \"Monsters\" (name_eng, name_sk, details_eng, details_sk, attack, health, radius, score_val, cost) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
//        executeUpdate(sql, monster.getName(), monster.getDetails(), monster.getAttack(), monster.getHealth(), monster.getRadius(), monster.getScore_val(), monster.getCost());
//    }
//
//    public void deleteMonster(int id) {
//        String sql = "DELETE FROM \"Monsters\" WHERE id = ?";
//        executeUpdate(sql, id);
//    }

    public Monster getMonsterById(int id) {
        String sql = "SELECT * FROM \"Monsters\" WHERE id = ?";
        List<Monster> monsters = findAll(sql, id);
        return monsters.isEmpty() ? null : monsters.get(0);
    }

    public List<Monster> getAllMonsters() {
        String sql = "SELECT * FROM \"Monsters\"";
        return findAll(sql);
    }

}
