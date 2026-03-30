package com.factions.persistence;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data mapper for faction invites.
 */
public class FactionInviteMapper {

    private static final Logger LOGGER = Logger.getLogger(FactionInviteMapper.class.getName());
    private final DatabaseManager db;

    public FactionInviteMapper(DatabaseManager db) {
        this.db = db;
    }

    public void insert(UUID factionId, UUID playerId, long invitedAt) throws SQLException {
        String sql = "INSERT INTO faction_invites (faction_id, player_id, invited_at) VALUES (?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionId.toString());
            stmt.setString(2, playerId.toString());
            stmt.setLong(3, invitedAt);
            stmt.executeUpdate();
        }
    }

    public void delete(UUID factionId, UUID playerId) throws SQLException {
        String sql = "DELETE FROM faction_invites WHERE faction_id = ? AND player_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionId.toString());
            stmt.setString(2, playerId.toString());
            stmt.executeUpdate();
        }
    }

    public void deleteByPlayer(UUID playerId) throws SQLException {
        String sql = "DELETE FROM faction_invites WHERE player_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, playerId.toString());
            stmt.executeUpdate();
        }
    }

    public boolean exists(UUID factionId, UUID playerId) throws SQLException {
        String sql = "SELECT 1 FROM faction_invites WHERE faction_id = ? AND player_id = ? LIMIT 1";
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
