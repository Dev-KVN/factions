package com.factions.config;

/**
 * Configuration settings for the power system.
 * Reads values from the plugin config.yml.
 */
public class PowerConfiguration {

    private final double maxPower;
    private final double gainRate;
    private final double deathPenaltyPercent;
    private final OfflineMode offlineMode;
    private final double offlineDecayRate; // per hour while offline
    private final double minutesPerPoint;
    private final double incrementPerDay;
    private final double minClaimsPerPower;

    public enum OfflineMode {
        FREEZE,   // Power stays the same when offline
        DECAY,    // Power decays over time when offline
        KEEP      // Power keeps regenerating (unlimited) - not recommended
    }

    /**
     * Constructs configuration from config values.
     */
    public PowerConfiguration(double maxPower, double gainRate, double deathPenaltyPercent,
                              OfflineMode offlineMode, double offlineDecayRate,
                              double minutesPerPoint, double incrementPerDay,
                              double minClaimsPerPower) {
        this.maxPower = maxPower;
        this.gainRate = gainRate;
        this.deathPenaltyPercent = deathPenaltyPercent;
        this.offlineMode = offlineMode;
        this.offlineDecayRate = offlineDecayRate;
        this.minutesPerPoint = minutesPerPoint;
        this.incrementPerDay = incrementPerDay;
        this.minClaimsPerPower = minClaimsPerPower;
    }

    public double getMaxPower() {
        return maxPower;
    }

    public double getGainRate() {
        return gainRate;
    }

    public double getDeathPenaltyPercent() {
        return deathPenaltyPercent;
    }

    public OfflineMode getOfflineMode() {
        return offlineMode;
    }

    public double getOfflineDecayRate() {
        return offlineDecayRate;
    }

    public double getMinutesPerPoint() {
        return minutesPerPoint;
    }

    public double getIncrementPerDay() {
        return incrementPerDay;
    }

    public double getMinClaimsPerPower() {
        return minClaimsPerPower;
    }

    /**
     * Calculates the death penalty absolute amount based on max power.
     */
    public double getDeathPenaltyAbsolute(double currentMaxPower) {
        return currentMaxPower * (deathPenaltyPercent / 100.0);
    }
}
