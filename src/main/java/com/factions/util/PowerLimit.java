package com.factions.util;

import com.factions.api.Faction;

/**
 * Utility for power-based claim limit calculations.
 * Determines whether a faction can perform claim-related actions based on power.
 */
public class PowerLimit {

    private final double minClaimsPerPower; // ratio: power needed per claim (e.g., 0.01 = 100 power per claim)

    public PowerLimit(double minClaimsPerPower) {
        this.minClaimsPerPower = Math.max(0.001, minClaimsPerPower); // avoid division by zero
    }

    /**
     * Calculates the maximum number of claims a faction can have based on its total power.
     */
    public int calculateMaxClaims(double factionPower) {
        return (int) Math.floor(factionPower * minClaimsPerPower);
    }

    /**
     * Checks if the faction can claim additional land.
     */
    public boolean canClaim(double factionPower, int currentClaims, int additionalClaims) {
        int maxClaims = calculateMaxClaims(factionPower);
        return (currentClaims + additionalClaims) <= maxClaims;
    }

    /**
     * Checks if an overclaim is valid based on power ratio.
     * Attacker must have at least the required multiplier times defender's power.
     *
     * @param attackerPower power of attacking faction
     * @param defenderPower power of defending faction
     * @param requiredRatio minimum ratio (e.g., 1.2 for 20% more)
     * @return true if attacker meets power requirement
     */
    public boolean canOverclaim(double attackerPower, double defenderPower, double requiredRatio) {
        return attackerPower >= defenderPower * requiredRatio;
    }

    /**
     * Gets the required attacker power for a given defender power and ratio.
     */
    public double getRequiredAttackerPower(double defenderPower, double requiredRatio) {
        return defenderPower * requiredRatio;
    }

    /**
     * Gets the amount of power a faction needs to reach a desired number of claims.
     */
    public double getRequiredPowerForClaims(int desiredClaims) {
        return desiredClaims / minClaimsPerPower;
    }
}
