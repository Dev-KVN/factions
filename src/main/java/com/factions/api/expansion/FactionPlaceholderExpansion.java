package com.factions.api.expansion;

import com.factions.api.Factions;
import com.factions.api.Faction;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * PlaceholderAPI expansion for Factions plugin.
 *
 * <p>Provides the following placeholders:</p>
 * <ul>
 *   <li>%factions_faction% - Returns the player's faction name or "No Faction"</li>
 *   <li>%factions_faction_tag% - Returns the player's faction tag</li>
 *   <li>%factions_faction_power% - Returns the player's faction power</li>
 *   <li>%factions_faction_max_claims% - Returns max claims for player's faction</li>
 *   <li>%factions_faction_claims% - Returns current claim count for player's faction</li>
 *   <li>%factions_faction_balance% - Returns faction bank balance (if economy enabled)</li>
 *   <li>%factions_territory% - Returns territory type at player's location</li>
 *   <li>%factions_owner_faction% - Returns faction that owns current chunk</li>
 * </ul>
 *
 * <p>The plugin automatically registers this expansion when PlaceholderAPI
 * is detected on the server. To use, simply install PlaceholderAPI and this
 * plugin, then use the placeholders in your chat, scoreboard, or other plugins.</p>
 *
 * @author Factions Team
 * @since 1.0.0
 */
public class FactionPlaceholderExpansion extends PlaceholderExpansion {

    private static final String IDENTIFIER = "factions";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String getAuthor() {
        return "FactionsTeam";
    }

    @Override
    public String getVersion() {
        return Factions.getAPIVersion();
    }

    @Override
    public boolean persist() {
        return true; // keep registered across reloads
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return null;
        }

        // Get player's faction
        Faction faction = Factions.getFaction(player);
        Chunk chunk = player.getLocation().getChunk();
        Faction chunkFaction = Factions.getFactionAt(chunk);

        switch (identifier.toLowerCase()) {
            case "faction":
                return faction != null ? faction.getName() : "No Faction";
            case "faction_tag":
                return faction != null ? faction.getTag() : "None";
            case "faction_power":
                return faction != null ? String.valueOf(faction.getPower()) : "0";
            case "faction_max_claims":
                return faction != null ? String.valueOf(faction.getMaxClaims()) : "0";
            case "faction_claims":
                return faction != null ? String.valueOf(faction.getClaimsCount()) : "0";
            case "faction_members":
                return faction != null ? String.valueOf(faction.getMembers().size()) : "0";
            case "faction_balance":
                return faction != null ? String.format("$%.2f", faction.getBankBalance()) : "$0.00";
            case "territory":
                if (chunkFaction != null) {
                    if (chunkFaction.isSafeZone()) return "SAFEZONE";
                    if (chunkFaction.isWarZone()) return "WARZONE";
                    return chunkFaction.getName();
                }
                return "WILDERNESS";
            case "owner_faction":
                return chunkFaction != null ? chunkFaction.getName() : "Wilderness";
            case "role":
                if (faction != null) {
                    String role = faction.getRole(player.getUniqueId());
                    return role != null ? role : "None";
                }
                return "None";
            default:
                return null;
        }
    }
}
