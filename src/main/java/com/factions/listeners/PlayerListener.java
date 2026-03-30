package com.factions.listeners;

import com.factions.FactionsPlugin;
import com.factions.api.Faction;
import com.factions.api.FactionMember;
import com.factions.service.PowerService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listener for player-related events.
 */
public class PlayerListener implements Listener {

    private static final Logger LOGGER = Logger.getLogger(PlayerListener.class.getName());
    private final FactionsPlugin plugin;

    public PlayerListener(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PowerService powerService = plugin.getPowerService();

        // Mark player as online for power regeneration
        powerService.setOnline(player.getUniqueId(), true);

        // Check if player has a faction and update last seen
        try {
            Faction faction = getPlayerFaction(player);
            if (faction != null) {
                faction.setLastSeen(System.currentTimeMillis());
                plugin.getFactionService().saveFaction(faction);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to update faction last seen for " + player.getName(), e);
        }

        LOGGER.fine("Player " + player.getName() + " joined, power regen enabled");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PowerService powerService = plugin.getPowerService();

        // Mark player as offline, power will decay
        powerService.setOnline(player.getUniqueId(), false);

        LOGGER.fine("Player " + player.getName() + " quit, power decay started");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        // Could add power-related effects on respawn
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Faction faction;
        try {
            faction = getPlayerFaction(player);
        } catch (Exception e) {
            return;
        }

        if (faction != null) {
            // Format chat based on faction relations with others
            // This would require checking relations with listeners
            // Simplified: just add faction tag
            event.setFormat("[" + faction.getTag() + "] %1$s: %2$s");
        }
    }

    /**
     * Gets the faction a player belongs to.
     */
    private Faction getPlayerFaction(Player player) {
        for (Faction faction : plugin.getFactionService().getAllFactions()) {
            if (faction.hasMember(player.getUniqueId())) {
                return faction;
            }
        }
        return null;
    }
}
