package com.factions.api;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a land claim owned by a faction.
 *
 * <p>A claim is typically a Bukkit Chunk. It stores ownership, territory type,
 * and timestamp information.</p>
 *
 * @see Faction#getClaims()
 * @since 1.0.0
 */
public interface Claim {

    /**
     * Get the world name for this claim.
     *
     * @return world name
     */
    String getWorld();

    /**
     * Get the chunk coordinates.
     *
     * @return chunk object
     */
    Chunk getChunk();

    /**
     * Get the faction that owns this claim.
     *
     * @return owning faction
     */
    Faction getFaction();

    /**
     * Get the timestamp when this claim was created.
     *
     * @return creation time in milliseconds since epoch
     */
    long getCreatedAt();

    /**
     * Get the territory type (WILDERNESS, SAFEZONE, WARZONE, or normal faction claim).
     *
     * @return territory type
     */
    TerritoryType getType();

    /**
     * Check if this claim is a buffer zone.
     *
     * @return true if buffer
     */
    boolean isBuffer();

    /**
     * Check if a player can build in this claim.
     *
     * @param player player to check
     * @return true if can build
     */
    boolean canBuild(Player player);

    /**
     * Check if a player can interact with blocks in this claim (containers, doors, etc.).
     *
     * @param player player to check
     * @return true if can interact
     */
    boolean canInteract(Player player);

    /**
     * Check if PvP is enabled in this claim.
     *
     * @return true if PvP allowed
     */
    boolean isPvPEnabled();

    /**
     * Get the X coordinate of the chunk.
     *
     * @return chunk X
     */
    int getChunkX();

    /**
     * Get the Z coordinate of the chunk.
     *
     * @return chunk Z
     */
    int getChunkZ();
}

/**
 * Territory type classification.
 */
enum TerritoryType {
    /**
     * Unclaimed wilderness.
     */
    WILDERNESS,
    /**
     * Safe zone where PvP is disabled and players are protected.
     */
    SAFEZONE,
    /**
     * War zone where PvP is always enabled.
     */
    WARZONE,
    /**
     * Normal faction claim.
     */
    CLAIM
}
