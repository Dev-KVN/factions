package com.factions.persistence;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data mapper for player power data.
 */
public class PlayerPowerMapper {

    private static final Logger LOGGER = Logger.getLogger(PlayerPowerMapper.class.getName());
    private final DatabaseManager db;

    public PlayerPowerMapper(DatabaseManager db) {
        this.db = db;
    }

    public void insert(UUID playerId, double power, double maxPower, long lastUpdate) throws SQLException {
        String sql = "INSERT INTO player_power (player_id, power, max_power, last_update, death_count) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerId.toString());
            stmt.setDouble(2, power);
            stmt.setDouble(3, maxPower);
            stmt.setLong(4, lastUpdate);
            stmt.setInt(5, 0); // initial death count = 0
            stmt.executeUpdate();
        }
    }

    public void update(UUID playerId, double power, double maxPower, long lastUpdate, int deathCount) throws SQLException {
        String sql = "UPDATE player_power SET power = ?, max_power = ?, last_update = ?, death_count = ? WHERE player_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, power);
            stmt.setDouble(2, maxPower);
            stmt.setLong(3, lastUpdate);
            stmt.setInt(4, deathCount);
            stmt.setString(5, playerId.toString());
            stmt.executeUpdate();
        }
    }

    public int getDeathCount(UUID playerId) throws SQLException {
        String sql = "SELECT death_count FROM player_power WHERE player_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("death_count");
                }
            }
        }
        return 0;
    }

    public double getPower(UUID playerId) throws SQLException {
        String sql = "SELECT power FROM player_power WHERE player_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("power");
                }
            }
        }
        return 0.0;
    }

    public double getMaxPower(UUID playerId) throws SQLException {
        String sql = "SELECT max_power FROM player_power WHERE player_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("max_power");
                }
            }
        }
        return 1000.0; // Default max power
    }

    public boolean exists(UUID playerId) throws SQLException {
        String sql = "SELECT 1 FROM player_power WHERE player_id = ? LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void delete(UUID playerId) throws SQLException {
        String sql = "DELETE FROM player_power WHERE player_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerId.toString());
            stmt.executeUpdate();
        }
    }
}
