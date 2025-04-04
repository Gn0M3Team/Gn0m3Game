package com.gnome.gnome.dao;

import com.gnome.gnome.db.DatabaseWrapper;
import com.gnome.gnome.exceptions.DataAccessException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for Data Access Objects (DAOs) providing common database operations.
 * Subclasses must implement the mapResultSet method to map ResultSet rows to specific objects.
 *
 * @param <T> the type of object managed by this DAO
 */
public abstract class BaseDAO<T> {

    /**
     * Maps a ResultSet row to an object of type T.
     *
     * @param rs the ResultSet containing the data
     * @return the mapped object
     * @throws SQLException if mapping fails
     */
    protected abstract T mapResultSet(ResultSet rs) throws SQLException;

    /**
     * Retrieves all objects matching the given SQL query and parameters.
     * The caller is responsible for closing the ResultSet if needed.
     *
     * @param query  the SQL query to execute
     * @param params the parameters to bind to the query
     * @return a list of objects mapped from the query results
     * @throws DataAccessException if data fetching fails
     */
    public List<T> findAll(String query, Object... params) {
        List<T> results = new ArrayList<>();
        ResultSet rs = null;
        try {
            rs = DatabaseWrapper.getInstance().executeQuery(query, params);
            while (rs.next()) {
                results.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error fetching data for query: " + query, e);
        } finally {
            closeResultSet(rs);
        }
        return results;
    }

    /**
     * Executes an SQL update statement with the given parameters.
     *
     * @param query  the SQL update statement
     * @param params the parameters to bind to the statement
     * @return the number of rows affected
     */
    public int executeUpdate(String query, Object... params) {
        return DatabaseWrapper.getInstance().executeUpdate(query, params);
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