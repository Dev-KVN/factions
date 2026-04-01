package com.factions.listeners;

import com.factions.FactionsPlugin;
import com.factions.api.Faction;
import com.factions.service.BountyService;
import com.factions.event.FactionDisbandEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listener for bounty-related events.
 * Handles automatic payouts on kills and cleanup on faction disband.
 */
public class BountyListener implements Listener {

    private static final Logger LOGGER = Logger.getLogger(BountyListener.class.getName());
    private final FactionsPlugin plugin;

    public BountyListener(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller(); // Bukkit method, may be null

        if (killer == null) {
            return; // no player killer, ignore
        }

        // Get factions for victim and killer
        Faction victimFaction = plugin.getFactionService().getFactionByPlayer(victim.getUniqueId());
        Faction killerFaction = plugin.getFactionService().getFactionByPlayer(killer.getUniqueId());

        if (victimFaction == null || killerFaction == null) {
            return; // either not in a faction
        }

        // Same faction? ignore
        if (victimFaction.getId().equals(killerFaction.getId())) {
            return;
        }

        // Pay out bounties on victim's faction to killer's faction
        try {
            BountyService bountyService = plugin.getBountyService();
            double payout = bountyService.payBountiesToKiller(victimFaction.getId(), killerFaction);
            if (payout > 0) {
                killer.sendMessage("§6[Bounty] §eYou collected " + payout + " from bounties on " + victimFaction.getTag());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error processing bounty payout", e);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFactionDisband(FactionDisbandEvent event) {
        Faction disbanded = event.getFaction();
        UUID factionId = disbanded.getId();

        try {
            BountyService bountyService = plugin.getBountyService();
            // Refund any bounties that targeted this faction
            double refunded = bountyService.refundBountiesForTarget(factionId);

            // Clean up any bounties placed by this faction
            bountyService.deleteBountiesByPlacer(factionId);

            LOGGER.info("Processed bounty refunds/cleanup for disbanded faction " + disbanded.getTag() + " (refunded: " + refunded + ")");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to process bounties for disbanded faction " + disbanded.getTag(), e);
        }
    }
}
