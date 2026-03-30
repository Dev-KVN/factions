package com.factions.event;

import com.factions.api.Faction;
import com.factions.api.RelationState;
import com.factions.api.RelationType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Base class for all relation-related events.
 */
public abstract class RelationEvent extends Event {

    private final Faction factionA;
    private final Faction factionB;
    private final RelationType relationType;
    private final RelationState state;
    private final Player player; // player who initiated the change

    public RelationEvent(Faction factionA, Faction factionB, RelationType relationType, RelationState state, Player player) {
        this.factionA = factionA;
        this.factionB = factionB;
        this.relationType = relationType;
        this.state = state;
        this.player = player;
    }

    public Faction getFactionA() {
        return factionA;
    }

    public Faction getFactionB() {
        return factionB;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public RelationState getState() {
        return state;
    }

    public Player getPlayer() {
        return player;
    }
}
