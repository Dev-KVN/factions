package com.factions.persistence;

import com.factions.api.Bounty;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data mapper for Bounty entities.
 * Handles CRUD operations for bounties in the database.
 */
public class BountyMapper {

    private static final Logger LOGGER = Logger.getLogger(BountyMapper.class.getName());
    private final DatabaseManager db;

    public BountyMapper(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Inserts a new bounty into the database.
     */
    public void insert(Bounty bounty) throws SQLException {
        String sql = "INSERT INTO bounties (id, target_faction_id, placer_faction_id, amount, placed_at) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bounty.getId().toString());
            stmt.setString(2, bounty.getTargetFactionId().toString());
            stmt.setString(3, bounty.getPlacerFactionId().toString());
            stmt.setDouble(4, bounty.getAmount());
            stmt.setLong(5, bounty.getPlacedAt());

            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a bounty by ID.
     */
    public void delete(UUID bountyId) throws SQLException {
        String sql = "DELETE FROM bounties WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, bountyId.toString());
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes all bounties for a specific target faction.
     * @return number of rows deleted
     */
    public int deleteByTarget(UUID targetFactionId) throws SQLException {
        String sql = "DELETE FROM bounties WHERE target_faction_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, targetFactionId.toString());
            return stmt.executeUpdate();
        }
    }

    /**
     * Deletes all bounties placed by a specific faction.
     * @return number of rows deleted
     */
    public int deleteByPlacer(UUID placerFactionId) throws SQLException {
        String sql = "DELETE FROM bounties WHERE placer_faction_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, placerFactionId.toString());
            return stmt.executeUpdate();
        }
    }

    /**
     * Finds all active bounties for a target faction.
     */
    public List<Bounty> findByTargetFactionId(UUID targetFactionId) throws SQLException {
        String sql = "SELECT * FROM bounties WHERE target_faction_id = ?";
        List<Bounty> bounties = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, targetFactionId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bounties.add(mapRow(rs));
                }
            }
        }
        return bounties;
    }

    /**
     * Finds all bounties placed by a specific faction.
     */
    public List<Bounty> findByPlacerFactionId(UUID placerFactionId) throws SQLException {
        String sql = "SELECT * FROM bounties WHERE placer_faction_id = ?";
        List<Bounty> bounties = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, placerFactionId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bounties.add(mapRow(rs));
                }
            }
        }
        return bounties;
    }

    /**
     * Maps a ResultSet row to a Bounty object.
     */
    private Bounty mapRow(ResultSet rs) throws SQLException {
        Bounty bounty = new Bounty();
        bounty.setId(UUID.fromString(rs.getString("id")));
        bounty.setTargetFactionId(UUID.fromString(rs.getString("target_faction_id")));
        bounty.setPlacerFactionId(UUID.fromString(rs.getString("placer_faction_id")));
        bounty.setAmount(rs.getDouble("amount"));
        bounty.setPlacedAt(rs.getLong("placed_at"));
        return bounty;
    }
}
