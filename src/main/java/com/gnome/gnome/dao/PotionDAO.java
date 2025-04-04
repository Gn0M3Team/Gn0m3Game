package com.gnome.gnome.dao;

import com.gnome.gnome.db.DatabaseWrapper;
import com.gnome.gnome.exceptions.DataAccessException;
import com.gnome.gnome.models.Potion;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object for managing Potion entities in the database.
 */
public class PotionDAO extends BaseDAO<Potion> {

    private final DatabaseWrapper db = DatabaseWrapper.getInstance();

    /**
     * Maps a ResultSet row to a Potion object.
     *
     * @param rs the ResultSet containing the potion data
     * @return the mapped Potion object
     * @throws SQLException if mapping fails
     */
    @Override
    protected Potion mapResultSet(ResultSet rs) throws SQLException {
        return new Potion(
                rs.getInt("id"),
                rs.getFloat("buff_val"),
                rs.getInt("score_val"),
                rs.getFloat("cost"),
                rs.getString("name_eng"),
                rs.getString("name_sk"),
                rs.getString("details_eng"),
                rs.getString("details_sk")
        );
    }

    /**
     * Inserts a new Potion into the database within a transaction and retrieves the generated ID.
     *
     * @param potion the Potion object to insert
     * @return the Potion object with the generated ID
     * @throws DataAccessException if the insertion fails
     */
    public Potion insertPotion(Potion potion) {
        try {
            db.beginTransaction();
            String sql = "INSERT INTO \"Potion\" (buff_val, score_val, cost, name_eng, name_sk, details_eng, details_sk) VALUES (?, ?, ?, ?, ?, ?, ?)";
            int rowsAffected = executeUpdate(sql, potion.getBuffVal(), potion.getScoreVal(), potion.getCost(),
                    potion.getNameEng(), potion.getNameSk(), potion.getDetailsEng(), potion.getDetailsSk());

            if (rowsAffected == 1) {
                ResultSet generatedKeys = db.executeQuery("SELECT LASTVAL()");
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    potion.setId(generatedId); // Update the potion object with the new ID
                }
                closeResultSet(generatedKeys); // Ensure resource is closed
            }

            db.commitTransaction();
            return potion;
        } catch (SQLException | DataAccessException e) {
            db.rollBackTransaction();
            throw new DataAccessException("Failed to insert potion", e);
        }
    }

    /**
     * Deletes a Potion from the database by its ID.
     *
     * @param id the ID of the Potion to delete
     * @throws DataAccessException if the deletion fails
     */
    public void deletePotion(int id) {
        String sql = "DELETE FROM \"Potion\" WHERE id = ?";
        executeUpdate(sql, id);
    }

    /**
     * Retrieves a Potion from the database by its ID.
     *
     * @param id the ID of the Potion to retrieve
     * @return the Potion object, or null if not found
     * @throws DataAccessException if retrieval fails
     */
    public Potion getPotionById(int id) {
        String sql = "SELECT * FROM \"Potion\" WHERE id = ?";
        List<Potion> potions = findAll(sql, id);
        return potions.isEmpty() ? null : potions.get(0);
    }

    /**
     * Retrieves all Potions from the database.
     *
     * @return a list of all Potion objects
     * @throws DataAccessException if retrieval fails
     */
    public List<Potion> getAllPotions() {
        String sql = "SELECT * FROM \"Potion\"";
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
                // Silently ignore; this is cleanup
            }
        }
    }
}