package com.factions.relation;

import com.factions.api.Faction;
import com.factions.api.Relation;
import com.factions.api.RelationState;
import com.factions.api.RelationType;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Manages diplomatic relations between factions.
 *
 * <p>Handles creation, modification, and queries of relations between factions,
 * including request state (PENDING/ACCEPTED) and tracking of who requested.</p>
 */
public interface RelationManager {

    /**
     * Get the relation between two factions, if any.
     *
     * @param factionA first faction
     * @param factionB second faction
     * @return relation object or null if no relation exists
     */
    Relation getRelation(Faction factionA, Faction factionB);

    /**
     * Initiate a relation request from one faction to another.
     *
     * @param from faction sending request
     * @param to faction receiving request
     * @param type requested relation type
     * @param requester player who requested
     * @return the created pending relation
     */
    Relation requestRelation(Faction from, Faction to, RelationType type, Player requester);

    /**
     * Accept a pending relation request.
     *
     * @param relation the pending relation to accept
     * @param accepter player accepting
     */
    void acceptRelation(Relation relation, Player accepter);

    /**
     * Break an existing relation (set to neutral or delete?).
     *
     * @param relation the relation to break
     * @param breaker player breaking the relation
     */
    void breakRelation(Relation relation, Player breaker);

    /**
     * Get all relations involving a given faction.
     *
     * @param faction the faction
     * @return set of relations
     */
    Set<Relation> getRelations(Faction faction);

    /**
     * Check if two factions have an accepted relation of the given type.
     *
     * @param factionA first faction
     * @param factionB second faction
     * @param type relation type to check
     * @return true if accepted relation exists
     */
    boolean hasAcceptedRelation(Faction factionA, Faction factionB, RelationType type);

    /**
     * Deny a pending relation request.
     *
     * @param relation the pending relation to deny
     * @param denier player denying
     */
    void denyRelation(Relation relation, Player denier);

    /**
     * Cancel a pending request that the player's faction sent.
     *
     * @param from faction that sent the request
     * @param to faction that received the request
     * @param canceller player cancelling
     */
    void cancelRequest(Faction from, Faction to, Player canceller);

    /**
     * Get all pending relation requests received by a faction.
     *
     * @param faction faction receiving requests
     * @return set of pending relations
     */
    Set<Relation> getPendingRequests(Faction faction);

    /**
     * Get the request expiration time (milliseconds). Requests older than this are considered expired.
     *
     * @return expiration time in ms
     */
    long getRequestExpiration();

    /**
     * Check if a faction can access another faction's territory (build/interact) based on their relation.
     *
     * <p>Access is granted if:
     * <ul>
     *   <li>The factions are the same (owner accessing own claim)</li>
     *   <li>They have an accepted ALLY or TRUCE relation</li>
     * </ul>
     *
     * @param viewer faction attempting to access
     * @param target faction owning the territory
     * @return true if access should be allowed
     */
    default boolean canAccessTerritory(Faction viewer, Faction target) {
        if (viewer.getId().equals(target.getId())) {
            return true;
        }
        Relation rel = getRelation(viewer, target);
        return rel != null && rel.getState() == RelationState.ACCEPTED &&
               (rel.getRelation() == RelationType.ALLY || rel.getRelation() == RelationType.TRUCE);
    }

    /**
     * Check if a relation is expired based on the manager's expiration time.
     *
     * @param relation relation to check
     * @return true if expired
     */
    default boolean isExpired(Relation relation) {
        return System.currentTimeMillis() - relation.getTimestamp() > getRequestExpiration();
    }
}
