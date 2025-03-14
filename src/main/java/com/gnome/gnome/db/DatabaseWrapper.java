package com.gnome.gnome.db;

import lombok.Getter;

import java.sql.*;

@Getter
public class DatabaseWrapper {
    private static final String URL = "jdbc:postgresql://trolley.proxy.rlwy.net:49768/railway";
    private static final String USER = "postgres";
    private static final String PASSWORD = "HAbrSBnCGAspMNgomaOkXxNHwGZLHgVP";

    private static DatabaseWrapper instance;
    private final Connection connection;

    private DatabaseWrapper() {
        try {
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static synchronized DatabaseWrapper getInstance() {
        if (instance == null) {
            instance = new DatabaseWrapper();
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