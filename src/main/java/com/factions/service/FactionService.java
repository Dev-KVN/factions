package com.factions.service;

import com.factions.api.*;
import com.factions.persistence.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for managing faction lifecycle and operations.
 */
public class FactionService {

    private static final Logger LOGGER = Logger.getLogger(FactionService.class.getName());

    private final DatabaseManager db;
    private final FactionMapper factionMapper;
    private final FactionMemberMapper memberMapper;
    private final FactionInviteMapper inviteMapper;
    private final BannedPlayerMapper bannedMapper;
    private final PowerService powerService;
    private final Map<UUID, Faction> factionCache; // factionId -> Faction

    public FactionService(DatabaseManager db, PowerService powerService) {
        this.db = db;
        this.factionMapper = new FactionMapper(db);
        this.memberMapper = new FactionMemberMapper(db);
        this.inviteMapper = new FactionInviteMapper(db);
        this.bannedMapper = new BannedPlayerMapper(db);
        this.powerService = powerService;
        this.factionCache = new ConcurrentHashMap<>();
        loadAllFactions();
    }

    /**
     * Loads all factions from database into cache.
     */
    private void loadAllFactions() {
        try {
            List<Faction> factions = factionMapper.findAll();
            for (Faction faction : factions) {
                factionCache.put(faction.getId(), faction);
            }
            LOGGER.info("Loaded " + factions.size() + " factions from database");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load factions from database", e);
        }
    }

