package com.factions.persistence;

import com.factions.api.Claim;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data mapper for Claim entities.
 */
public class ClaimMapper {

    private static final Logger LOGGER = Logger.getLogger(ClaimMapper.class.getName());
    private final DatabaseManager db;

    public ClaimMapper(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Inserts a new claim.
     */
    public void insert(Claim claim) throws SQLException {
        String sql = "INSERT INTO claims (id, faction_id, world, chunk_x, chunk_z, " +
                     "claimed_at, claimed_by) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String claimId = claim.getWorld() + ":" + claim.getChunkX() + ":" + claim.getChunkZ();
            stmt.setString(1, claimId);
            stmt.setString(2, claim.getFactionId().toString());
            stmt.setString(3, claim.getWorld());
            stmt.setInt(4, claim.getChunkX());
            stmt.setInt(5, claim.getChunkZ());
            stmt.setLong(6, claim.getClaimedAt());
            stmt.setString(7, claim.getClaimedBy());
            stmt.executeUpdate();
        }
    }

    /**
     * Updates a claim (primarily the claimed_by field).
     */
    public void update(Claim claim) throws SQLException {
        String sql = "UPDATE claims SET claimed_by = ?, claimed_at = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String claimId = claim.getWorld() + ":" + claim.getChunkX() + ":" + claim.getChunkZ();
            stmt.setString(1, claim.getClaimedBy());
            stmt.setLong(2, claim.getClaimedAt());
            stmt.setString(3, claimId);
            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a claim by its coordinates.
     */
    public void delete(String world, int chunkX, int chunkZ) throws SQLException {
        String sql = "DELETE FROM claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, world);
            stmt.setInt(2, chunkX);
            stmt.setInt(3, chunkZ);
            stmt.executeUpdate();
        }
    }

    /**
     * Finds a claim by world and chunk coordinates.
     */
    public Optional<Claim> findByChunk(String world, int chunkX, int chunkZ) throws SQLException {
        String sql = "SELECT * FROM claims WHERE world = ? AND chunk_x = ? AND chunk_z = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, world);
            stmt.setInt(2, chunkX);
            stmt.setInt(3, chunkZ);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Claim claim = mapRow(rs);
                    return Optional.of(claim);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds all claims for a faction.
     */
    public List<Claim> findByFaction(UUID factionId) throws SQLException {
        String sql = "SELECT * FROM claims WHERE faction_id = ? ORDER BY world, chunk_x, chunk_z";
        List<Claim> claims = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    claims.add(mapRow(rs));
                }
            }
        }
        return claims;
    }

    /**
     * Finds all claims in a world.
     */
    public List<Claim> findByWorld(String world) throws SQLException {
        String sql = "SELECT * FROM claims WHERE world = ? ORDER BY chunk_x, chunk_z";
        List<Claim> claims = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, world);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    claims.add(mapRow(rs));
                }
            }
        }
        return claims;
    }

    /**
     * Counts the number of claims for a faction.
     */
    public int countByFaction(UUID factionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM claims WHERE faction_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    /**
     * Checks if a chunk is claimed by any faction.
     */
    public boolean isClaimed(String world, int chunkX, int chunkZ) throws SQLException {
        String sql = "SELECT 1 FROM claims WHERE world = ? AND chunk_x = ? AND chunk_z = ? LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, world);
            stmt.setInt(2, chunkX);
            stmt.setInt(3, chunkZ);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Finds all claims that intersect a rectangular region.
     */
    public List<Claim> findInRegion(String world, int minX, int minZ, int maxX, int maxZ) throws SQLException {
        String sql = "SELECT * FROM claims WHERE world = ? AND " +
                     "chunk_x BETWEEN ? AND ? AND chunk_z BETWEEN ? AND ?";
        List<Claim> claims = new ArrayList<>();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, world);
            stmt.setInt(2, minX >> 4); // Convert block to chunk
            stmt.setInt(3, maxX >> 4);
            stmt.setInt(4, minZ >> 4);
            stmt.setInt(5, maxZ >> 4);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    claims.add(mapRow(rs));
                }
            }
        }
        return claims;
    }

    /**
     * Maps a result set row to a Claim object.
     */
    private Claim mapRow(ResultSet rs) throws SQLException {
        String id = rs.getString("id");
        String[] parts = id.split(":");
        if (parts.length != 3) {
            throw new SQLException("Invalid claim ID format: " + id);
        }

        UUID factionId = UUID.fromString(rs.getString("faction_id"));
        Claim claim = new Claim(factionId, parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        claim.setClaimedAt(rs.getLong("claimed_at"));
        claim.setClaimedBy(rs.getString("claimed_by"));
        return claim;
    }
}
