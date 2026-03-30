package com.factions.api;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a diplomatic relationship between two factions.
 *
 * <p>A relation captures the association between a pair of factions, including
 * the relationship type (ALLY, ENEMY, NEUTRAL, TRUCE), when it was established,
 * who requested it, and its current state (PENDING or ACCEPTED).</p>
 *
 * <p>Relations are considered symmetric: if faction A has a relation with B,
 * then the relation applies equally in both directions. The relation is stored
 * with the lexicographically smaller faction UUID as {@code getFactionA()} to
 * ensure a single canonical entry per faction pair.</p>
 *
 * @since 1.0.0
 */
public interface Relation {

    /**
     * Get the first faction in the pair (lexicographically smaller UUID).
     *
     * @return faction A
     */
    Faction getFactionA();

    /**
     * Get the second faction in the pair (lexicographically larger UUID).
     *
     * @return faction B
     */
    Faction getFactionB();

    /**
     * Get the diplomatic relation type.
     *
     * @return relation type (ALLY, ENEMY, NEUTRAL, or TRUCE)
     */
    RelationType getRelation();

    /**
     * Get the timestamp when this relation was created or last updated.
     *
     * @return timestamp in milliseconds since epoch
     */
    long getTimestamp();

    /**
     * Get the player UUID who requested this relation.
     *
     * @return requester UUID
     */
    UUID getRequestedBy();

    /**
     * Get the current state of the relation.
     *
     * @return PENDING or ACCEPTED
     */
    RelationState getState();

    /**
     * Convenience method to get the other faction involved in this relation.
     *
     * @param faction the faction to exclude
     * @return the other faction in the relation
     */
    default Faction getOther(Faction faction) {
        Faction a = getFactionA();
        Faction b = getFactionB();
        if (faction.equals(a)) {
            return b;
        } else if (faction.equals(b)) {
            return a;
        } else {
            throw new IllegalArgumentException("Faction is not part of this relation");
        }
    }
}
