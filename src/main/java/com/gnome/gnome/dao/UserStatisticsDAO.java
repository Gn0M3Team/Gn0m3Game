package com.gnome.gnome.dao;

import com.gnome.gnome.db.DatabaseWrapper;
import com.gnome.gnome.exceptions.DataAccessException;
import com.gnome.gnome.models.UserStatistics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserStatisticsDAO extends BaseDAO<UserStatistics> {

    private final DatabaseWrapper db = DatabaseWrapper.getInstance();

    /**
     * Maps a ResultSet row to a UserStatistics object.
     *
     * @param rs the ResultSet containing the user statistics data
     * @return the mapped UserStatistics object
     * @throws SQLException if mapping fails
     */
    @Override
    protected UserStatistics mapResultSet(ResultSet rs) throws SQLException {
        return new UserStatistics(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getInt("total_maps_played"),
                rs.getInt("total_deaths"),
                rs.getInt("total_monsters_killed"),
                rs.getInt("total_chests_opened")
        );
    }

    /**
     * Inserts a new UserStatistics entry into the database.
     *
     * @param userStatistics the UserStatistics object to insert
     * @throws DataAccessException if the insertion fails
     */
    public void insertUserStatistics(UserStatistics userStatistics) {
        try {
            db.beginTransaction();

            String sql = "INSERT INTO \"UserStatistics\" (username, total_maps_played, total_deaths, " +
                    "total_monsters_killed, total_chests_opened) VALUES (?, ?, ?, ?, ?)";

            int rowsAffected = executeUpdate(sql, userStatistics.getUsername(),
                    userStatistics.getTotalMapsPlayed(),
                    userStatistics.getTotalDeaths(),
                    userStatistics.getTotalMonstersKilled(),
                    userStatistics.getTotalChestsOpened());

            if (rowsAffected != 1) {
                throw new SQLException("Failed to insert user statistics; no rows affected.");
            }

            db.commitTransaction();

        } catch (SQLException | DataAccessException e) {
            db.rollBackTransaction();
            throw new DataAccessException("Failed to insert user statistics", e);
        }
    }

    /**
     * Retrieves a UserStatistics entry by its username.
     *
     * @param username the username of the user whose statistics to retrieve
     * @return the UserStatistics object, or null if not found
     * @throws DataAccessException if retrieval fails
     */
    public UserStatistics getUserStatisticsByUsername(String username) {
        String sql = "SELECT * FROM \"UserStatistics\" WHERE username = ?";
        List<UserStatistics> stats = findAll(sql, username);
        return stats.isEmpty() ? null : stats.get(0);
    }

    /**
     * Retrieves all UserStatistics entries from the database.
     *
     * @return a list of all UserStatistics objects
     * @throws DataAccessException if retrieval fails
     */
    public List<UserStatistics> getAllUserStatistics() {
        String sql = "SELECT * FROM \"UserStatistics\"";
        return findAll(sql);
    }

    /**
     * Updates the UserStatistics for a given username.
     *
     * @param userStatistics the UserStatistics object to update
     * @throws DataAccessException if the update fails
     */
    public void updateUserStatistics(UserStatistics userStatistics) {
        try {
            db.beginTransaction();

            String sql = "UPDATE \"UserStatistics\" SET total_maps_played = ?, total_deaths = ?, " +
                    "total_monsters_killed = ?, total_chests_opened = ? WHERE username = ?";

            int rowsAffected = executeUpdate(sql, userStatistics.getTotalMapsPlayed(),
                    userStatistics.getTotalDeaths(),
                    userStatistics.getTotalMonstersKilled(),
                    userStatistics.getTotalChestsOpened(),
                    userStatistics.getUsername());

            if (rowsAffected != 1) {
                throw new SQLException("Failed to update user statistics; no rows affected.");
            }

            db.commitTransaction();

        } catch (SQLException | DataAccessException e) {
            db.rollBackTransaction();
            throw new DataAccessException("Failed to update user statistics", e);
        }
    }

    /**
     * Deletes the UserStatistics entry by its username.
     *
     * @param username the username of the user whose statistics to delete
     * @throws DataAccessException if deletion fails
     */
    public void deleteUserStatisticsByUsername(String username) {
        try {
            db.beginTransaction();

            String sql = "DELETE FROM \"UserStatistics\" WHERE username = ?";

            int rowsAffected = executeUpdate(sql, username);

            if (rowsAffected != 1) {
                throw new SQLException("Failed to delete user statistics; no rows affected for username: " + username);
            }

            db.commitTransaction();

        } catch (SQLException | DataAccessException e) {
            db.rollBackTransaction();
            throw new DataAccessException("Failed to delete user statistics for username: " + username, e);
        }
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
