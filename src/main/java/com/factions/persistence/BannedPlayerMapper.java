package com.factions.persistence;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data mapper for banned players.
 */
public class BannedPlayerMapper {

    private static final Logger LOGGER = Logger.getLogger(BannedPlayerMapper.class.getName());
    private final DatabaseManager db;

    public BannedPlayerMapper(DatabaseManager db) {
        this.db = db;
    }

    public void insert(UUID factionId, UUID playerId, long bannedAt) throws SQLException {
        String sql = "INSERT INTO banned_players (faction_id, player_id, banned_at) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionId.toString());
            stmt.setString(2, playerId.toString());
            stmt.setLong(3, bannedAt);
            stmt.executeUpdate();
        }
    }

    public void delete(UUID factionId, UUID playerId) throws SQLException {
        String sql = "DELETE FROM banned_players WHERE faction_id = ? AND player_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionId.toString());
            stmt.setString(2, playerId.toString());
            stmt.executeUpdate();
        }
    }

    public boolean isBanned(UUID factionId, UUID playerId) throws SQLException {
        String sql = "SELECT 1 FROM banned_players WHERE faction_id = ? AND player_id = ? LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionId.toString());
            stmt.setString(2, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
