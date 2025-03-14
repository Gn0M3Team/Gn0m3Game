package com.gnome.gnome.dao;

import com.gnome.gnome.db.DatabaseWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO<T> {
    protected abstract T mapResultSet(ResultSet rs) throws SQLException;

    public List<T> findAll(String query, Object... params) {
        List<T> results = new ArrayList<>();
        try (ResultSet rs = DatabaseWrapper.getInstance().executeQuery(query, params)) {
            while (rs.next()) {
                results.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching data", e);
        }
        return results;
    }

    public int executeUpdate(String query, Object... params) {
        return DatabaseWrapper.getInstance().executeUpdate(query, params);
    }
}