package com.factions.service;

import com.factions.api.*;
import com.factions.persistence.DatabaseManager;
import com.factions.persistence.RelationMapper;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for managing diplomatic relations between factions.
 */
public class RelationService {

    private static final Logger LOGGER = Logger.getLogger(RelationService.class.getName());

    private final DatabaseManager db;
    private final RelationMapper relationMapper;
    private final Map<UUID, Map<UUID, RelationState>> relationCache; // factionId -> (otherId -> state)

    public RelationService(DatabaseManager db) {
        this.db = db;
        this.relationMapper = new RelationMapper(db);
        this.relationCache = new ConcurrentHashMap<>();
    }

    /**
     * Sets a relation between two factions.
     * @param factionA The initiating faction
     * @param factionB The target faction
     * @param state The relation state to set
     * @param establishedBy Player UUID who initiated this
     */
    public boolean setRelation(Faction factionA, Faction factionB, RelationState state, UUID establishedBy) {
        if (factionA == null || factionB == null) {
            return false;
        }

        UUID idA = factionA.getId();
        UUID idB = factionB.getId();

        // Cannot set relation with self
        if (idA.equals(idB)) {
            return false;
        }

        // Check if faction has permission (based on member role)
        // For now, assume caller has already checked permissions

        try {
            if (state == RelationState.NEUTRAL) {
                relationMapper.delete(idA, idB);
                relationMapper.delete(idB, idA); // Remove reciprocal
                removeFromCache(idA, idB);
                removeFromCache(idB, idA);
            } else {
                String now = LocalDateTime.now().toString();
                relationMapper.insert(idA, idB, state, now, establishedBy != null ? establishedBy.toString() : null);

                // Set reciprocal relation automatically
                RelationState reciprocal = getReciprocalState(state);
                relationMapper.insert(idB, idA, reciprocal, now, establishedBy != null ? establishedBy.toString() : null);
                addToCache(idA, idB, state);
                addToCache(idB, idA, reciprocal);
            }

            LOGGER.info(String.format("Relation set: %s -> %s = %s", factionA.getTag(), factionB.getTag(), state));
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save relation to database", e);
            return false;
        }
    }

    /**
     * Gets the relation state between two factions.
     */
    public RelationState getRelation(Faction factionA, Faction factionB) {
        if (factionA == null || factionB == null) {
            return RelationState.NEUTRAL;
        }

        UUID idA = factionA.getId();
        UUID idB = factionB.getId();

        if (idA.equals(idB)) {
            return RelationState.ALLY; // Self is always ally
        }

        return getFromCache(idA, idB);
    }

    /**
     * Retrieves relation from cache or database.
     */
    private RelationState getFromCache(UUID factionA, UUID factionB) {
        Map<UUID, RelationState> cacheA = relationCache.get(factionA);
        if (cacheA != null) {
            RelationState state = cacheA.get(factionB);
            if (state != null) {
                return state;
            }
        }

        // Load from DB
        try {
            RelationState state = relationMapper.findRelation(factionA, factionB);
            addToCache(factionA, factionB, state);
            return state;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to load relation from database", e);
            return RelationState.NEUTRAL;
        }
    }

    private void addToCache(UUID factionA, UUID factionB, RelationState state) {
        relationCache.computeIfAbsent(factionA, k -> new ConcurrentHashMap<>()).put(factionB, state);
    }

    private void removeFromCache(UUID factionA, UUID factionB) {
        Map<UUID, RelationState> map = relationCache.get(factionA);
        if (map != null) {
            map.remove(factionB);
        }
    }

    /**
     * Gets all relations for a faction.
     */
    public Map<UUID, RelationState> getAllRelations(Faction faction) {
        // This would ideally query the database for all relations involving this faction
        // For now, return what's in cache
        return Collections.unmodifiableMap(relationCache.getOrDefault(faction.getId(), Collections.emptyMap()));
    }

    /**
     * Gets the reciprocal state for a given relation.
     * ALLY -> ALLY, ENEMY -> ENEMY, TRUCE -> TRUCE, NEUTRAL -> NEUTRAL
     */
    private RelationState getReciprocalState(RelationState state) {
        // In this implementation, relations are symmetric (except maybe for special cases)
        return state;
    }

    /**
     * Checks if two factions are allied.
     */
    public boolean areAllied(Faction factionA, Faction factionB) {
        return getRelation(factionA, factionB) == RelationState.ALLY;
    }

    /**
     * Checks if two factions are enemies.
     */
    public boolean areEnemies(Faction factionA, Faction factionB) {
        return getRelation(factionA, factionB) == RelationState.ENEMY;
    }

    /**
     * Checks if two factions have a truce.
     */
    public boolean haveTruce(Faction factionA, Faction factionB) {
        return getRelation(factionA, factionB) == RelationState.TRUCE;
    }

    /**
     * Proposes a relation request (for future use with acceptance system).
     * Currently relations are set immediately.
     */
    public void proposeRelation(Faction factionA, Faction factionB, RelationState proposedState) {
        // In future, this could create a pending relation that requires acceptance
        // For now, just set it directly
        setRelation(factionA, factionB, proposedState, null);
    }

    /**
     * Clears the relation cache (useful for reloads).
     */
    public void clearCache() {
        relationCache.clear();
    }
}
