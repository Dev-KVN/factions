package com.factions.api;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Core service interface implemented by the Factions plugin.
 *
 * <p>This interface defines the internal contract that the API facade delegates
 * to. Plugin developers should not interact with this directly; use the
 * {@link Factions} facade instead.</p>
 *
 * @author Factions Team
 * @since 1.0.0
 */
interface FactionsCore {

    // ==================== Faction Lookup ====================

    Faction getFaction(UUID playerId);

    Faction getFactionByName(String name);

    Faction getFactionByTag(String tag);

    Faction getFactionAt(Chunk chunk);

    Set<Claim> getClaims(Faction faction);

    Set<Claim> getClaims(Chunk chunk);

    // ==================== Membership Checks ====================

    boolean isFactionMember(UUID playerId, Faction faction);

    // ==================== Territory Utilities ====================

    boolean isClaimed(Location location);

    boolean isSafeZone(Location location);

    boolean isWarZone(Location location);

    // ==================== Listener Registration ====================

    void registerListener(FactionListener listener);

    void unregisterListener(FactionListener listener);

    void registerClaimListener(ClaimListener listener);

    void unregisterClaimListener(ClaimListener listener);

    void registerEconomyListener(EconomyListener listener);

    void unregisterEconomyListener(EconomyListener listener);

    // ==================== PlaceholderAPI Integration ====================

    boolean registerPlaceholderExpansion(PlaceholderExpansion expansion);

    boolean unregisterPlaceholderExpansion(PlaceholderExpansion expansion);

    // ==================== Utilities ====================

    Set<Faction> getAllFactions();

    boolean isNameAvailable(String name);

    boolean isTagAvailable(String tag);
}
