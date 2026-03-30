package com.factions.api;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a land claim (typically a chunk) owned by a faction.
 */
public class Claim {

    private final UUID factionId;
    private final String world;
    private final int chunkX;
    private final int chunkZ;
    private long claimedAt;
    private String claimedBy; // Player who claimed it

    public Claim(UUID factionId, String world, int chunkX, int chunkZ) {
        this.factionId = factionId;
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.claimedAt = System.currentTimeMillis();
        this.claimedBy = null;
    }

    public UUID getFactionId() {
        return factionId;
    }

    public String getWorld() {
        return world;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public long getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(long claimedAt) {
        this.claimedAt = claimedAt;
    }

    public String getClaimedBy() {
        return claimedBy;
    }

    public void setClaimedBy(String claimedBy) {
        this.claimedBy = claimedBy;
    }

    /**
     * Checks if this claim contains a block at the given coordinates.
     */
    public boolean contains(int x, int z) {
        int chunkMinX = chunkX << 4;
        int chunkMaxX = (chunkX << 4) + 15;
        int chunkMinZ = chunkZ << 4;
        int chunkMaxZ = (chunkZ << 4) + 15;
        return x >= chunkMinX && x <= chunkMaxX && z >= chunkMinZ && z <= chunkMaxZ;
    }

    /**
     * Gets the center of this chunk (block coordinates).
     */
    public int getCenterX() {
        return (chunkX << 4) + 7;
    }

    public int getCenterZ() {
        return (chunkZ << 4) + 7;
    }

    /**
     * Gets the corner coordinates of this chunk.
     */
    public int getMinX() {
        return chunkX << 4;
    }

    public int getMinZ() {
        return chunkZ << 4;
    }

    public int getMaxX() {
        return (chunkX << 4) + 15;
    }

    public int getMaxZ() {
        return (chunkZ << 4) + 15;
    }

    /**
     * Checks if this claim is adjacent to another claim.
     * Adjacent means sharing a border (not just a corner).
     */
    public boolean isAdjacentTo(Claim other) {
        if (!this.world.equals(other.world)) {
            return false;
        }

        int dx = Math.abs(this.chunkX - other.chunkX);
        int dz = Math.abs(this.chunkZ - other.chunkZ);

        return (dx == 1 && dz == 0) || (dx == 0 && dz == 1);
    }

    /**
     * Checks if this claim connects to a set of claims (for connected claims enforcement).
     */
    public boolean connectsTo(Set<Claim> claims) {
        for (Claim other : claims) {
            if (other.getId().equals(this.getId())) {
                continue;
            }
            if (isAdjacentTo(other)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Claim claim = (Claim) o;
        return chunkX == claim.chunkX &&
               chunkZ == claim.chunkZ &&
               Objects.equals(world, claim.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, chunkX, chunkZ);
    }

    @Override
    public String toString() {
        return String.format("Claim{world='%s', chunkX=%d, chunkZ=%d, faction=%s}",
                world, chunkX, chunkZ, factionId);
    }

    // Helper to get unique ID for this claim (used for sets)
    public String getId() {
        return world + ":" + chunkX + ":" + chunkZ;
    }
}
