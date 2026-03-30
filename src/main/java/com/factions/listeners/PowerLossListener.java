package com.factions.listeners;

import com.factions.FactionsPlugin;
import com.factions.api.Faction;
import com.factions.service.PowerService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listener specifically for power loss on player death.
 * Separated from general player events for clarity and separation of concerns.
 */
public class PowerLossListener implements Listener {

    private static final Logger LOGGER = Logger.getLogger(PowerLossListener.class.getName());

    private final FactionsPlugin plugin;
    private final PowerService powerService;

    public PowerLossListener(FactionsPlugin plugin) {
        this.plugin = plugin;
        this.powerService = plugin.getPowerService();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        try {
            // Get death penalty from config
            double deathPenaltyPercent = plugin.getConfig().getDouble("power.death-penalty-percent", 10.0);
            double penalty = powerService.getPower(playerId) * (deathPenaltyPercent / 100.0);

            // Apply power loss
            powerService.removePower(playerId, penalty);

            // Increment death count tracking
            powerService.incrementDeathCount(playerId);

            // Recalculate faction power if player is in a faction
            Faction faction = getPlayerFaction(playerId);
            if (faction != null) {
                powerService.recalculateFactionPower(faction);
            }

            LOGGER.info("Applied " + deathPenaltyPercent + "% power penalty (" + penalty + ") to " + player.getName());

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to apply death penalty to " + player.getName(), e);
        }
    }

    /**
     * Gets the faction a player belongs to.
     */
    private Faction getPlayerFaction(UUID playerId) {
        for (Faction faction : plugin.getFactionService().getAllFactions()) {
            if (faction.hasMember(playerId)) {
                return faction;
            }
        }
        return null;
    }
}
