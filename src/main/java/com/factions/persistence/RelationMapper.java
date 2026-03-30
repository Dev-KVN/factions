package com.factions.persistence;

import com.factions.api.RelationState;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data mapper for faction relations.
 */
public class RelationMapper {

    private static final Logger LOGGER = Logger.getLogger(RelationMapper.class.getName());
    private final DatabaseManager db;

    public RelationMapper(DatabaseManager db) {
        this.db = db;
    }

    public void insert(UUID factionA, UUID factionB, RelationState state,
                       String establishedAt, String establishedBy) throws SQLException {
        String sql = "INSERT INTO relations (faction_a, faction_b, type, established_at, established_by) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionA.toString());
            stmt.setString(2, factionB.toString());
            stmt.setString(3, state.name());
            stmt.setString(4, establishedAt);
            stmt.setString(5, establishedBy);
            stmt.executeUpdate();
        }
    }

    public void update(UUID factionA, UUID factionB, RelationState state) throws SQLException {
        String sql = "UPDATE relations SET type = ? WHERE faction_a = ? AND faction_b = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, state.name());
            stmt.setString(2, factionA.toString());
            stmt.setString(3, factionB.toString());
            stmt.executeUpdate();
        }
    }

    public void delete(UUID factionA, UUID factionB) throws SQLException {
        String sql = "DELETE FROM relations WHERE faction_a = ? AND faction_b = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionA.toString());
            stmt.setString(2, factionB.toString());
            stmt.executeUpdate();
        }
    }

    public RelationState findRelation(UUID factionA, UUID factionB) throws SQLException {
        String sql = "SELECT type FROM relations WHERE faction_a = ? AND faction_b = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionA.toString());
            stmt.setString(2, factionB.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return RelationState.valueOf(rs.getString("type"));
                }
            }
        }
        return RelationState.NEUTRAL;
    }
}
