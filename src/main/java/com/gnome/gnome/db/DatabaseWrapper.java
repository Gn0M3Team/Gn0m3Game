package com.gnome.gnome.db;

import com.gnome.gnome.annotations.config.Value;
import com.gnome.gnome.exceptions.DataAccessException;
import com.gnome.gnome.utils.annotation.MyValueInjection;
import lombok.Getter;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A singleton wrapper class for managing database connections and executing SQL queries.
 * Provides methods for executing updates, queries, and managing transactions.
 */
@Getter
public class DatabaseWrapper {
    private final String URL;
    private final String USER;
    private final String PASSWORD;

    private static final Logger logger = Logger.getLogger(DatabaseWrapper.class.getName());

    private static DatabaseWrapper instance;
    private final Connection connection;

    /**
     * Constructs a new DatabaseWrapper with the specified database connection details.
     *
     * @param url      the database URL
     * @param user     the database username
     * @param password the database password
     * @throws DataAccessException if the database connection fails
     */
    private DatabaseWrapper(@Value("app.url") String url,
                            @Value("app.user") String user,
                            @Value("app.password") String password) {
        URL = url;
        USER = user;
        PASSWORD = password;
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to connect to database", e);
        }
    }

    /**
     * Returns the singleton instance of DatabaseWrapper. Creates a new instance if it doesn't exist.
     *
     * @return the singleton DatabaseWrapper instance
     * @throws DataAccessException if instance creation fails
     */
    public static synchronized DatabaseWrapper getInstance() {
        if (instance == null) {
            try {
                instance = MyValueInjection.getInstance().createInstance(DatabaseWrapper.class);
            } catch (Exception e) {
                throw new DataAccessException("Failed to create DatabaseWrapper instance", e);
            }
        }
        return instance;
    }

    /**
     * Executes an SQL update statement with the given parameters.
     *
     * @param sql    the SQL update statement
     * @param params the parameters to bind to the prepared statement
     * @return the number of rows affected
     * @throws DataAccessException if the query execution fails
     */
    public int executeUpdate(String sql, Object... params) {
        try (PreparedStatement stmt = prepareStatement(sql, params)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Query execution failed: " + sql, e);
        }
    }

    /**
     * Executes an SQL query with the given parameters and returns the result set.
     * The caller is responsible for closing the returned ResultSet.
     *
     * @param sql    the SQL query
     * @param params the parameters to bind to the prepared statement
     * @return the ResultSet containing the query results
     * @throws DataAccessException if the query execution fails
     */
    public ResultSet executeQuery(String sql, Object... params) {
        try {
            PreparedStatement stmt = prepareStatement(sql, params);
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new DataAccessException("Query execution failed: " + sql, e);
        }
    }

    /**
     * Prepares a SQL statement with the given parameters.
     *
     * @param sql    the SQL statement
     * @param params the parameters to bind
     * @return the prepared statement
     * @throws SQLException if statement preparation fails
     */
    private PreparedStatement prepareStatement(String sql, Object... params) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
        return stmt;
    }

    /**
     * method for starting transaction in manual transaction management invoked from
     * AOP class before any database update is done
     */
    public void beginTransaction() {
        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Cannot begin transaction");
        }
    }

    /**
     * method for commit transaction in manual transaction management invoked from
     * AOP class after all database update is done
     */
    public void commitTransaction() {
        try {
            connection.commit();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Cannot commit transaction: ", e.getCause().toString());
        }
    }

    /**
     * method for rollback transaction in manual transaction management invoked from
     * AOP class if there is any exception while database update is done
     */
    public void rollBackTransaction() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Cannot rollback transaction: " + e.getCause().toString());
        }
    }

    /**
     * Closes the database connection and resets the singleton instance.
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                instance = null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to close database connection", e);
        }
    }
}