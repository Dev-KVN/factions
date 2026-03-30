package com.factions.economic;

import java.util.UUID;

/**
 * Provider interface for economy integration.
 * Allows linking power-based costs with an economy system like Vault.
 */
public interface PowerProvider {

    /**
     * Gets the bank balance for a player.
     *
     * @return balance in economy units
     */
    double getBankBalance(UUID playerId);

    /**
     * Withdraws money from a player's account.
     *
     * @return true if withdrawal succeeded, false if insufficient funds
     */
    boolean withdraw(UUID playerId, double amount);

    /**
     * Deposits money into a player's account.
     */
    void deposit(UUID playerId, double amount);

    /**
     * Checks if the player can afford the given amount.
     */
    boolean canAfford(UUID playerId, double amount);
}
