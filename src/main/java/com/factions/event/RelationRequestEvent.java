package com.factions.event;

import com.factions.api.Faction;
import com.factions.api.RelationState;
import com.factions.api.RelationType;
import org.bukkit.entity.Player;

/**
 * Called when a relation request is sent from one faction to another.
 * The request is in PENDING state.
 */
public class RelationRequestEvent extends RelationEvent {

    public RelationRequestEvent(Faction from, Faction to, RelationType type, Player requester) {
        super(from, to, type, RelationState.PENDING, requester);
    }
}
