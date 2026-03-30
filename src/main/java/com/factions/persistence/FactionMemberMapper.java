package com.factions.persistence;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data mapper for faction members.
 */
public class FactionMemberMapper {

    private static final Logger LOGGER = Logger.getLogger(FactionMemberMapper.class.getName());
    private final DatabaseManager db;

    public FactionMemberMapper(DatabaseManager db) {
        this.db = db;
    }

    public void insert(UUID factionId, UUID playerId, String role, String joinedAt,
                       String lastOnline, String lastIp, double contributedPower, boolean banned) throws SQLException {
        String sql = "INSERT INTO faction_members (faction_id, player_id, role, joined_at, " +
                     "last_online, last_ip, contributed_power, banned) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionId.toString());
            stmt.setString(2, playerId.toString());
            stmt.setString(3, role);
            stmt.setString(4, joinedAt);
            stmt.setString(5, lastOnline);
            stmt.setString(6, lastIp);
            stmt.setDouble(7, contributedPower);
            stmt.setBoolean(8, banned);
            stmt.executeUpdate();
        }
    }

    public void update(UUID factionId, UUID playerId, String role, String lastOnline,
                       String lastIp, double contributedPower, boolean banned) throws SQLException {
        String sql = "UPDATE faction_members SET role = ?, last_online = ?, " +
                     "last_ip = ?, contributed_power = ?, banned = ? " +
                     "WHERE faction_id = ? AND player_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, role);
            stmt.setString(2, lastOnline);
            stmt.setString(3, lastIp);
            stmt.setDouble(4, contributedPower);
            stmt.setBoolean(5, banned);
            stmt.setString(6, factionId.toString());
            stmt.setString(7, playerId.toString());
            stmt.executeUpdate();
        }
    }

    public void delete(UUID factionId, UUID playerId) throws SQLException {
        String sql = "DELETE FROM faction_members WHERE faction_id = ? AND player_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionId.toString());
            stmt.setString(2, playerId.toString());
            stmt.executeUpdate();
        }
    }

    public boolean exists(UUID factionId, UUID playerId) throws SQLException {
        String sql = "SELECT 1 FROM faction_members WHERE faction_id = ? AND player_id = ? LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionId.toString());
            stmt.setString(2, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public String getRole(UUID factionId, UUID playerId) throws SQLException {
        String sql = "SELECT role FROM faction_members WHERE faction_id = ? AND player_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionId.toString());
            stmt.setString(2, playerId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        }
        return null;
    }

    public void updateRole(UUID factionId, UUID playerId, String newRole) throws SQLException {
        String sql = "UPDATE faction_members SET role = ? WHERE faction_id = ? AND player_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newRole);
            stmt.setString(2, factionId.toString());
            stmt.setString(3, playerId.toString());
            stmt.executeUpdate();
        }
    }
}
