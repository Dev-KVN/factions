package com.factions.model;

import java.util.UUID;

/**
 * Player power data record.
 * Holds a player's current power state including tracking for deaths.
 */
public record PlayerPower(
    UUID uuid,
    double currentPower,
    double maxPower,
    long lastUpdate,
    int deathCount
) {
    /**
     * Creates a new PlayerPower with default values for a new player.
     */
    public static PlayerPower createNew(UUID uuid, double defaultMaxPower) {
        long now = System.currentTimeMillis();
        return new PlayerPower(uuid, defaultMaxPower, defaultMaxPower, now, 0);
    }

    /**
     * Checks if the player is at full power.
     */
    public boolean isAtMax() {
        return currentPower >= maxPower;
    }

    /**
     * Applies a power loss, ensuring it doesn't go below zero.
     */
    public PlayerPower withPowerLoss(double amount) {
        double newPower = Math.max(0, currentPower - amount);
        return new PlayerPower(uuid, newPower, maxPower, System.currentTimeMillis(), deathCount);
    }

    /**
     * Adds power, respecting the maximum cap.
     */
    public PlayerPower withPowerGain(double amount) {
        double newPower = Math.min(maxPower, currentPower + amount);
        return new PlayerPower(uuid, newPower, maxPower, System.currentTimeMillis(), deathCount);
    }

    /**
     * Increments death count and resets power to max (if desired) or just tracks death.
     */
    public PlayerPower withDeath() {
        return new PlayerPower(uuid, currentPower, maxPower, System.currentTimeMillis(), deathCount + 1);
    }
}
