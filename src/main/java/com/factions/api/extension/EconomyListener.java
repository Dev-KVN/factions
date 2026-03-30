package com.factions.api.extension;

import com.factions.api.Faction;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

/**
 * Listener for faction economy events.
 *
 * <p>Economy integration requires Vault and an economy provider. This listener
 * allows you to track deposits, withdrawals, and transactions related to faction
 * banks.</p>
 *
 * <p>Register via {@link com.factions.api.Factions#registerListener(EconomyListener)}.</p>
 *
 * @author Factions Team
 * @since 1.0.0
 */
public interface EconomyListener {

    /**
     * Called when a player deposits money into a faction bank.
     *
     * @param faction recipient faction
     * @param player who deposited
     * @param amount amount deposited
     */
    void onDeposit(Faction faction, Player player, BigDecimal amount);

    /**
     * Called when money is withdrawn from a faction bank.
     *
     * @param faction source faction
     * @param player who withdrew
     * @param amount amount withdrawn
     */
    void onWithdraw(Faction faction, Player player, BigDecimal amount);

    /**
     * Called when a faction's balance changes (any source).
     *
     * @param faction affected faction
     * @param oldBalance previous balance
     * @param newBalance new balance
     * @param reason transaction reason (deposit, withdraw, tax, etc.)
     */
    void onBalanceChange(Faction faction, BigDecimal oldBalance, BigDecimal newBalance, String reason);

    /**
     * Called when a faction is overdrawn (negative balance) or returns to positive.
     *
     * @param faction affected faction
     * @param inDebt true if now in debt
     */
    default void onDebtStatusChange(Faction faction, boolean inDebt) {
        // default no-op
    }

    /**
     * Called when a claim cost is paid.
     *
     * @param faction faction paying
     * @param amount amount paid
     * @param claimCount total claims after payment
     */
    default void onClaimPayment(Faction faction, BigDecimal amount, int claimCount) {
        // default no-op
    }
}
