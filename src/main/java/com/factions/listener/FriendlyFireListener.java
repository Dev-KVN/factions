package com.factions.listener;

import com.factions.api.Faction;
import com.factions.api.Factions;
import com.factions.api.RelationState;
import com.factions.api.RelationType;
import com.factions.relation.RelationManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Prevents friendly fire between factions with ALLY or TRUCE relations.
 *
 * <p>When a player attempts to damage another player, this listener checks
 * their faction relations. If both players belong to factions that have an
 * accepted ALLY or TRUCE relation, the damage is cancelled.</p>
 */
public class FriendlyFireListener implements Listener {

    private final RelationManager relationManager;

    public FriendlyFireListener(RelationManager relationManager) {
        this.relationManager = relationManager;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Only handle player vs player damage
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (!(damager instanceof Player attacker) || !(entity instanceof Player victim)) {
            return; // Not PvP
        }

        // Get factions
        Faction attackerFaction = Factions.getFaction(attacker.getUniqueId());
        Faction victimFaction = Factions.getFaction(victim.getUniqueId());

        // If either has no faction, allow damage (no relation)
        if (attackerFaction == null || victimFaction == null) {
            return;
        }

        // Same faction? Typically factions have internal rules. We'll ignore same faction (maybe already handled elsewhere). For now, treat same faction as ally? Usually same faction players cannot damage each other (friendly fire is off). But that's separate from inter-faction relations. We'll treat same faction as already safe, so cancel.
        if (attackerFaction.equals(victimFaction)) {
            event.setCancelled(true);
            return;
        }

        // Check relation
        var relation = relationManager.getRelation(attackerFaction, victimFaction);
        if (relation != null && relation.getState() == RelationState.ACCEPTED) {
            RelationType type = relation.getRelation();
            if (type == RelationType.ALLY || type == RelationType.TRUCE) {
                // Cancel friendly fire between allies/truce
                event.setCancelled(true);
            }
        }
    }
}
