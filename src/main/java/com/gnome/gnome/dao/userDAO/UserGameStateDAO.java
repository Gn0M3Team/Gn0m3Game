package com.gnome.gnome.dao.userDAO;

import com.gnome.gnome.dao.BaseDAO;
import com.gnome.gnome.db.DatabaseWrapper;
import com.gnome.gnome.exceptions.DataAccessException;
import com.gnome.gnome.models.user.UserGameState;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserGameStateDAO extends BaseDAO<UserGameState> {

    private final DatabaseWrapper db = DatabaseWrapper.getInstance();

    /**
     * Maps a ResultSet row to a UserGameState object, focusing on game state fields.
     *
     * @param rs the ResultSet containing the user game state data
     * @return the mapped UserGameState object
     * @throws SQLException if mapping fails
     */
    @Override
    protected UserGameState mapResultSet(ResultSet rs) throws SQLException {
        return new UserGameState(
                rs.getString("username"),
                rs.getFloat("balance"),
                rs.getFloat("health"),
                rs.getInt("score"),
                rs.getInt("death_counter"),
                rs.getInt("map_level"),
                (Integer) rs.getObject("weapon_id"), // Nullable
                (Integer) rs.getObject("potion_id"), // Nullable
                (Integer) rs.getObject("armor_id")  // Nullable
        );
    }

    /**
     * Inserts a new UserGameState into the database within a transaction.
     * Note: Username links to an existing AuthUser.
     *
     * @param userGameState the UserGameState object to insert
     * @throws DataAccessException if the insertion fails
     */
    public void insertUserGameState(UserGameState userGameState) {
        try {
            db.beginTransaction();
            String sql = "INSERT INTO \"Users\" (username, balance, health, score, death_counter, map_level, weapon_id, potion_id, armor_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            executeUpdate(sql, userGameState.getUsername(), userGameState.getBalance(), userGameState.getHealth(),
                    userGameState.getScore(), userGameState.getDeathCounter(), userGameState.getMapLevel(),
                    userGameState.getWeaponId(), userGameState.getPotionId(), userGameState.getArmorId());
            db.commitTransaction();
        } catch (DataAccessException e) {
            db.rollBackTransaction();
            throw e;
        }
    }

    /**
     * Deletes a UserGameState from the database by their username.
     *
     * @param username the username of the UserGameState to delete
     * @throws DataAccessException if the deletion fails
     */
    public void deleteUserGameState(String username) {
        String sql = "DELETE FROM \"Users\" WHERE username = ?";
        executeUpdate(sql, username);
    }

    /**
     * Retrieves a UserGameState from the database by their username.
     *
     * @param username the username of the UserGameState to retrieve
     * @return the UserGameState object, or null if not found
     * @throws DataAccessException if retrieval fails
     */
    public UserGameState getUserGameStateByUsername(String username) {
        String sql = "SELECT username, balance, health, score, death_counter, map_level, weapon_id, potion_id, armor_id FROM \"Users\" WHERE username = ?";
        List<UserGameState> userGameStates = findAll(sql, username);
        return userGameStates.isEmpty() ? null : userGameStates.get(0);
    }

    /**
     * Retrieves all UserGameStates from the database.
     *
     * @return a list of all UserGameState objects
     * @throws DataAccessException if retrieval fails
     */
    public List<UserGameState> getAllUserGameStates() {
        String sql = "SELECT username, balance, health, score, death_counter, map_level, weapon_id, potion_id, armor_id FROM \"Users\"";
        return findAll(sql);
    }

    /**
     * Updates a UserGameState's data in the database.
     *
     * @param userGameState the UserGameState object with updated data
     * @throws DataAccessException if the update fails
     */
    public void updateUserGameState(UserGameState userGameState) {
        try {
            db.beginTransaction();
            String sql = "UPDATE \"Users\" SET balance = ?, health = ?, score = ?, death_counter = ?, map_level = ?, " +
                    "weapon_id = ?, potion_id = ?, armor_id = ? WHERE username = ?";
            executeUpdate(sql, userGameState.getBalance(), userGameState.getHealth(), userGameState.getScore(),
                    userGameState.getDeathCounter(), userGameState.getMapLevel(),
                    userGameState.getWeaponId(), userGameState.getPotionId(), userGameState.getArmorId(),
                    userGameState.getUsername());
            db.commitTransaction();
        } catch (DataAccessException e) {
            db.rollBackTransaction();
            throw e;
        }
    }
}