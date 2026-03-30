package com.factions.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a Faction in the plugin.
 *
 * <p>This interface provides read-only access to faction properties. To modify
 * a faction, use the methods provided by the {@link Factions} facade or cast
 * to the implementation if you are within the core plugin.</p>
 *
 * <h2>Common Operations</h2>
 * <ul>
 *   <li>Get faction name, tag, description, MOTD</li>
 *   <li>Check membership and roles</li>
 *   <li>Retrieve power and land claims</li>
 *   <li>Access faction home location</li>
 * </ul>
 *
 * @see Factions
 * @since 1.0.0
 */
public interface Faction {

    /**
     * Get the unique identifier of this faction.
     *
     * @return faction UUID
     */
    UUID getId();

    /**
     * Get the display name of this faction.
     *
     * @return faction name
     */
    String getName();

    /**
     * Get the faction tag (abbreviation used in chat, etc.).
     *
     * @return faction tag
     */
    String getTag();

    /**
     * Get the faction description.
     *
     * @return description text
     */
    String getDescription();

    /**
     * Get the faction MOTD (Message of the Day).
     *
     * @return MOTD text
     */
    String getMotd();

    /**
     * Check if this faction is the safezone.
     *
     * @return true if safezone
     */
    boolean isSafeZone();

    /**
     * Check if this faction is the warzone.
     *
     * @return true if warzone
     */
    boolean isWarZone();

    /**
     * Get the total power of this faction (sum of member power).
     *
     * @return faction power
     */
    double getPower();

    /**
     * Get the maximum land claims this faction can have based on power.
     *
     * @return max claims
     */
    int getMaxClaims();

    /**
     * Get the number of claims currently owned.
     *
     * @return claim count
     */
    int getClaimsCount();

    /**
     * Get the faction home location, if set.
     *
     * @return home location or null if not set
     */
    Location getHome();

    /**
     * Check if a player is a member of this faction.
     *
     * @param playerId player UUID
     * @return true if member
     */
    boolean isMember(UUID playerId);

    /**
     * Check if a player is a member of this faction.
     *
     * @param player Bukkit player
     * @return true if member
     */
    boolean isMember(Player player);

    /**
     * Get the role of a player in this faction.
     *
     * @param playerId player UUID
     * @return role name (LEADER, OFFICER, MEMBER, RECRUIT) or null if not a member
     */
    String getRole(UUID playerId);

    /**
     * Get all member UUIDs of this faction.
     *
     * @return set of member UUIDs
     */
    Set<UUID> getMembers();

    /**
     * Get the leader of this faction.
     *
     * @return leader UUID or null if none
     */
    UUID getLeader();

    /**
     * Get all officers of this faction.
     *
     * @return set of officer UUIDs
     */
    Set<UUID> getOfficers();

    /**
     * Check if this faction has an alliance with another faction.
     *
     * @param other other faction
     * @return true if ally
     */
    boolean isAlliedWith(Faction other);

    /**
     * Check if this faction is at war with another faction.
     *
     * @param other other faction
     * @return true if enemy
     */
    boolean isEnemy(Faction other);

    /**
     * Get the diplomatic relation with another faction.
     *
     * @param other other faction
     * @return relation string (ALLY, ENEMY, NEUTRAL, TRUCE)
     */
    String getRelation(Faction other);

    /**
     * Get all claims (chunks) owned by this faction.
     *
     * @return set of claims
     */
    Set<Claim> getClaims();

    /**
     * Get the bank balance for this faction (if economy enabled).
     *
     * @return balance or 0 if economy disabled
     */
    double getBankBalance();
}
