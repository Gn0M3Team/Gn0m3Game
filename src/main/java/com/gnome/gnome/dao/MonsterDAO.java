package com.gnome.gnome.dao;

import com.gnome.gnome.exceptions.DataAccessException;
import com.gnome.gnome.models.Monster;
import com.gnome.gnome.db.DatabaseWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object for managing Monster entities in the database.
 */
public class MonsterDAO extends BaseDAO<Monster> {

    private final DatabaseWrapper db = DatabaseWrapper.getInstance();

    /**
     * Maps a ResultSet row to a Monster object. Assumes Monster class has fields for all columns.
     *
     * @param rs the ResultSet containing the monster data
     * @return the mapped Monster object
     * @throws SQLException if mapping fails
     */
    @Override
    protected Monster mapResultSet(ResultSet rs) throws SQLException {
        return new Monster(
                rs.getInt("id"),
                rs.getString("name_eng"),
                rs.getString("name_sk"),
                rs.getString("details_eng"),
                rs.getString("details_sk"),
                rs.getFloat("attack"),
                rs.getFloat("health"),
                rs.getFloat("radius"),
                rs.getInt("score_val"),
                rs.getFloat("cost")
        );
    }

    /**
     * Inserts a new Monster into the database within a transaction.
     *
     * @param monster the Monster object to insert
     * @throws DataAccessException if the insertion fails
     */
    public void insertMonster(Monster monster) {
        try {
            db.beginTransaction();
            String sql = "INSERT INTO \"Monsters\" (name_eng, name_sk, details_eng, details_sk, attack, health, radius, score_val, cost) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            executeUpdate(sql, monster.getName(), monster.getName_sk(), monster.getDetails(), monster.getDetails_sk(),
                    monster.getAttack(), monster.getHealth(), monster.getRadius(), monster.getScore_val(), monster.getCost());
            db.commitTransaction();
        } catch (DataAccessException e) {
            db.rollBackTransaction();
            throw e;
        }
    }

    /**
     * Deletes a Monster from the database by its ID.
     *
     * @param id the ID of the Monster to delete
     * @throws DataAccessException if the deletion fails
     */
    public void deleteMonster(int id) {
        String sql = "DELETE FROM \"Monsters\" WHERE id = ?";
        executeUpdate(sql, id);
    }

    /**
     * Retrieves a Monster from the database by its ID.
     *
     * @param id the ID of the Monster to retrieve
     * @return the Monster object, or null if not found
     * @throws DataAccessException if retrieval fails
     */
    public Monster getMonsterById(int id) {
        String sql = "SELECT * FROM \"Monsters\" WHERE id = ?";
        List<Monster> monsters = findAll(sql, id);
        return monsters.isEmpty() ? null : monsters.get(0);
    }

    /**
     * Retrieves a Monster from the database by its name_eng.
     *
     * @param nameEng the name of the Monster to retrieve
     * @return the Monster object, or null if not found
     * @throws DataAccessException if retrieval fails
     */
    public Monster getMonsterByNameEng(String nameEng) {
        String sql = "SELECT * FROM \"Monsters\" WHERE name_eng = ?";
        List<Monster> monsters = findAll(sql, nameEng);
        return monsters.isEmpty() ? null : monsters.get(0);
    }

    /**
     * Retrieves all Monsters from the database.
     *
     * @return a list of all Monster objects
     * @throws DataAccessException if retrieval fails
     */
    public List<Monster> getAllMonsters() {
        String sql = "SELECT * FROM \"Monsters\"";
        return findAll(sql);
    }

}
