package com.gnome.gnome.dao.userDAO;

import com.gnome.gnome.dao.BaseDAO;
import com.gnome.gnome.db.DatabaseWrapper;
import com.gnome.gnome.exceptions.DataAccessException;
import com.gnome.gnome.models.user.AuthUser;
import com.gnome.gnome.models.user.PlayerRole;

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
                PlayerRole.fromString(rs.getString("role"))
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
            executeUpdate(sql, authUser.getUsername(), authUser.getPassword(), authUser.getRole().toString());
            db.commitTransaction();
        } catch (DataAccessException e) {
            db.rollBackTransaction();
            throw e;
        }
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
     * Retrieves a paginated list of users from the database.
     *
     * @param offset the starting point in the list of users (number of rows to skip).
     * @param limit the maximum number of users to retrieve.
     * @return a list of AuthUser objects within the specified page range.
     *
     * The users are ordered alphabetically by username.
     */
    public List<AuthUser> getUsersByPage(int offset, int limit) {
        String sql = "SELECT username, password, role FROM \"Users\" ORDER BY username LIMIT ? OFFSET ?";
        return findAll(sql, limit, offset);
    }

    /**
     * Updates the role of a specific user.
     *
     * @param authUser the username of the user whose role is being updated and  the new role to set
     * @throws DataAccessException if the update fails
     */
    public void updateUserRole(AuthUser authUser) {
        try {
            db.beginTransaction();
            String sql = "UPDATE \"Users\" SET role = ? WHERE username = ?";
            executeUpdate(sql, authUser.getRole().toString(), authUser.getUsername());
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
    public boolean deleteUserByUsername(String username) {
        String sql = "DELETE FROM \"Users\" WHERE username = ?";
        try {
            int rowsAffected = executeUpdate(sql, username);
            return rowsAffected > 0;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
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