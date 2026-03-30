package com.factions.api;

import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Represents a diplomatic relationship between two factions.
 */
public class Relation {

    public enum RelationType {
        ALLY,
        ENEMY,
        TRUCE,
        NEUTRAL
    }

    private final UUID factionAId;
    private final UUID factionBId;
    private RelationType type;
    private LocalDateTime establishedAt;
    private LocalDateTime expiresAt; // For truces
    private String establishedBy; // Player who set this relation

    public Relation(UUID factionAId, UUID factionBId, RelationType type) {
        this.factionAId = factionAId;
        this.factionBId = factionBId;
        this.type = type;
        this.establishedAt = LocalDateTime.now();
        this.expiresAt = null;
        this.establishedBy = null;
    }

    public UUID getFactionAId() {
        return factionAId;
    }

    public UUID getFactionBId() {
        return factionBId;
    }

    public RelationType getType() {
        return type;
    }

    public void setType(RelationType type) {
        this.type = type;
        if (type == RelationType.NEUTRAL) {
            this.expiresAt = null;
        }
    }

    public LocalDateTime getEstablishedAt() {
        return establishedAt;
    }

    public void setEstablishedAt(LocalDateTime establishedAt) {
        this.establishedAt = establishedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getEstablishedBy() {
        return establishedBy;
    }

    public void setEstablishedBy(String establishedBy) {
        this.establishedBy = establishedBy;
    }

    /**
     * Checks if this relation is currently active (not expired).
     */
    public boolean isActive() {
        if (type == RelationType.NEUTRAL) {
            return false;
        }
        if (expiresAt == null) {
            return true;
        }
        return LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Sets an expiration for time-limited relations (like truce).
     */
    public void setExpirationInHours(int hours) {
        this.expiresAt = LocalDateTime.now().plusHours(hours);
    }

    /**
     * Clears expiration (for permanent relations).
     */
    public void clearExpiration() {
        this.expiresAt = null;
    }

    /**
     * Gets the reciprocal relation type.
     * ALLY <-> ALLY, ENEMY <-> ENEMY, NEUTRAL/TRUCE are symmetric in most implementations.
     */
    public RelationType getReciprocalType() {
        switch (type) {
            case ALLY:
                return RelationType.ALLY;
            case ENEMY:
                return RelationType.ENEMY;
            case TRUCE:
                return RelationType.TRUCE;
            case NEUTRAL:
            default:
                return RelationType.NEUTRAL;
        }
    }

    /**
     * Checks if this relation allows PvP between the factions.
     */
    public boolean allowsPvP() {
        return type == RelationType.ENEMY || type == RelationType.NEUTRAL;
    }

    /**
     * Checks if this relation allows building/interaction in territories.
     */
    public boolean allowsBuild() {
        return type == RelationType.ALLY;
    }

    /**
     * Checks if this relation allows container access.
     */
    public boolean allowsContainerAccess() {
        return type == RelationType.ALLY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relation relation = (Relation) o;
        return factionAId.equals(relation.factionAId) &&
               factionBId.equals(relation.factionBId);
    }

    @Override
    public int hashCode() {
        return factionAId.hashCode() * 31 + factionBId.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Relation{factionA=%s, factionB=%s, type=%s, active=%s}",
                factionAId, factionBId, type, isActive());
    }
}
