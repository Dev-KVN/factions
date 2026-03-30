package com.factions.api.extension;

import com.factions.api.Faction;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Listener for faction-related events.
 *
 * <p>Implement this interface to receive notifications when factions are created,
 * disbanded, or when players join/leave factions. Register your listener via
 * {@link com.factions.api.Factions#registerListener(FactionListener)}.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Factions.registerListener(new FactionListener() {
 *     @Override
 *     public void onFactionCreate(Faction faction) {
 *         getLogger().info("Faction created: " + faction.getName());
 *     }
 * });
 * }</pre>
 *
 * @author Factions Team
 * @since 1.0.0
 */
public interface FactionListener {

    /**
     * Called when a faction is created.
     *
     * @param faction the newly created faction
     */
    void onFactionCreate(Faction faction);

    /**
     * Called when a faction is disbanded.
     *
     * @param faction the disbanded faction
     */
    void onFactionDisband(Faction faction);

    /**
     * Called when a faction is renamed.
     *
     * @param faction the faction
     * @param oldName previous name
     * @param newName new name
     */
    void onFactionRename(Faction faction, String oldName, String newName);

    /**
     * Called when a player joins a faction.
     *
     * @param faction the faction joined
     * @param playerId player UUID
     */
    void onPlayerJoin(Faction faction, UUID playerId);

    /**
     * Called when a player leaves a faction (kick or quit).
     *
     * @param faction the faction
     * @param playerId player UUID
     */
    void onPlayerLeave(Faction faction, UUID playerId);

    /**
     * Called when a player is invited to a faction.
     *
     * @param faction the faction
     * @param playerId invited player UUID
     */
    void onPlayerInvite(Faction faction, UUID playerId);

    /**
     * Called when a player accepts a faction invite.
     *
     * @param faction the faction
     * @param playerId player UUID
     */
    void onPlayerInviteAccept(Faction faction, UUID playerId);

    /**
     * Called when a player denies a faction invite.
     *
     * @param faction the faction
     * @param playerId player UUID
     */
    void onPlayerInviteDeny(Faction faction, UUID playerId);

    /**
     * Called when a player is kicked from a faction.
     *
     * @param faction the faction
     * @param playerId player UUID
     * @param kickedBy who kicked the player
     */
    void onPlayerKick(Faction faction, UUID playerId, UUID kickedBy);

    /**
     * Called when a player is banned from a faction.
     *
     * @param faction the faction
     * @param playerId banned player UUID
     * @param bannedBy who banned the player
     */
    void onPlayerBan(Faction faction, UUID playerId, UUID bannedBy);

    /**
     * Called when a player is unbanned from a faction.
     *
     * @param faction the faction
     * @param playerId unbanned player UUID
     * @param unbannedBy who unbanned the player
     */
    void onPlayerUnban(Faction faction, UUID playerId, UUID unbannedBy);

    /**
     * Called when a member's role changes.
     *
     * @param faction the faction
     * @param playerId player UUID
     * @param oldRole previous role
     * @param newRole new role
     */
    void onRoleChange(Faction faction, UUID playerId, String oldRole, String newRole);

    /**
     * Called when the faction description is updated.
     *
     * @param faction the faction
     * @param oldDesc previous description
     * @param newDesc new description
     */
    void onDescriptionChange(Faction faction, String oldDesc, String newDesc);

    /**
     * Called when the faction tag is updated.
     *
     * @param faction the faction
     * @param oldTag previous tag
     * @param newTag new tag
     */
    void onTagChange(Faction faction, String oldTag, String newTag);

    /**
     * Called when the faction MOTD is updated.
     *
     * @param faction the faction
     * @param oldMotd previous MOTD
     * @param newMotd new MOTD
     */
    void onMotdChange(Faction faction, String oldMotd, String newMotd);

    /**
     * Called when the faction home is set.
     *
     * @param faction the faction
     * @param location new home location
     */
    void onHomeSet(Faction faction, Location location);

    /**
     * Called when a faction opens its bank (economy integration).
     *
     * @param faction the faction
     * @param player who opened the bank
     * @param currentBalance current balance
     */
    default void onBankOpen(Faction faction, Player player, double currentBalance) {
        // default no-op
    }

    /**
     * Called when money is deposited into the faction bank.
     *
     * @param faction the faction
     * @param player who deposited (null for console/auto)
     * @param amount amount deposited
     */
    default void onBankDeposit(Faction faction, Player player, double amount) {
        // default no-op
    }

    /**
     * Called when money is withdrawn from the faction bank.
     *
     * @param faction the faction
     * @param player who withdrew (null for console/auto)
     * @param amount amount withdrawn
     */
    default void onBankWithdraw(Faction faction, Player player, double amount) {
        // default no-op
    }
}
