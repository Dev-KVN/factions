package com.factions.api.extension;

import com.factions.api.Claim;
import com.factions.api.Faction;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Listener for claim-related events.
 *
 * <p>Implement this interface to receive notifications when claims are claimed,
 * unclaimed, or modified. Register via {@link com.factions.api.Factions#registerListener(ClaimListener)}.</p>
 *
 * @author Factions Team
 * @since 1.0.0
 */
public interface ClaimListener {

    /**
     * Called when a chunk claim is successfully created.
     *
     * @param faction faction that claimed it
     * @param claim the new claim
     * @param claimedBy who claimed it (null for async/auto)
     */
    void onClaimCreated(Faction faction, Claim claim, Player claimedBy);

    /**
     * Called when a chunk claim is unclaimed.
     *
     * @param faction faction that lost the claim
     * @param claim the removed claim
     * @param unclaimedBy who removed it (null for async/auto)
     */
    void onClaimRemoved(Faction faction, Claim claim, Player unclaimedBy);

    /**
     * Called when an overclaim occurs (raiding).
     *
     * @param attacker faction that overclaimed
     * @param defender faction that lost the claim
     * @param claim the contested claim
     * @param overclaimedBy who performed the overclaim
     */
    void onOverclaim(Faction attacker, Faction defender, Claim claim, Player overclaimedBy);

    /**
     * Called when territory type changes (e.g., setting safezone).
     *
     * @param claim the claim
     * @param oldType previous type
     * @param newType new type
     * @param changedBy who changed it (null for system)
     */
    void onTerritoryTypeChange(Claim claim, Claim.TerritoryType oldType, Claim.TerritoryType newType, Player changedBy);

    /**
     * Called when chunk border visualization is requested.
     *
     * @param player viewer
     * @param claim claim being viewed
     * @param locations block locations to highlight
     */
    default void onBorderVisualize(Player player, Claim claim, List<Location> locations) {
        // default no-op
    }

    /**
     * Called when a player attempts to build in a claim but is denied.
     *
     * @param player player who attempted build
     * @param claim claim where attempt occurred
     * @param location exact block location
     */
    default void onBuildDenied(Player player, Claim claim, Location location) {
        // default no-op
    }

    /**
     * Called when a claim limit is reached ( faction cannot claim more ).
     *
     * @param faction faction that hit limit
     */
    default void onClaimLimitReached(Faction faction) {
        // default no-op
    }
}
