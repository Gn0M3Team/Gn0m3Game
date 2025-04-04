package com.gnome.gnome.dao;

import com.gnome.gnome.db.DatabaseWrapper;
import com.gnome.gnome.exceptions.DataAccessException;
import com.gnome.gnome.models.Armor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Data Access Object for managing Armor entities in the database.
 */
public class ArmorDAO extends BaseDAO<Armor> {

    private final DatabaseWrapper db = DatabaseWrapper.getInstance();

    /**
     * Maps a ResultSet row to an Armor object.
     *
     * @param rs the ResultSet containing the armor data
     * @return the mapped Armor object
     * @throws SQLException if mapping fails
     */
    @Override
    protected Armor mapResultSet(ResultSet rs) throws SQLException {
        return new Armor(
                rs.getInt("id"),
                rs.getFloat("def_cof"),
                rs.getInt("health"),
                rs.getFloat("cost"),
                rs.getString("name_eng"),
                rs.getString("name_sk"),
                rs.getString("details_eng"),
                rs.getString("details_sk")
        );
    }

    /**
     * Inserts a new Armor into the database within a transaction and retrieves the generated ID.
     *
     * @param armor the Armor object to insert
     * @return the Armor object with the generated ID
     * @throws DataAccessException if the insertion fails
     */
    public Armor insertArmor(Armor armor) {
        try {
            db.beginTransaction();
            String sql = "INSERT INTO \"Armor\" (def_cof, health, cost, name_eng, name_sk, details_eng, details_sk) VALUES (?, ?, ?, ?, ?, ?, ?)";
            int rowsAffected = executeUpdate(sql, armor.getDefCof(), armor.getHealth(), armor.getCost(),
                    armor.getNameEng(), armor.getNameSk(), armor.getDetailsEng(), armor.getDetailsSk());

            if (rowsAffected == 1) {
                ResultSet generatedKeys = db.executeQuery("SELECT LASTVAL()");
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    armor.setId(generatedId); // Update the armor object with the new ID
                }
                closeResultSet(generatedKeys); // Ensure resource is closed
            }

            db.commitTransaction();
            return armor;
        } catch (SQLException | DataAccessException e) {
            db.rollBackTransaction();
            throw new DataAccessException("Failed to insert armor", e);
        }
    }

    /**
     * Deletes an Armor from the database by its ID.
     *
     * @param id the ID of the Armor to delete
     * @throws DataAccessException if the deletion fails
     */
    public void deleteArmor(int id) {
        String sql = "DELETE FROM \"Armor\" WHERE id = ?";
        executeUpdate(sql, id);
    }

    /**
     * Retrieves an Armor from the database by its ID.
     *
     * @param id the ID of the Armor to retrieve
     * @return the Armor object, or null if not found
     * @throws DataAccessException if retrieval fails
     */
    public Armor getArmorById(int id) {
        String sql = "SELECT * FROM \"Armor\" WHERE id = ?";
        List<Armor> armors = findAll(sql, id);
        return armors.isEmpty() ? null : armors.get(0);
    }

    /**
     * Retrieves all Armors from the database.
     *
     * @return a list of all Armor objects
     * @throws DataAccessException if retrieval fails
     */
    public List<Armor> getAllArmors() {
        String sql = "SELECT * FROM \"Armor\"";
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
