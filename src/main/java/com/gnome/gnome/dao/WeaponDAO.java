package com.gnome.gnome.dao;

import com.gnome.gnome.db.DatabaseWrapper;
import com.gnome.gnome.exceptions.DataAccessException;
import com.gnome.gnome.models.Weapon;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object for managing Weapon entities in the database.
 */
public class WeaponDAO extends BaseDAO<Weapon> {

    private final DatabaseWrapper db = DatabaseWrapper.getInstance();

    /**
     * Maps a ResultSet row to a Weapon object.
     *
     * @param rs the ResultSet containing the weapon data
     * @return the mapped Weapon object
     * @throws SQLException if mapping fails
     */
    @Override
    protected Weapon mapResultSet(ResultSet rs) throws SQLException {
        return new Weapon(
                rs.getInt("id"),
                rs.getFloat("atk_value"),
                rs.getFloat("cost"),
                rs.getString("name_eng"),
                rs.getString("name_sk"),
                rs.getString("details_eng"),
                rs.getString("details_sk")
        );
    }

    /**
     * Inserts a new Weapon into the database within a transaction and retrieves the generated ID.
     *
     * @param weapon the Weapon object to insert
     * @return the Weapon object with the generated ID
     * @throws DataAccessException if the insertion fails
     */
    public Weapon insertWeapon(Weapon weapon) {
        try {
            db.beginTransaction();
            String sql = "INSERT INTO \"Weapon\" (atk_value, cost, name_eng, name_sk, details_eng, details_sk) VALUES (?, ?, ?, ?, ?, ?)";
            int rowsAffected = executeUpdate(sql, weapon.getAtkValue(), weapon.getCost(), weapon.getNameEng(),
                    weapon.getNameSk(), weapon.getDetailsEng(), weapon.getDetailsSk());

            if (rowsAffected == 1) {
                // Retrieve the generated ID (assuming PostgreSQL or similar with LASTVAL)
                ResultSet generatedKeys = db.executeQuery("SELECT LASTVAL()");
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    weapon.setId(generatedId); // Update the weapon object with the new ID
                }
                closeResultSet(generatedKeys); // Ensure resource is closed
            }

            db.commitTransaction();
            return weapon;
        } catch (SQLException e) {
            db.rollBackTransaction();
            throw new DataAccessException("Failed to insert weapon", e);
        }
    }

    /**
     * Deletes a Weapon from the database by its ID.
     *
     * @param id the ID of the Weapon to delete
     * @throws DataAccessException if the deletion fails
     */
    public void deleteWeapon(int id) {
        String sql = "DELETE FROM \"Weapon\" WHERE id = ?";
        executeUpdate(sql, id);
    }

    /**
     * Retrieves a Weapon from the database by its ID.
     *
     * @param id the ID of the Weapon to retrieve
     * @return the Weapon object, or null if not found
     * @throws DataAccessException if retrieval fails
     */
    public Weapon getWeaponById(int id) {
        String sql = "SELECT * FROM \"Weapon\" WHERE id = ?";
        List<Weapon> weapons = findAll(sql, id);
        return weapons.isEmpty() ? null : weapons.get(0);
    }

    /**
     * Retrieves a Weapon from the database by its English name.
     * <p>
     * This method queries the "Weapon" table for a row where the name_eng column matches the provided name.
     * If multiple weapons have the same name_eng, only the first match is returned.
     * The search is case-sensitive due to PostgreSQL's string comparison behavior.
     *
     * @param nameEng the English name of the weapon to retrieve (e.g., "Dragon Sword")
     * @return the Weapon object with the matching name_eng, or null if not found
     * @throws DataAccessException if retrieval fails due to a database error
     */
    public Weapon getWeaponByNameEng(String nameEng) {
        String sql = "SELECT * FROM \"Weapon\" WHERE name_eng = ?";
        List<Weapon> weapons = findAll(sql, nameEng);
        return weapons.isEmpty() ? null : weapons.get(0);
    }

    /**
     * Retrieves all Weapons from the database.
     *
     * @return a list of all Weapon objects
     * @throws DataAccessException if retrieval fails
     */
    public List<Weapon> getAllWeapons() {
        String sql = "SELECT * FROM \"Weapon\"";
        return findAll(sql);
    }

    /**
     * Closes the ResultSet to free resources.
     *
     * @param rs the ResultSet to close
     */
    private void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // ignore; this is cleanup
            }
        }
    }
}