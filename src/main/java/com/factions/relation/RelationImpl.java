package com.factions.relation;

import com.factions.api.Faction;
import com.factions.api.Relation;
import com.factions.api.RelationState;
import com.factions.api.RelationType;

import java.util.UUID;

/**
 * Concrete implementation of the Relation interface.
 */
public class RelationImpl implements Relation {

    private final Faction factionA;
    private final Faction factionB;
    private RelationType relation;
    private long timestamp;
    private final UUID requestedBy;
    private RelationState state;

    public RelationImpl(Faction factionA, Faction factionB, RelationType relation, UUID requestedBy, RelationState state) {
        // Ensure canonical ordering: factionA is the one with smaller UUID
        if (factionA.getId().compareTo(factionB.getId()) <= 0) {
            this.factionA = factionA;
            this.factionB = factionB;
        } else {
            this.factionA = factionB;
            this.factionB = factionA;
        }
        this.relation = relation;
        this.timestamp = System.currentTimeMillis();
        this.requestedBy = requestedBy;
        this.state = state;
    }

    @Override
    public Faction getFactionA() {
        return factionA;
    }

    @Override
    public Faction getFactionB() {
        return factionB;
    }

    @Override
    public RelationType getRelation() {
        return relation;
    }

    public void setRelation(RelationType relation) {
        this.relation = relation;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public UUID getRequestedBy() {
        return requestedBy;
    }

    @Override
    public RelationState getState() {
        return state;
    }

    public void setState(RelationState state) {
        this.state = state;
    }
}
