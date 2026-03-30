package com.factions.api;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Factions API - Main entry point.
 *
 * <p>This class provides static methods to interact with the Factions plugin.
 * It acts as a facade to the internal core implementation, which is loaded
 * when the plugin is enabled. All API calls delegate to the core service.</p>
 *
 * <h2>Basic Usage</h2>
 * <pre>{@code
 * // Get a player's faction
 * Faction faction = Factions.getFaction(player.getUniqueId());
 *
 * // Get faction by name
 * Faction faction = Factions.getFactionByName("MyFaction");
 *
 * // Check membership
 * boolean member = Factions.isFactionMember(player, faction);
 *
 * // Get claims in a chunk
 * Set<Claim> claims = Factions.getClaims(chunk);
 * }</pre>
 *
 * <h2>Version</h2>
 * <p>API Version: 1.0.0</p>
 *
 * @author Factions Team
 * @see Faction
 * @see Claim
 * @since 1.0.0
 */
public final class Factions {

    private static final Logger logger = LoggerFactory.getLogger(Factions.class);
    private static final String API_VERSION = "1.0.0";

    /** Core implementation provided by the plugin at runtime. */
    private static FactionsCore core;

    private Factions() {
        throw new AssertionError("Cannot instantiate Factions API");
    }

    /**
     * Set the core implementation. Called by the plugin during startup.
     *
     * @param core core service
     */
    static void setCore(FactionsCore core) {
        if (Factions.core != null) {
            logger.warn("Factions core already set! Overwriting (possible reload).");
        }
        Factions.core = core;
        logger.info("Factions API loaded (v{})", API_VERSION);
    }

    /**
     * Clear the core implementation. Called by the plugin during shutdown.
     */
    static void clearCore() {
        Factions.core = null;
    }

    /**
     * Get the API version string.
     *
     * @return version
     */
    public static String getAPIVersion() {
        return API_VERSION;
    }

    /**
     * Check if the API is available (plugin loaded).
     *
     * @return true if core is present
     */
    public static boolean isAvailable() {
        return core != null;
    }

    // ==================== Faction Lookup ====================

    /**
     * Get the faction a player belongs to.
     *
     * @param playerId player UUID
     * @return owning faction or null if player has no faction
     */
    public static Faction getFaction(UUID playerId) {
        ensureCore();
        return core.getFaction(playerId);
    }

    /**
     * Get the faction a player belongs to.
     *
     * @param player Bukkit player
     * @return owning faction or null if none
     */
    public static Faction getFaction(Player player) {
        return getFaction(player.getUniqueId());
    }

    /**
     * Get a faction by its exact name.
     *
     * @param name faction name (case-insensitive)
     * @return faction or null if not found
     */
    public static Faction getFactionByName(String name) {
        ensureCore();
        return core.getFactionByName(name);
    }

    /**
     * Get a faction by its tag (abbreviation).
     *
     * @param tag faction tag (case-insensitive)
     * @return faction or null if not found
     */
    public static Faction getFactionByTag(String tag) {
        ensureCore();
        return core.getFactionByTag(tag);
    }

    /**
     * Get the faction that owns a specific chunk.
     *
     * @param chunk world chunk
     * @return owning faction or null if unowned/wilderness
     */
    public static Faction getFactionAt(Chunk chunk) {
        ensureCore();
        return core.getFactionAt(chunk);
    }

    /**
     * Get all claims owned by a faction.
     *
     * @param faction faction
     * @return set of claims
     */
    public static Set<Claim> getClaims(Faction faction) {
        ensureCore();
        return core.getClaims(faction);
    }

    /**
     * Get the claim(s) at a specific chunk location.
     *
     * @param chunk world chunk
     * @return claims at this chunk (usually 0 or 1)
     */
    public static Set<Claim> getClaims(Chunk chunk) {
        ensureCore();
        return core.getClaims(chunk);
    }

    // ==================== Membership Checks ====================

    /**
     * Check if a player is a member of a faction.
     *
     * @param playerId player UUID
     * @param faction faction to check
     * @return true if member
     */
    public static boolean isFactionMember(UUID playerId, Faction faction) {
        ensureCore();
        return core.isFactionMember(playerId, faction);
    }

