package com.factions.service;

import com.factions.api.*;
import com.factions.persistence.*;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for managing land claims.
 * Handles claiming, unclaiming, overclaiming, and claim limits.
 */
public class ClaimService {

    private static final Logger LOGGER = Logger.getLogger(ClaimService.class.getName());

    private final DatabaseManager db;
    private final ClaimMapper claimMapper;
    private final PowerService powerService;
    private final FactionService factionService;

    // Chunk cache for fast lookup
    private final Map<String, UUID> chunkToFaction; // "world:x:z" -> factionId
    private final Map<UUID, Set<String>> factionClaims; // factionId -> set of claim IDs

    public ClaimService(DatabaseManager db, PowerService powerService, FactionService factionService) {
        this.db = db;
        this.claimMapper = new ClaimMapper(db);
        this.powerService = powerService;
        this.factionService = factionService;
        this.chunkToFaction = new ConcurrentHashMap<>();
        this.factionClaims = new ConcurrentHashMap<>();
        loadAllClaims();
    }

    /**
     * Loads all claims into cache at startup.
     */
    private void loadAllClaims() {
        try {
            Collection<Faction> allFactions = factionService.getAllFactions();
            int totalClaims = 0;
            for (Faction faction : allFactions) {
                List<Claim> claims = claimMapper.findByFaction(faction.getId());
                for (Claim claim : claims) {
                    String claimId = claim.getWorld() + ":" + claim.getChunkX() + ":" + claim.getChunkZ();
                    chunkToFaction.put(claimId, claim.getFactionId());
                    factionClaims.computeIfAbsent(faction.getId(), k -> ConcurrentHashMap.newKeySet()).add(claimId);
                    totalClaims++;
                }
            }
            LOGGER.info("Loaded " + totalClaims + " claims for " + allFactions.size() + " factions into cache");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load claims", e);
        }
    }

    /**
     * Claims a chunk for a faction.
     * Returns true if claim successful, false otherwise.
     */
    public boolean claimChunk(Faction faction, String world, int chunkX, int chunkZ, String claimingPlayer) {
        String claimId = world + ":" + chunkX + ":" + chunkZ;

        // Check if already claimed
        if (chunkToFaction.containsKey(claimId)) {
            LOGGER.fine("Chunk already claimed: " + claimId);
            return false;
        }

        // Check faction power limit
        if (faction.getClaimCount() >= faction.getMaxClaims()) {
            LOGGER.fine("Faction has reached claim limit: " + faction.getClaimCount() + "/" + faction.getMaxClaims());
            return false;
        }

        // Check connected claims (if enabled) - first claim always allowed
        if (!faction.getClaims().isEmpty()) {
            Claim newClaim = new Claim(faction.getId(), world, chunkX, chunkZ);
            if (!isConnectedToExistingClaim(newClaim, faction)) {
                LOGGER.fine("New claim not connected to existing territory");
                return false;
            }
        }

        // Create and store claim
        Claim claim = new Claim(faction.getId(), world, chunkX, chunkZ);
        claim.setClaimedAt(System.currentTimeMillis());
        claim.setClaimedBy(claimingPlayer);

        try {
            claimMapper.insert(claim);
            faction.addClaim(claim);
            chunkToFaction.put(claimId, faction.getId());
            factionClaims.computeIfAbsent(faction.getId(), k -> ConcurrentHashMap.newKeySet()).add(claimId);

            // Update faction's claim count in DB via FactionMapper
            factionService.updateClaimCount(faction);

            LOGGER.info("Faction " + faction.getTag() + " claimed chunk " + claimId);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save claim to database", e);
            return false;
        }
    }

    /**
     * Unclaims a chunk from a faction.
     */
    public boolean unclaimChunk(Faction faction, String world, int chunkX, int chunkZ) {
        String claimId = world + ":" + chunkX + ":" + chunkZ;

        Claim claim = null;
        for (Claim c : faction.getClaims()) {
            if (c.getWorld().equals(world) && c.getChunkX() == chunkX && c.getChunkZ() == chunkZ) {
                claim = c;
                break;
            }
        }

        if (claim == null) {
            return false;
        }

        try {
            claimMapper.delete(world, chunkX, chunkZ);
            faction.removeClaim(claim);
            chunkToFaction.remove(claimId);
            factionClaims.getOrDefault(faction.getId(), Collections.emptySet()).remove(claimId);

            // Update count
            factionService.updateClaimCount(faction);

            LOGGER.info("Faction " + faction.getTag() + " unclaimed chunk " + claimId);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete claim from database", e);
            return false;
        }
    }

