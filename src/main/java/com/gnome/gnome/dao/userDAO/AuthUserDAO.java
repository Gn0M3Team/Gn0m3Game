package com.gnome.gnome.dao.userDAO;

import com.gnome.gnome.dao.BaseDAO;
import com.gnome.gnome.db.DatabaseWrapper;
import com.gnome.gnome.exceptions.DataAccessException;
import com.gnome.gnome.models.user.AuthUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object for managing AuthUser entities in the database.
 * Provides basic CRUD operations for authentication data in the "Users" table.
 */
public class AuthUserDAO extends BaseDAO<AuthUser> {

    private final DatabaseWrapper db = DatabaseWrapper.getInstance();

    /**
     * Maps a ResultSet row to an AuthUser object, focusing on authentication fields.
     *
     * @param rs the ResultSet containing the user authentication data
     * @return the mapped AuthUser object
     * @throws SQLException if mapping fails
     */
    @Override
    protected AuthUser mapResultSet(ResultSet rs) throws SQLException {
        return new AuthUser(
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("role")
        );
    }

    /**
     * Inserts a new AuthUser into the database within a transaction.
     * Note: Username is assumed to be the unique identifier.
     *
     * @param authUser the AuthUser object to insert
     * @throws DataAccessException if the insertion fails
     */
    public void insertAuthUser(AuthUser authUser) {
        try {
            db.beginTransaction();
            String sql = "INSERT INTO \"Users\" (username, password, role) VALUES (?, ?, ?)";
            executeUpdate(sql, authUser.getUsername(), authUser.getPassword(), authUser.getRole());
            db.commitTransaction();
        } catch (DataAccessException e) {
            db.rollBackTransaction();
            throw e;
        }
    }

    /**
     * Deletes an AuthUser from the database by their username.
     *
     * @param username the username of the AuthUser to delete
     * @throws DataAccessException if the deletion fails
     */
    public void deleteAuthUser(String username) {
        String sql = "DELETE FROM \"Users\" WHERE username = ?";
        executeUpdate(sql, username);
    }

    /**
     * Retrieves an AuthUser from the database by their username.
     *
     * @param username the username of the AuthUser to retrieve
     * @return the AuthUser object, or null if not found
     * @throws DataAccessException if retrieval fails
     */
    public AuthUser getAuthUserByUsername(String username) {
        String sql = "SELECT username, password, role FROM \"Users\" WHERE username = ?";
        List<AuthUser> authUsers = findAll(sql, username);
        return authUsers.isEmpty() ? null : authUsers.get(0);
    }

    /**
     * Retrieves all AuthUsers from the database.
     *
     * @return a list of all AuthUser objects
     * @throws DataAccessException if retrieval fails
     */
    public List<AuthUser> getAllAuthUsers() {
        String sql = "SELECT username, password, role FROM \"Users\"";
        return findAll(sql);
    }

    /**
     * Updates an AuthUser's data in the database.
     *
     * @param authUser the AuthUser object with updated data
     * @throws DataAccessException if the update fails
     */
    public void updateAuthUser(AuthUser authUser) {
        try {
            db.beginTransaction();
            String sql = "UPDATE \"Users\" SET password = ?, role = ? WHERE username = ?";
            executeUpdate(sql, authUser.getPassword(), authUser.getRole(), authUser.getUsername());
            db.commitTransaction();
        } catch (DataAccessException e) {
            db.rollBackTransaction();
            throw e;
        }
    }
}