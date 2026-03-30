package com.factions.event;

import com.factions.api.Faction;
import com.factions.api.RelationState;
import com.factions.api.RelationType;
import org.bukkit.entity.Player;

/**
 * Called when a pending relation request is denied.
 * The relation is removed.
 */
public class RelationDenyEvent extends RelationEvent {

    public RelationDenyEvent(Faction from, Faction to, RelationType type, Player denier) {
        super(from, to, type, RelationState.PENDING, denier);
        // Note: after deny, relation is removed, so state remains PENDING for reference.
    }
}
