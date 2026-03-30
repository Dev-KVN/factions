package com.factions.event;

import com.factions.api.Faction;
import com.factions.api.RelationState;
import com.factions.api.RelationType;
import org.bukkit.entity.Player;

/**
 * Called when a pending relation request is accepted.
 * The relation becomes ACCEPTED.
 */
public class RelationAcceptEvent extends RelationEvent {

    public RelationAcceptEvent(Faction from, Faction to, RelationType type, Player accepter) {
        super(from, to, type, RelationState.ACCEPTED, accepter);
    }
}
