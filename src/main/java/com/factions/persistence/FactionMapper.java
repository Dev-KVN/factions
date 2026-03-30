package com.factions.persistence;

import com.factions.api.Faction;
import com.factions.api.FactionImpl;
import com.factions.api.FactionMember;
import com.factions.api.RelationState;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data mapper for Faction entities.
 * Handles all CRUD operations for factions in the database.
 */
public class FactionMapper {

    private static final Logger LOGGER = Logger.getLogger(FactionMapper.class.getName());
    private final DatabaseManager db;

    public FactionMapper(DatabaseManager db) {
        this.db = db;
    }

    /**
     * Inserts a new faction into the database.
     */
    public void insert(Faction faction) throws SQLException {
        String sql = "INSERT INTO factions (id, name, tag, description, motd, leader_id, " +
                     "bank_balance, created_at, last_seen, power, max_claims, claim_count, " +
                     "home_world, home_x, home_y, home_z, peaceful, permanent) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, faction.getId().toString());
            stmt.setString(2, faction.getName());
            stmt.setString(3, faction.getTag());
            stmt.setString(4, faction.getDescription());
            stmt.setString(5, faction.getMotd());
            stmt.setString(6, faction.getLeaderId() != null ? faction.getLeaderId().toString() : null);
            stmt.setDouble(7, faction.getBankBalance());
            stmt.setLong(8, faction.getCreatedAt());
            stmt.setLong(9, faction.getLastSeen());
            stmt.setDouble(10, faction.getPower());
            stmt.setInt(11, faction.getMaxClaims());
            stmt.setInt(12, faction.getClaimCount());
            stmt.setString(13, faction.getHomeWorld());
            stmt.setInt(14, faction.getHomeX());
            stmt.setInt(15, faction.getHomeY());
            stmt.setInt(16, faction.getHomeZ());
            stmt.setBoolean(17, faction.isPeaceful());
            stmt.setBoolean(18, faction.isPermanent());

            stmt.executeUpdate();
        }
    }

    /**
     * Updates an existing faction in the database.
     */
    public void update(Faction faction) throws SQLException {
        String sql = "UPDATE factions SET name = ?, tag = ?, description = ?, motd = ?, " +
                     "leader_id = ?, bank_balance = ?, last_seen = ?, power = ?, " +
                     "max_claims = ?, claim_count = ?, home_world = ?, home_x = ?, " +
                     "home_y = ?, home_z = ?, peaceful = ?, permanent = ? " +
                     "WHERE id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, faction.getName());
            stmt.setString(2, faction.getTag());
            stmt.setString(3, faction.getDescription());
            stmt.setString(4, faction.getMotd());
            stmt.setString(5, faction.getLeaderId() != null ? faction.getLeaderId().toString() : null);
            stmt.setDouble(6, faction.getBankBalance());
            stmt.setLong(7, faction.getLastSeen());
            stmt.setDouble(8, faction.getPower());
            stmt.setInt(9, faction.getMaxClaims());
            stmt.setInt(10, faction.getClaimCount());
            stmt.setString(11, faction.getHomeWorld());
            stmt.setInt(12, faction.getHomeX());
            stmt.setInt(13, faction.getHomeY());
            stmt.setInt(14, faction.getHomeZ());
            stmt.setBoolean(15, faction.isPeaceful());
            stmt.setBoolean(16, faction.isPermanent());
            stmt.setString(17, faction.getId().toString());

            stmt.executeUpdate();
        }
    }

    /**
     * Deletes a faction by ID (cascades to related tables via foreign keys).
     */
    public void delete(UUID factionId) throws SQLException {
        String sql = "DELETE FROM factions WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, factionId.toString());
            stmt.executeUpdate();
        }
    }

    /**
     * Finds a faction by ID, including members and claims.
     */
    public Faction findById(UUID factionId) throws SQLException {
        String sql = "SELECT * FROM factions WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, factionId.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                FactionImpl faction = new FactionImpl(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        rs.getString("tag")
                );

                faction.setDescription(rs.getString("description"));
                faction.setMotd(rs.getString("motd"));
                String leaderIdStr = rs.getString("leader_id");
                if (leaderIdStr != null) {
                    faction.setLeaderId(UUID.fromString(leaderIdStr));
                }
                faction.setBankBalance(rs.getDouble("bank_balance"));
                faction.setCreatedAt(rs.getLong("created_at"));
                faction.setLastSeen(rs.getLong("last_seen"));
                faction.setPower(rs.getDouble("power"));
                faction.setMaxClaims(rs.getInt("max_claims"));
                faction.setClaimCount(rs.getInt("claim_count"));
                faction.setHomeWorld(rs.getString("home_world"));
                faction.setHomeX(rs.getInt("home_x"));
                faction.setHomeY(rs.getInt("home_y"));
                faction.setHomeZ(rs.getInt("home_z"));
                faction.setPeaceful(rs.getBoolean("peaceful"));
                faction.setPermanent(rs.getBoolean("permanent"));

                // Load related data
                loadMembers(faction);
                loadInvites(faction);
                loadBanned(faction);
                loadClaims(faction);
                loadRelations(faction);

                return faction;
            }
        }
    }

    /**
     * Finds a faction by tag.
     */
    public Faction findByTag(String tag) throws SQLException {
        String sql = "SELECT id FROM factions WHERE LOWER(tag) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tag);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return findById(UUID.fromString(rs.getString("id")));
                }
            }
        }
        return null;
    }

    /**
     * Finds all factions.
     */
    public List<Faction> findAll() throws SQLException {
        String sql = "SELECT id FROM factions ORDER BY name";
        List<Faction> factions = new ArrayList<>();
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Faction faction = findById(UUID.fromString(rs.getString("id")));
                if (faction != null) {
                    factions.add(faction);
                }
            }
        }
        return factions;
    }

    /**
     * Loads faction members into the faction object.
     */
    private void loadMembers(FactionImpl faction) throws SQLException {
        String sql = "SELECT player_id, role, joined_at, last_online, last_ip, " +
                     "contributed_power, banned FROM faction_members WHERE faction_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, faction.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID playerId = UUID.fromString(rs.getString("player_id"));
                    faction.addMember(playerId);
                    // Member details could be stored separately or in a cache
                }
            }
        }
    }

    /**
     * Loads faction invites into the faction object.
     */
    private void loadInvites(FactionImpl faction) throws SQLException {
        String sql = "SELECT player_id FROM faction_invites WHERE faction_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, faction.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID playerId = UUID.fromString(rs.getString("player_id"));
                    faction.addInvite(playerId);
                }
            }
        }
    }

    /**
     * Loads banned players into the faction object.
     */
    private void loadBanned(FactionImpl faction) throws SQLException {
        String sql = "SELECT player_id FROM banned_players WHERE faction_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, faction.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID playerId = UUID.fromString(rs.getString("player_id"));
                    faction.ban(playerId);
                }
            }
        }
    }

    /**
     * Loads claims into the faction object.
     */
    private void loadClaims(FactionImpl faction) throws SQLException {
        String sql = "SELECT id, world, chunk_x, chunk_z, claimed_at, claimed_by FROM claims WHERE faction_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, faction.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String[] parts = rs.getString("id").split(":");
                    if (parts.length == 3) {
                        String world = rs.getString("world");
                        int chunkX = rs.getInt("chunk_x");
                        int chunkZ = rs.getInt("chunk_z");
                        com.factions.api.Claim claim = new com.factions.api.Claim(
                                faction.getId(), world, chunkX, chunkZ
                        );
                        claim.setClaimedAt(rs.getLong("claimed_at"));
                        claim.setClaimedBy(rs.getString("claimed_by"));
                        faction.addClaim(claim);
                    }
                }
            }
        }
    }

    /**
     * Loads relations into the faction object by directly populating the internal map.
     */
    private void loadRelations(FactionImpl faction) throws SQLException {
        String sql = "SELECT faction_b, type FROM relations WHERE faction_a = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, faction.getId().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID otherId = UUID.fromString(rs.getString("faction_b"));
                    RelationState state = RelationState.valueOf(rs.getString("type"));
                    faction.getRelationsInternal().put(otherId, state);
                }
            }
        }
    }
}
