package com.factions.api;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Interface representing a Faction in the system.
 * Provides the contract for faction operations and data access.
 */
public interface Faction {

    /**
     * Gets the unique identifier for this faction.
     */
    UUID getId();

    /**
     * Gets the faction tag (display name, max 4 characters typically).
     */
    String getTag();

    /**
     * Sets the faction tag.
     */
    void setTag(String tag);

    /**
     * Gets the faction description.
     */
    String getDescription();

    /**
     * Sets the faction description.
     */
    void setDescription(String description);

    /**
     * Gets the faction name (full name).
     */
    String getName();

    /**
     * Sets the faction name.
     */
    void setName(String name);

    /**
     * Gets the message of the day.
     */
    String getMotd();

    /**
     * Sets the message of the day.
     */
    void setMotd(String motd);

    /**
     * Gets the leader's UUID.
     */
    UUID getLeaderId();

    /**
     * Sets the leader's UUID.
     */
    void setLeaderId(UUID leaderId);

    /**
     * Gets the bank balance (if economy enabled).
     */
    double getBankBalance();

    /**
     * Sets the bank balance.
     */
    void setBankBalance(double balance);

    /**
     * Gets when this faction was created.
     */
    long getCreatedAt();

    /**
     * Sets the creation timestamp.
     */
    void setCreatedAt(long timestamp);

    /**
     * Gets the list of member IDs in this faction.
     */
    Set<UUID> getMembers();

    /**
     * Adds a member to the faction.
     */
    void addMember(UUID playerId);

    /**
     * Removes a member from the faction.
     */
    void removeMember(UUID playerId);

    /**
     * Checks if a player is in this faction.
     */
    boolean hasMember(UUID playerId);

    /**
     * Gets the list of invited player UUIDs.
     */
    Set<UUID> getInvites();

    /**
     * Adds an invite for a player.
     */
    void addInvite(UUID playerId);

    /**
     * Removes an invite for a player.
     */
    void removeInvite(UUID playerId);

    /**
     * Gets the list of banned player UUIDs.
     */
    Set<UUID> getBanned();

    /**
     * Bans a player from joining the faction.
     */
    void ban(UUID playerId);

    /**
     * Unbans a player.
     */
    void unban(UUID playerId);

    /**
     * Gets the relation type with another faction.
     */
    RelationState getRelation(Faction other);

    /**
     * Sets the relation with another faction.
     */
    void setRelation(Faction other, RelationState state);

    /**
     * Gets the total faction power (sum of member power).
     */
    double getPower();

    /**
     * Sets the total faction power.
     */
    void setPower(double power);

    /**
     * Gets the maximum land claims allowed based on power.
     */
    int getMaxClaims();

    /**
     * Sets the maximum land claims.
     */
    void setMaxClaims(int maxClaims);

    /**
     * Gets the current number of claimed chunks.
     */
    int getClaimCount();

    /**
     * Sets the claim count.
     */
    void setClaimCount(int claimCount);

    /**
     * Gets all claimed chunk coordinates.
     */
    List<Claim> getClaims();

    /**
     * Adds a claim to the faction.
     */
    void addClaim(Claim claim);

    /**
     * Removes a claim from the faction.
     */
    void removeClaim(Claim claim);

    /**
     * Gets the home world name.
     */
    String getHomeWorld();

    /**
     * Sets the home world name.
     */
    void setHomeWorld(String world);

    /**
     * Gets the home X coordinate.
     */
    int getHomeX();

    /**
     * Sets the home X coordinate.
     */
    void setHomeX(int x);

    /**
     * Gets the home Y coordinate.
     */
    int getHomeY();

    /**
     * Sets the home Y coordinate.
     */
    void setHomeY(int y);

    /**
     * Gets the home Z coordinate.
     */
    int getHomeZ();

    /**
     * Sets the home Z coordinate.
     */
    void setHomeZ(int z);

    /**
     * Checks if the faction has a home set.
     */
    boolean hasHome();

    /**
     * Gets the peaceful flag (safezone-like).
     */
    boolean isPeaceful();

    /**
     * Sets the peaceful flag.
     */
    void setPeaceful(boolean peaceful);

    /**
     * Gets the permanent flag (cannot be disbanded).
     */
    boolean isPermanent();

    /**
     * Sets the permanent flag.
     */
    void setPermanent(boolean permanent);

    /**
     * Gets the last seen timestamp for activity tracking.
     */
    long getLastSeen();

    /**
     * Sets the last seen timestamp.
     */
    void setLastSeen(long timestamp);
}
