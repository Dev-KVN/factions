package com.factions.listeners;

import com.factions.FactionsPlugin;
import com.factions.api.Faction;
import com.factions.api.RelationState;
import com.factions.service.ClaimService;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Listener for chunk and territory protection events.
 */
public class ChunkListener implements Listener {

    private static final Logger LOGGER = Logger.getLogger(ChunkListener.class.getName());
    private final FactionsPlugin plugin;

    public ChunkListener(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        checkClaimProtection(event, player, block.getChunk(), "break");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();

        checkClaimProtection(event, player, block.getChunk(), "place");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Could handle container protection
    }

    /**
     * Checks if a player is allowed to modify a claimed chunk.
     */
    private void checkClaimProtection(org.bukkit.event.Cancellable event, Player player,
                                       Chunk chunk, String action) {
        ClaimService claimService = plugin.getClaimService();

        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        String world = chunk.getWorld().getName();

        claimService.getClaimingFaction(world, chunkX, chunkZ).ifPresent(factionId -> {
            try {
                Faction faction = plugin.getFactionService().getFaction(factionId);
                if (faction == null) {
                    return;
                }

                // Player is a member of the faction that owns the claim
                if (faction.hasMember(player.getUniqueId())) {
                    return; // Allowed
                }

                // Check relation if it's an enemy/ally
                Faction playerFaction = getPlayerFaction(player);
                if (playerFaction != null) {
                    RelationState relation = plugin.getRelationService()
                            .getRelation(playerFaction, faction);

                    if (relation == RelationState.ALLY) {
                        return; // Allies can build
                    }

                    if (relation == RelationState.NEUTRAL || relation == RelationState.ENEMY) {
                        // Enemies/Neutrals cannot build
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "You cannot " + action + " blocks in enemy/neutral territory.");
                    }
                } else {
                    // Player not in a faction - always deny access to claims
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "This land is claimed by " + faction.getTag());
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error checking claim protection", e);
                event.setCancelled(true);
            }
        });
    }

    /**
     * Gets the faction a player belongs to.
     */
    private Faction getPlayerFaction(Player player) {
        try {
            for (Faction faction : plugin.getFactionService().getAllFactions()) {
                if (faction.hasMember(player.getUniqueId())) {
                    return faction;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting player faction", e);
        }
        return null;
    }
}
