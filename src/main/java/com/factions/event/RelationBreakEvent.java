package com.factions.event;

import com.factions.api.Faction;
import com.factions.api.RelationState;
import com.factions.api.RelationType;
import org.bukkit.entity.Player;

/**
 * Called when an existing ACCEPTED relation is broken (or a pending request is cancelled).
 * The relation is removed from storage.
 */
public class RelationBreakEvent extends RelationEvent {

    public RelationBreakEvent(Faction from, Faction to, RelationType type, RelationState previousState, Player breaker) {
        super(from, to, type, previousState, breaker);
    }
}