    /**
     * Check if a player is a member of a faction.
     *
     * @param player Bukkit player
     * @param faction faction to check
     * @return true if member
     */
    public static boolean isFactionMember(Player player, Faction faction) {
        return isFactionMember(player.getUniqueId(), faction);
    }

    // ==================== Territory Utilities ====================

    /**
     * Check if a location is in claimed territory (not wilderness).
     *
     * @param location world location
     * @return true if claimed
     */
    public static boolean isClaimed(Location location) {
        ensureCore();
        return core.isClaimed(location);
    }

    /**
     * Check if a location is in a safezone.
     *
     * @param location world location
     * @return true if safezone
     */
    public static boolean isSafeZone(Location location) {
        ensureCore();
        return core.isSafeZone(location);
    }

    /**
     * Check if a location is in a warzone.
     *
     * @param location world location
     * @return true if warzone
     */
    public static boolean isWarZone(Location location) {
        ensureCore();
        return core.isWarZone(location);
    }

    // ==================== Listener Registration ====================

    /**
     * Register a faction event listener.
     *
     * @param listener listener implementation
     */
    public static void registerListener(FactionListener listener) {
        ensureCore();
        core.registerListener(listener);
    }

    /**
     * Unregister a faction event listener.
     *
     * @param listener listener to remove
     */
    public static void unregisterListener(FactionListener listener) {
        ensureCore();
        core.unregisterListener(listener);
    }

    /**
     * Register a claim event listener.
     *
     * @param listener listener implementation
     */
    public static void registerListener(ClaimListener listener) {
        ensureCore();
        core.registerClaimListener(listener);
    }

    /**
     * Unregister a claim event listener.
     *
     * @param listener listener to remove
     */
    public static void unregisterListener(ClaimListener listener) {
        ensureCore();
        core.unregisterClaimListener(listener);
    }

    /**
     * Register an economy event listener (if Vault integration is enabled).
     *
     * @param listener listener implementation
     */
    public static void registerListener(EconomyListener listener) {
        ensureCore();
        core.registerEconomyListener(listener);
    }

    /**
     * Unregister an economy event listener.
     *
     * @param listener listener to remove
     */
    public static void unregisterListener(EconomyListener listener) {
        ensureCore();
        core.unregisterEconomyListener(listener);
    }

    // ==================== PlaceholderAPI Integration ====================

    /**
     * Register a custom PlaceholderAPI expansion.
     *
     * <p>This method is optional and only needed if you want to add custom
     * placeholders beyond the built-in ones. The plugin registers its own
     * expansions automatically when PlaceholderAPI is detected.</p>
     *
     * @param expansion placeholder expansion instance
     * @return true if registered successfully
     */
    public static boolean registerPlaceholderExpansion(me.clip.placeholderapi.expansion.PlaceholderExpansion expansion) {
        ensureCore();
        return core.registerPlaceholderExpansion(expansion);
    }

    /**
     * Unregister a custom PlaceholderAPI expansion.
     *
     * @param expansion expansion to remove
     * @return true if unregistered
     */
    public static boolean unregisterPlaceholderExpansion(me.clip.placeholderapi.expansion.PlaceholderExpansion expansion) {
        ensureCore();
        return core.unregisterPlaceholderExpansion(expansion);
    }

    // ==================== Utility Methods ====================

    /**
     * Get all factions currently loaded.
     *
     * @return set of all factions
     */
    public static Set<Faction> getAllFactions() {
        ensureCore();
        return core.getAllFactions();
    }

    /**
     * Get the total number of factions.
     *
     * @return faction count
     */
    public static int getFactionCount() {
        return getAllFactions().size();
    }

    /**
     * Check if a faction name is valid (not taken).
     *
     * @param name name to check
     * @return true if available
     */
    public static boolean isNameAvailable(String name) {
        ensureCore();
        return core.isNameAvailable(name);
    }

    /**
     * Check if a faction tag is valid (not taken).
     *
     * @param tag tag to check
     * @return true if available
     */
    public static boolean isTagAvailable(String tag) {
        ensureCore();
        return core.isTagAvailable(tag);
    }

    // ==================== Private Helpers ====================

    private static void ensureCore() {
        if (core == null) {
            throw new IllegalStateException("Factions API is not available. " +
                    "Ensure the Factions plugin is enabled.");
        }
    }
}
