package com.factions.api;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a bounty placed by one faction on another.
 */
public class Bounty {

    private UUID id;
    private UUID targetFactionId;
    private UUID placerFactionId;
    private double amount;
    private long placedAt;

    public Bounty() {
        this.placedAt = System.currentTimeMillis();
    }

    public Bounty(UUID targetFactionId, UUID placerFactionId, double amount) {
        this();
        this.id = UUID.randomUUID();
        this.targetFactionId = targetFactionId;
        this.placerFactionId = placerFactionId;
        this.amount = amount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTargetFactionId() {
        return targetFactionId;
    }

    public void setTargetFactionId(UUID targetFactionId) {
        this.targetFactionId = targetFactionId;
    }

    public UUID getPlacerFactionId() {
        return placerFactionId;
    }

    public void setPlacerFactionId(UUID placerFactionId) {
        this.placerFactionId = placerFactionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(long placedAt) {
        this.placedAt = placedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bounty bounty = (Bounty) o;
        return Objects.equals(id, bounty.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Bounty{" +
                "id=" + id +
                ", targetFactionId=" + targetFactionId +
                ", placerFactionId=" + placerFactionId +
                ", amount=" + amount +
                ", placedAt=" + placedAt +
                '}';
    }
}
