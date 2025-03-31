package com.gnome.gnome.db;

import com.gnome.gnome.annotations.config.Value;
import com.gnome.gnome.utils.annotation.MyValueInjection;
import lombok.Getter;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class DatabaseWrapper {
    private final String URL;
    private final String USER;
    private final String PASSWORD;

    private static final Logger logger = Logger.getLogger(DatabaseWrapper.class.getName());

    private static DatabaseWrapper instance;
    private final Connection connection;

    private DatabaseWrapper(@Value("app.url") String url,
                            @Value("app.user") String user,
                            @Value("app.password") String password) {
        URL = url;
        USER = user;
        PASSWORD = password;
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static synchronized DatabaseWrapper getInstance() {
        if (instance == null) {
            instance = MyValueInjection.getInstance().createInstance(DatabaseWrapper.class);
        }
        return instance;
    }

    public int executeUpdate(String sql, Object... params) {
        try (PreparedStatement stmt = prepareStatement(sql, params)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Query execution failed: " + sql, e);
        }
    }

    public ResultSet executeQuery(String sql, Object... params) {
        try {
            PreparedStatement stmt = prepareStatement(sql, params);
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException("Query execution failed: " + sql, e);
        }
    }

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

    public void close() {
        try {
            if (connection != null) {
                connection.close();
                instance = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}