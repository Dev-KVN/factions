package com.factions.service;

import com.factions.api.Bounty;
import com.factions.api.Faction;
import com.factions.persistence.BountyMapper;
import com.factions.persistence.DatabaseManager;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for managing faction bounties.
 * Handles placing, paying out, and refunding bounties.
 */
public class BountyService {

    private static final Logger LOGGER = Logger.getLogger(BountyService.class.getName());

    private final DatabaseManager db;
    private final BountyMapper bountyMapper;
    private final FactionService factionService;

    public BountyService(DatabaseManager db, FactionService factionService) {
        this.db = db;
        this.bountyMapper = new BountyMapper(db);
        this.factionService = factionService;
    }

    /**
     * Places a bounty from one faction on another.
     * Deducts the amount from the placer's bank immediately.
     *
     * @return true if successful, false otherwise
     */
    public boolean placeBounty(Faction placer, Faction target, double amount) {
        if (placer.equals(target)) {
            LOGGER.warning(placer.getTag() + " cannot place a bounty on themselves");
            return false;
        }
        if (amount <= 0) {
            LOGGER.warning("Invalid bounty amount: " + amount);
            return false;
        }
        if (placer.getBankBalance() < amount) {
            LOGGER.warning(placer.getTag() + " has insufficient funds for bounty (need " + amount + ", have " + placer.getBankBalance() + ")");
            return false;
        }

        try {
            // Deduct from placer immediately
            placer.setBankBalance(placer.getBankBalance() - amount);
            factionService.saveFaction(placer);

            // Create bounty record
            Bounty bounty = new Bounty(target.getId(), placer.getId(), amount);
            bountyMapper.insert(bounty);

            LOGGER.info(placer.getTag() + " placed a bounty of " + amount + " on " + target.getTag());
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to place bounty", e);
            return false;
        }
    }

    /**
     * Gets all active bounties for a target faction.
     */
    public List<Bounty> getBountiesForTarget(UUID targetFactionId) {
        try {
            return bountyMapper.findByTargetFactionId(targetFactionId);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to get bounties for target " + targetFactionId, e);
            return List.of();
        }
    }

    /**
     * Gets all bounties placed by a specific faction.
     */
    public List<Bounty> getBountiesForPlacer(UUID placerFactionId) {
        try {
            return bountyMapper.findByPlacerFactionId(placerFactionId);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to get bounties for placer " + placerFactionId, e);
            return List.of();
        }
    }

    /**
     * Pays out bounties for a target faction to the killer's faction.
     * This aggregates all active bounties on the target and transfers the total
     * to the killer's faction bank, then deletes those bounties.
     *
     * @return total amount paid out
     */
    public double payBountiesToKiller(UUID targetFactionId, Faction killerFaction) {
        List<Bounty> bounties = getBountiesForTarget(targetFactionId);
        if (bounties.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (Bounty bounty : bounties) {
            total += bounty.getAmount();
        }

        try {
            // Add total to killer faction
            killerFaction.setBankBalance(killerFaction.getBankBalance() + total);
            factionService.saveFaction(killerFaction);

            // Delete all bounties for this target
            bountyMapper.deleteByTarget(targetFactionId);

            LOGGER.info("Paid out " + total + " to " + killerFaction.getTag() + " for bounties on target");
            return total;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to pay bounties", e);
            return 0.0;
        }
    }

    /**
     * Refunds all bounties for a target faction back to their placers.
     * Called when a faction is disbanded. Returns money to those who placed bounties.
     *
     * @return total amount refunded
     */
    public double refundBountiesForTarget(UUID targetFactionId) {
        List<Bounty> bounties = getBountiesForTarget(targetFactionId);
        if (bounties.isEmpty()) {
            return 0.0;
        }

        double total = 0.0;
        for (Bounty bounty : bounties) {
            total += bounty.getAmount();

            // Refund to placer
            Faction placer = factionService.getFaction(bounty.getPlacerFactionId());
            if (placer != null) {
                placer.setBankBalance(placer.getBankBalance() + bounty.getAmount());
                try {
                    factionService.saveFaction(placer);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to save placer faction " + placer.getTag() + " during refund", e);
                }
            } else {
                LOGGER.warning("Placer faction not found for bounty refund: " + bounty.getPlacerFactionId());
            }
        }

        try {
            // Delete all bounties for this target
            bountyMapper.deleteByTarget(targetFactionId);
            LOGGER.info("Refunded " + total + " to placers due to target disbandment");
            return total;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete bounties for target " + targetFactionId, e);
            return 0.0;
        }
    }

    /**
     * Deletes all bounties placed by a faction.
     * Called when a faction disbands; the bounties become void.
     *
     * @return number of bounties deleted
     */
    public int deleteBountiesByPlacer(UUID placerFactionId) {
        try {
            // We don't need to refund because placer is being disbanded, funds already lost.
            int count = bountyMapper.deleteByPlacer(placerFactionId);
            LOGGER.info("Deleted " + count + " bounties placed by disbanded faction");
            return count;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete bounties by placer " + placerFactionId, e);
            return 0;
        }
    }
}