    /**
     * Creates a new faction.
     */
    public Faction createFaction(String name, String tag, UUID leaderId) throws IllegalStateException {
        if (tag.length() > 4) {
            throw new IllegalArgumentException("Faction tag must be 4 characters or less");
        }

        // Check if tag is already taken
        try {
            Faction existing = factionMapper.findByTag(tag);
            if (existing != null) {
                throw new IllegalStateException("Faction tag already in use");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to check tag availability", e);
            throw new RuntimeException("Database error checking tag", e);
        }

        UUID factionId = UUID.randomUUID();
        FactionImpl faction = new FactionImpl(factionId, name, tag);
        faction.setLeaderId(leaderId);
        faction.setCreatedAt(System.currentTimeMillis());
        faction.setLastSeen(System.currentTimeMillis());
        faction.addMember(leaderId);
        faction.setPower(100.0); // Starting power for first member

        try {
            factionMapper.insert(faction);
            memberMapper.insert(factionId, leaderId, "LEADER",
                    LocalDateTime.now().toString(),
                    LocalDateTime.now().toString(),
                    null, 0.0, false);
            factionCache.put(factionId, faction);
            LOGGER.info("Created faction: " + tag + " (ID: " + factionId + ")");
            return faction;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create faction in database", e);
            throw new RuntimeException("Failed to create faction", e);
        }
    }

    /**
     * Deletes a faction.
     */
    public boolean deleteFaction(UUID factionId) {
        Faction faction = getFaction(factionId);
        if (faction == null) {
            return false;
        }

        try {
            // Delete all related data
            // Mappers with foreign key cascades will handle cleanup
            factionMapper.delete(factionId);
            factionCache.remove(factionId);
            LOGGER.info("Deleted faction: " + faction.getTag());
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete faction from database", e);
            return false;
        }
    }

    /**
     * Gets a faction by ID (from cache or database).
     */
    public Faction getFaction(UUID factionId) {
        Faction faction = factionCache.get(factionId);
        if (faction == null) {
            try {
                faction = factionMapper.findById(factionId);
                if (faction != null) {
                    factionCache.put(factionId, faction);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to load faction " + factionId, e);
            }
        }
        return faction;
    }

    /**
     * Gets a faction by tag.
     */
    public Faction getFactionByTag(String tag) {
        try {
            return factionMapper.findByTag(tag);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to load faction by tag " + tag, e);
            return null;
        }
    }

    /**
     * Gets the role of a player in a faction.
     */
    public FactionMember.Role getMemberRole(Faction faction, UUID playerId) {
        try {
            String roleStr = memberMapper.getRole(faction.getId(), playerId);
            if (roleStr != null) {
                return FactionMember.Role.valueOf(roleStr);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to get member role", e);
        }
        return null;
    }

    /**
     * Sets the role of a player in a faction.
     */
    public boolean setMemberRole(Faction faction, UUID playerId, FactionMember.Role newRole) {
        if (!faction.hasMember(playerId)) {
            return false;
        }
        try {
            memberMapper.updateRole(faction.getId(), playerId, newRole.name());
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to set member role", e);
            return false;
        }
    }

    /**
     * Gets all factions.
     */
    public Collection<Faction> getAllFactions() {
        return new ArrayList<>(factionCache.values());
    }

    /**
     * Finds a faction that has invited a specific player.
     */
    public Faction findFactionWithInvite(UUID playerId) {
        for (Faction faction : getAllFactions()) {
            if (faction.getInvites().contains(playerId)) {
                return faction;
            }
        }
        return null;
    }

    /**
     * Gets the role of a member as an enum.
     */
    public FactionMember.Role getMemberRole(Faction faction, UUID playerId) {
        try {
            String roleStr = memberMapper.getRole(faction.getId(), playerId);
            if (roleStr != null) {
                return FactionMember.Role.valueOf(roleStr);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to get member role", e);
        }
        return null;
    }

    /**
     * Sets the role of a member.
     */
    public boolean setMemberRole(Faction faction, UUID playerId, FactionMember.Role newRole) {
        if (!faction.hasMember(playerId)) {
            return false;
        }
        try {
            memberMapper.updateRole(faction.getId(), playerId, newRole.name());
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to set member role", e);
            return false;
        }
    }

    /**
     * Adds a member to a faction.
     */
    public boolean addMember(Faction faction, UUID playerId) {
        if (faction.hasMember(playerId)) {
            return false;
        }

        try {
            memberMapper.insert(faction.getId(), playerId, "RECRUIT",
                    LocalDateTime.now().toString(),
                    LocalDateTime.now().toString(),
                    null, 0.0, false);
            faction.addMember(playerId);
            faction.setLastSeen(System.currentTimeMillis());
            factionMapper.update(faction);
            powerService.recalculateFactionPower(faction);
            LOGGER.info("Added member " + playerId + " to faction " + faction.getTag());
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add member to faction", e);
            return false;
        }
    }

    /**
     * Removes a member from a faction.
     */
    public boolean removeMember(Faction faction, UUID playerId) {
        try {
            memberMapper.delete(faction.getId(), playerId);
            faction.removeMember(playerId);
            faction.setLastSeen(System.currentTimeMillis());
            factionMapper.update(faction);
            powerService.recalculateFactionPower(faction);
            LOGGER.info("Removed member " + playerId + " from faction " + faction.getTag());
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to remove member from faction", e);
            return false;
        }
    }

    /**
     * Invites a player to a faction.
     */
    public boolean invitePlayer(Faction faction, UUID playerId) {
        try {
            long now = System.currentTimeMillis();
            inviteMapper.insert(faction.getId(), playerId, now);
            faction.addInvite(playerId);
            LOGGER.info("Faction " + faction.getTag() + " invited player " + playerId);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to record invite", e);
            return false;
        }
    }

    /**
     * Accepts an invitation.
     */
    public boolean acceptInvite(Faction faction, UUID playerId) {
        try {
            inviteMapper.delete(faction.getId(), playerId);
            faction.removeInvite(playerId);
            return addMember(faction, playerId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to accept invite", e);
            return false;
        }
    }

    /**
     * Denies an invitation.
     */
    public boolean denyInvite(Faction faction, UUID playerId) {
        try {
            inviteMapper.delete(faction.getId(), playerId);
            faction.removeInvite(playerId);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to deny invite", e);
            return false;
        }
    }

    /**
     * Bans a player from the faction.
     */
    public boolean banPlayer(Faction faction, UUID playerId) {
        try {
            long now = System.currentTimeMillis();
            bannedMapper.insert(faction.getId(), playerId, now);
            faction.ban(playerId);
            faction.setLastSeen(System.currentTimeMillis());
            factionMapper.update(faction);
            LOGGER.info("Faction " + faction.getTag() + " banned player " + playerId);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to ban player", e);
            return false;
        }
    }

    /**
     * Unbans a player from the faction.
     */
    public boolean unbanPlayer(Faction faction, UUID playerId) {
        try {
            bannedMapper.delete(faction.getId(), playerId);
            faction.unban(playerId);
            faction.setLastSeen(System.currentTimeMillis());
            factionMapper.update(faction);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to unban player", e);
            return false;
        }
    }

    /**
     * Updates faction claim count after changes.
     */
    public void updateClaimCount(Faction faction) {
        try {
            factionMapper.update(faction);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to update claim count for " + faction.getTag(), e);
        }
    }

    /**
     * Updates faction in cache and database.
     */
    public void saveFaction(Faction faction) {
        try {
            factionMapper.update(faction);
            factionCache.put(faction.getId(), faction);
            powerService.recalculateFactionPower(faction);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save faction", e);
        }
    }

    /**
     * Clears all caches.
     */
    public void clearCache() {
        factionCache.clear();
        // Note: relation cache is in RelationService, will need to clear that separately
    }
}