    /**
     * Unclaims all chunks for a faction (e.g., on disband).
     */
    public void unclaimAllForFaction(Faction faction) {
        try {
            List<Claim> claims = claimMapper.findByFaction(faction.getId());
            for (Claim claim : claims) {
                claimMapper.delete(claim.getWorld(), claim.getChunkX(), claim.getChunkZ());
                String claimId = claim.getWorld() + ":" + claim.getChunkX() + ":" + claim.getChunkZ();
                chunkToFaction.remove(claimId);
            }
            faction.getClaims().clear();
            faction.setClaimCount(0);
            factionClaims.remove(faction.getId());
            LOGGER.info("Unclaimed all " + claims.size() + " chunks for faction " + faction.getTag());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to unclaim all for faction", e);
        }
    }

    /**
     * Checks if a chunk is claimed.
     */
    public Optional<UUID> getClaimingFaction(String world, int chunkX, int chunkZ) {
        String claimId = world + ":" + chunkX + ":" + chunkZ;
        UUID factionId = chunkToFaction.get(claimId);
        return Optional.ofNullable(factionId);
    }

    /**
     * Checks if a chunk is claimed by a specific faction.
     */
    public boolean isChunkClaimedBy(Faction faction, String world, int chunkX, int chunkZ) {
        String claimId = world + ":" + chunkX + ":" + chunkZ;
        return chunkToFaction.get(claimId) != null && chunkToFaction.get(claimId).equals(faction.getId());
    }

    /**
     * Checks if a faction owns a specific chunk.
     */
    public boolean ownsChunk(Faction faction, String world, int chunkX, int chunkZ) {
        return isChunkClaimedBy(faction, world, chunkX, chunkZ);
    }

    /**
     * Gets all chunks owned by a faction.
     */
    public Set<String> getClaimIds(Faction faction) {
        return factionClaims.getOrDefault(faction.getId(), Collections.emptySet());
    }

    /**
     * Checks if a new claim is adjacent to any existing claim.
     * Enforces connected-claims rule.
     */
    private boolean isConnectedToExistingClaim(Claim newClaim, Faction faction) {
        for (Claim existing : faction.getClaims()) {
            if (newClaim.isAdjacentTo(existing)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if an enemy faction can overclaim this chunk (raid mechanic).
     * Overclaiming requires the attacking faction to have more power.
     * Only works on enemy territory.
     */
    public boolean canOverclaim(Faction attacker, Faction defender, String world, int chunkX, int chunkZ) {
        // Check relation
        if (attacker.getRelation(defender) != RelationState.ENEMY) {
            LOGGER.fine("Can only overclaim enemy factions");
            return false;
        }

        // Check if chunk is actually claimed by defender
        if (!isChunkClaimedBy(defender, world, chunkX, chunkZ)) {
            LOGGER.fine("Chunk not claimed by enemy faction");
            return false;
        }

        // Power check: attacker must have significantly more power
        double attackerPower = powerService.recalculateFactionPower(attacker);
        double defenderPower = powerService.recalculateFactionPower(defender);

        // Simple rule: attacker power must be at least 20% greater than defender's
        return attackerPower >= defenderPower * 1.2;
    }

    /**
     * Overclaims a chunk from an enemy faction.
     */
    public boolean overclaimChunk(Faction attacker, Faction defender, String world,
                                  int chunkX, int chunkZ, String attackingPlayer) {
        if (!canOverclaim(attacker, defender, world, chunkX, chunkZ)) {
            return false;
        }

        // Unclaim from defender
        unclaimChunk(defender, world, chunkX, chunkZ);

        // Claim for attacker
        return claimChunk(attacker, world, chunkX, chunkZ, attackingPlayer);
    }

    /**
     * Gets the faction that owns a chunk, or empty if unclaimed.
     */
    public Optional<Faction> getOwningFaction(String world, int chunkX, int chunkZ) {
        return getClaimingFaction(world, chunkX, chunkZ)
                .flatMap(factionId -> Optional.ofNullable(factionService.getFaction(factionId)));
    }

    /**
     * Counts the number of claimed chunks in a world for a faction.
     */
    public int getClaimCount(Faction faction, String world) {
        int count = 0;
        for (Claim claim : faction.getClaims()) {
            if (claim.getWorld().equals(world)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Clears the cache (used for reload/refresh).
     */
    public void clearCache() {
        chunkToFaction.clear();
        factionClaims.clear();
        loadAllClaims();
    }
}
