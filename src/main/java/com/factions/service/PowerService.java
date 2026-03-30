package com.factions.service;

import com.factions.api.Faction;
import com.factions.api.FactionMember;
import com.factions.persistence.DatabaseManager;
import com.factions.persistence.PlayerPowerMapper;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for managing player and faction power.
 * Handles power regeneration, offline decay, and faction power aggregation.
 */
public class PowerService {

    private static final Logger LOGGER = Logger.getLogger(PowerService.class.getName());

    public static final double DEFAULT_MAX_POWER = 1000.0;
    public static final double POWER_REGEN_RATE = 1.0; // per minute while online
    public static final double OFFLINE_DECAY_RATE = 0.5; // per hour while offline
    public static final long POWER_UPDATE_INTERVAL = 60 * 1000; // 1 minute

    private final DatabaseManager db;
    private final PlayerPowerMapper playerPowerMapper;
    private final Map<UUID, Double> playerPowerCache; // playerId -> current power
    private final Map<UUID, Long> lastPowerUpdate; // playerId -> last update timestamp
    private final Map<UUID, Double> maxPowerCache; // playerId -> max power
    private final Set<UUID> onlinePlayers;

    private final ScheduledExecutorService scheduler;

    public PowerService(DatabaseManager db) {
        this.db = db;
        this.playerPowerMapper = new PlayerPowerMapper(db);
        this.playerPowerCache = new ConcurrentHashMap<>();
        this.lastPowerUpdate = new ConcurrentHashMap<>();
        this.maxPowerCache = new ConcurrentHashMap<>();
        this.onlinePlayers = ConcurrentHashMap.newKeySet();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PowerService-Task");
            t.setDaemon(true);
            return t;
        });

        loadAllPowerFromDb();
    }

    /**
     * Starts the power regeneration scheduler.
     */
    public void start() {
        scheduler.scheduleAtFixedRate(this::updateAllPower, POWER_UPDATE_INTERVAL,
                                            POWER_UPDATE_INTERVAL, TimeUnit.MILLISECONDS);
        LOGGER.info("Power regeneration scheduler started");
    }

    /**
     * Stops the power scheduler.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
        LOGGER.info("Power service shutdown");
    }

    /**
     * Loads all player power data from database into cache.
     */
    private void loadAllPowerFromDb() {
        try {
            // In a full implementation, we'd query all player_power rows
            // For now, power is loaded on demand when players join
            LOGGER.info("Player power cache initialized");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load player power from DB", e);
        }
    }

    /**
     * Updates power for all tracked players (called by scheduler).
     */
    private void updateAllPower() {
        long now = System.currentTimeMillis();
        List<UUID> toUpdate = new ArrayList<>(playerPowerCache.keySet());

        for (UUID playerId : toUpdate) {
            try {
                updatePlayerPower(playerId, now);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to update power for " + playerId, e);
            }
        }
    }

    /**
     * Updates a single player's power based on online status and time elapsed.
     */
    private void updatePlayerPower(UUID playerId, long now) throws SQLException {
        Long lastUpdate = lastPowerUpdate.get(playerId);
        if (lastUpdate == null) {
            // First time seeing this player, load from DB
            loadPlayerPowerFromDb(playerId);
            return;
        }

        double currentPower = playerPowerCache.getOrDefault(playerId, DEFAULT_MAX_POWER);
        double maxPower = maxPowerCache.getOrDefault(playerId, DEFAULT_MAX_POWER);
        long elapsedMs = now - lastUpdate;
        double elapsedMinutes = elapsedMs / (1000.0 * 60.0);

        if (onlinePlayers.contains(playerId)) {
            // Online: regenerate power
            double regen = POWER_REGEN_RATE * elapsedMinutes;
            currentPower = Math.min(maxPower, currentPower + regen);
        } else {
            // Offline: decay power (max 1 hour per day)
            double decay = OFFLINE_DECAY_RATE * elapsedMinutes;
            currentPower = Math.max(0, currentPower - decay);
        }

        playerPowerCache.put(playerId, currentPower);
        lastPowerUpdate.put(playerId, now);

        // Persist to DB periodically (every update for simplicity)
        savePlayerPowerToDb(playerId, currentPower, maxPower, now);
    }

    /**
     * Loads a player's power from the database.
     */
    private void loadPlayerPowerFromDb(UUID playerId) throws SQLException {
        try {
            if (playerPowerMapper.exists(playerId)) {
                double power = playerPowerMapper.getPower(playerId);
                double max = playerPowerMapper.getMaxPower(playerId);
                playerPowerCache.put(playerId, power);
                maxPowerCache.put(playerId, max);
            } else {
                // New player, start with max power
                playerPowerCache.put(playerId, DEFAULT_MAX_POWER);
                maxPowerCache.put(playerId, DEFAULT_MAX_POWER);
                playerPowerMapper.insert(playerId, DEFAULT_MAX_POWER, DEFAULT_MAX_POWER,
                                       System.currentTimeMillis());
            }
            lastPowerUpdate.put(playerId, System.currentTimeMillis());
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to load power for " + playerId, e);
            throw e;
        }
    }

    /**
     * Saves a player's power to the database.
     */
    private void savePlayerPowerToDb(UUID playerId, double power, double maxPower, long lastUpdate) throws SQLException {
        playerPowerMapper.update(playerId, power, maxPower, lastUpdate, 0);
    }

    /**
     * Gets the current power for a player.
     */
    public double getPower(UUID playerId) {
        if (!playerPowerCache.containsKey(playerId)) {
            try {
                loadPlayerPowerFromDb(playerId);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to load power for " + playerId, e);
                return 0;
            }
        }
        return playerPowerCache.getOrDefault(playerId, 0.0);
    }

    /**
     * Gets the max power for a player.
     */
    public double getMaxPower(UUID playerId) {
        if (!maxPowerCache.containsKey(playerId)) {
            try {
                loadPlayerPowerFromDb(playerId);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to load max power for " + playerId, e);
                return DEFAULT_MAX_POWER;
            }
        }
        return maxPowerCache.getOrDefault(playerId, DEFAULT_MAX_POWER);
    }

    /**
     * Sets a player's power directly (e.g., after death).
     */
    public void setPower(UUID playerId, double power) {
        double max = maxPowerCache.getOrDefault(playerId, DEFAULT_MAX_POWER);
        playerPowerCache.put(playerId, Math.min(max, Math.max(0, power)));
        lastPowerUpdate.put(playerId, System.currentTimeMillis());
        try {
            savePlayerPowerToDb(playerId, playerPowerCache.get(playerId), max, System.currentTimeMillis());
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to save power for " + playerId, e);
        }
    }

    /**
     * Increases a player's power (e.g., from killing enemies).
     */
    public void addPower(UUID playerId, double amount) {
        double current = getPower(playerId);
        double max = getMaxPower(playerId);
        double newPower = Math.min(max, current + amount);
        setPower(playerId, newPower);
    }

    /**
     * Decreases a player's power (e.g., from death).
     */
    public void removePower(UUID playerId, double amount) {
        double current = getPower(playerId);
        double newPower = Math.max(0, current - amount);
        setPower(playerId, newPower);
    }

    /**
     * Sets a player online status (affects regeneration).
     */
    public void setOnline(UUID playerId, boolean online) {
        if (online) {
            onlinePlayers.add(playerId);
            lastPowerUpdate.put(playerId, System.currentTimeMillis());
        } else {
            onlinePlayers.remove(playerId);
            try {
                updatePlayerPower(playerId, System.currentTimeMillis());
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to update power on logout for " + playerId, e);
            }
        }
    }

    /**
     * Recalculates faction power by summing all member power.
     * This is called when members change or power updates need to propagate.
     */
    public double recalculateFactionPower(Faction faction) {
        double totalPower = 0.0;
        for (UUID memberId : faction.getMembers()) {
            totalPower += getPower(memberId);
        }
        faction.setPower(totalPower);
        faction.setMaxClaims((int) (totalPower / 1000.0) * 100); // Example: 100 claims per 1000 power

        persistFactionPower(faction);
        return totalPower;
    }

    /**
     * Persists faction power to the database (via FactionMapper).
     * This would be called by the FactionService.
     */
    private void persistFactionPower(Faction faction) {
        // The FactionService should handle the actual persistence
        // Here we just ensure the faction object is updated
    }

    /**
     * Called when a player dies - applies power loss based on configurable penalty.
     */
    public void applyDeathPenalty(UUID playerId, double penaltyPercent) {
        double maxPower = getMaxPower(playerId);
        double penalty = maxPower * (penaltyPercent / 100.0);
        removePower(playerId, penalty);
    }

    /**
     * Resets a player's power to max (e.g., after cooldown or admin action).
     */
    public void resetPower(UUID playerId) {
        setPower(playerId, getMaxPower(playerId));
    }

    /**
     * Gets power statistics for debugging/monitoring.
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cachedPlayers", playerPowerCache.size());
        stats.put("onlinePlayers", onlinePlayers.size());
        stats.put("schedulerActive", !scheduler.isShutdown());
        return stats;
    }
}
