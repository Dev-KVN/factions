package com.factions.api;

import com.factions.FactionsPlugin;
import com.factions.service.FactionService;

import java.util.Collection;
import java.util.UUID;

/**
 * Public API for the Factions plugin.
 * Other plugins can use this to interact with factions.
 */
public class FactionsAPI {

    private static FactionsPlugin plugin;

    /**
     * Internal: set the plugin instance.
     */
    public static void setPlugin(FactionsPlugin plugin) {
        FactionsAPI.plugin = plugin;
    }

    /**
     * Gets the faction service.
     */
    public static FactionService getFactionService() {
        return plugin.getFactionService();
    }

    /**
     * Gets a faction by its ID.
     */
    public static Faction getFaction(UUID id) {
        return plugin.getFactionService().getFaction(id);
    }

    /**
     * Gets a faction by its tag (case-insensitive).
     */
    public static Faction getFactionByTag(String tag) {
        return plugin.getFactionService().getFactionByTag(tag);
    }

    /**
     * Gets all factions.
     */
    public static Collection<Faction> getFactions() {
        return plugin.getFactionService().getAllFactions();
    }

    /**
     * Gets the faction a player belongs to.
     */
    public static Faction getPlayerFaction(UUID playerId) {
        for (Faction faction : getFactions()) {
            if (faction.hasMember(playerId)) {
                return faction;
            }
        }
        return null;
    }

    /**
     * Creates a new faction.
     */
    public static Faction createFaction(String name, String tag, UUID leaderId) {
        return plugin.getFactionService().createFaction(name, tag, leaderId);
    }

    /**
     * Checks if a player is in a faction.
     */
    public static boolean hasFaction(UUID playerId) {
        return getPlayerFaction(playerId) != null;
    }

    /**
     * Checks if two factions are allied.
     */
    public static boolean areAllied(Faction a, Faction b) {
        return a.getRelation(b) == RelationState.ALLY;
    }

    /**
     * Checks if two factions are enemies.
     */
    public static boolean areEnemies(Faction a, Faction b) {
        return a.getRelation(b) == RelationState.ENEMY;
    }

    /**
     * Gets the power service for power operations.
     */
    public static com.factions.service.PowerService getPowerService() {
        return plugin.getPowerService();
    }

    /**
     * Gets the claim service for chunk operations.
     */
    public static com.factions.service.ClaimService getClaimService() {
        return plugin.getClaimService();
    }

    /**
     * Gets the relation service for diplomacy operations.
     */
    public static com.factions.service.RelationService getRelationService() {
        return plugin.getRelationService();
    }
}
