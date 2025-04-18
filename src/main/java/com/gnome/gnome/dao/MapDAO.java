package com.gnome.gnome.dao;

import com.gnome.gnome.db.DatabaseWrapper;
import com.gnome.gnome.exceptions.DataAccessException;
import com.gnome.gnome.models.Map;
import com.gnome.gnome.utils.MapParser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class MapDAO extends BaseDAO<Map> {

    private final DatabaseWrapper db = DatabaseWrapper.getInstance();

    /**
     * Maps a ResultSet row to a Map object, converting the map_string to a 2D ArrayList.
     *
     * @param rs the ResultSet containing the map data
     * @return the mapped Map object
     * @throws SQLException if mapping fails
     */
    @Override
    protected Map mapResultSet(ResultSet rs) throws SQLException {
        return new Map(
                rs.getString("username"),
                MapParser.convertStringToMap(rs.getString("map_string")),
                rs.getInt("score_val"),
                rs.getString("map_name_eng"),
                rs.getString("map_name_sk"),
                rs.getInt("level")
        );
    }

    /**
     * Inserts a new Map into the database within a transaction and retrieves the generated ID.
     * The map data is converted to a string before storage.
     *
     * @param map the Map object to insert
     * @return the Map object with the generated ID
     * @throws DataAccessException if the insertion fails
     */
    public Map insertMap(Map map) {
        try {
            db.beginTransaction();
            String mapString = MapParser.convertMapToString(map.getMapData());
            if (mapString == null) {
                throw new IllegalArgumentException("mapString cannot be null");
            }
            String sql = "INSERT INTO \"Maps\" (username, map_string, score_val, map_name_eng, map_name_sk, level) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            int rowsAffected = executeUpdate(sql, map.getUsername(), mapString, map.getScoreVal(),
                    map.getMapNameEng(), map.getMapNameSk(), map.getLevel());

            if (rowsAffected != 1) {
                throw new SQLException("Failed to insert map; no rows affected.");
            }

            db.commitTransaction();
            return map;

        } catch (SQLException | DataAccessException e) {
            db.rollBackTransaction();
            throw new DataAccessException("Failed to insert map", e);
        }
    }

    /**
     * Retrieves a Map from the database by its ID.
     *
     * @param id the ID of the Map to retrieve
     * @return the Map object, or null if not found
     * @throws DataAccessException if retrieval fails
     */
    public Map getMapById(int id) {
        String sql = "SELECT * FROM \"Maps\" WHERE map_id = ?";
        List<Map> maps = findAll(sql, id);
        return maps.isEmpty() ? null : maps.get(0);
    }

    /**
     * Retrieves all Maps from the database created by the specified username.
     *
     * @param username the username of the map creator
     * @return a list of Map objects created by the user
     * @throws DataAccessException if retrieval fails
     */
    public List<Map> getMapsByUsername(String username) {
        String sql = "SELECT * FROM \"Maps\" WHERE username = ?";
        return findAll(sql, username);
    }

    /**
     * Retrieves a Map from the database by its name (either map_name_eng or map_name_sk).
     *
     * @param mapName the name of the map to retrieve (English or Slovak)
     * @return the Map object, or null if not found
     * @throws DataAccessException if retrieval fails
     */
    public Map getMapByName(String mapName) {
        String sql = "SELECT * FROM \"Maps\" WHERE map_name_eng = ? OR map_name_sk = ?";
        List<Map> maps = findAll(sql, mapName, mapName);
        return maps.isEmpty() ? null : maps.get(0);
    }

    /**
     * Retrieves all Maps from the database.
     *
     * @return a list of all Map objects, where each map's data is parsed into a 2D int array
     * @throws DataAccessException if retrieval fails
     */
    public List<Map> getAllMaps() {
        String sql = "SELECT * FROM \"Maps\"";
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
                // ignore; this is cleanup
            }
        }
    }
}