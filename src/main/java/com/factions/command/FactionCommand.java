package com.factions.command;

import com.factions.api.Faction;
import com.factions.api.Factions;
import com.factions.api.Relation;
import com.factions.api.RelationState;
import com.factions.api.RelationType;
import com.factions.relation.RelationManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Handles the main /f command and its subcommands for relation management.
 */
public class FactionCommand implements CommandExecutor {

    private final RelationManager relationManager;

    public FactionCommand(RelationManager relationManager) {
        this.relationManager = relationManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            // TODO: show faction info
            player.sendMessage("§eUsage: /f <ally|enemy|neutral|truce|relations|accept|deny|cancel> [args]");
            return true;
        }

        String sub = args[0].toLowerCase();
        try {
            switch (sub) {
                case "ally" -> handleRelationCommand(player, args, RelationType.ALLY);
                case "enemy" -> handleRelationCommand(player, args, RelationType.ENEMY);
                case "neutral" -> handleNeutral(player, args);
                case "truce" -> handleRelationCommand(player, args, RelationType.TRUCE);
                case "relations" -> handleRelations(player, args);
                case "accept" -> handleAccept(player, args);
                case "deny" -> handleDeny(player, args);
                case "cancel" -> handleCancel(player, args);
                default -> player.sendMessage("§cUnknown subcommand. Use ally, enemy, neutral, truce, relations, accept, deny, or cancel.");
            }
        } catch (Exception e) {
            player.sendMessage("§cError: " + e.getMessage());
            Bukkit.getLogger().severe("Error handling /f command: " + e.getMessage());
        }
        return true;
    }

    private void handleRelationCommand(Player player, String[] args, RelationType type) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /f " + type.name().toLowerCase() + " <faction>");
            return;
        }

        Faction playerFaction = Factions.getFaction(player.getUniqueId());
        if (playerFaction == null) {
            player.sendMessage("§cYou must be in a faction to use this command.");
            return;
        }

        Faction targetFaction = Factions.getFactionByName(args[1]);
        if (targetFaction == null) {
            player.sendMessage("§cFaction not found: " + args[1]);
            return;
        }

        if (targetFaction.equals(playerFaction)) {
            player.sendMessage("§cYou cannot send a relation request to your own faction.");
            return;
        }

        // Check existing relation
        Relation existing = relationManager.getRelation(playerFaction, targetFaction);
        if (existing != null && existing.getState() == RelationState.ACCEPTED) {
            player.sendMessage("§eYou already have a " + existing.getRelation() + " relation with that faction.");
            return;
        }

        // If there is a pending request from the other faction to player's faction, accept it (auto-accept)
        if (existing != null && existing.getState() == RelationState.PENDING) {
            if (existing.getRequestedBy().equals(playerFaction.getId())) {
                player.sendMessage("§cYou already have a pending request to this faction.");
                return;
            } else {
                // Check if request expired
                if (isExpired(existing)) {
                    // Expired: deny it
                    relationManager.denyRelation(existing, player);
                    notifyFaction(playerFaction, "§eYour pending " + existing.getRelation() + " request to " + targetFaction.getName() + " has expired.");
                    notifyFaction(targetFaction, "§eYour pending " + existing.getRelation() + " request from " + playerFaction.getName() + " has expired.");
                    return;
                }
                // Request from them to us; accept it
                relationManager.acceptRelation(existing, player);
                notifyFaction(playerFaction, "§aYou accepted the " + existing.getRelation() + " request from " + targetFaction.getName() + ".");
                notifyFaction(targetFaction, "§a" + playerFaction.getName() + " accepted your " + existing.getRelation() + " request.");
                return;
            }
        }

        // Otherwise, create a new pending request
        relationManager.requestRelation(playerFaction, targetFaction, type, player);
        player.sendMessage("§aSent " + type.name().toLowerCase() + " request to " + targetFaction.getName() + ".");
        // Notify target faction
        notifyFaction(targetFaction, "§eReceived " + type.name().toLowerCase() + " request from " + playerFaction.getName() + ". Use /f accept or /f deny to respond.");
    }

    private void handleNeutral(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /f neutral <faction>");
            return;
        }

        Faction playerFaction = Factions.getFaction(player.getUniqueId());
        if (playerFaction == null) {
            player.sendMessage("§cYou must be in a faction to use this command.");
            return;
        }

        Faction targetFaction = Factions.getFactionByName(args[1]);
        if (targetFaction == null) {
            player.sendMessage("§cFaction not found: " + args[1]);
            return;
        }

        Relation relation = relationManager.getRelation(playerFaction, targetFaction);
        if (relation == null) {
            player.sendMessage("§eNo existing relation with " + targetFaction.getName() + ".");
            return;
        }

        // Break the relation (sets neutral effectively)
        RelationState oldState = relation.getState();
        relationManager.breakRelation(relation, player);
        player.sendMessage("§aBroke relation with " + targetFaction.getName() + ". They are now neutral.");
        if (oldState == RelationState.ACCEPTED) {
            notifyFaction(targetFaction, "§e" + playerFaction.getName() + " broke your relation. You are now neutral.");
        }
    }

    private void handleAccept(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /f accept <faction>");
            return;
        }

        Faction playerFaction = Factions.getFaction(player.getUniqueId());
        if (playerFaction == null) {
            player.sendMessage("§cYou must be in a faction to use this command.");
            return;
        }

        Faction targetFaction = Factions.getFactionByName(args[1]);
        if (targetFaction == null) {
            player.sendMessage("§cFaction not found: " + args[1]);
            return;
        }

        Relation relation = relationManager.getRelation(playerFaction, targetFaction);
        if (relation == null) {
            player.sendMessage("§eNo relation request from " + targetFaction.getName() + ".");
            return;
        }

        if (relation.getState() != RelationState.PENDING) {
            player.sendMessage("§eNo pending request from " + targetFaction.getName() + ".");
            return;
        }

        // Ensure the request was from the target faction to player's faction
        if (!relation.getRequestedBy().equals(targetFaction.getId())) {
            player.sendMessage("§cYou can only accept requests from the other faction.");
            return;
        }

        // Check expiration
        if (isExpired(relation)) {
            relationManager.denyRelation(relation, player);
            notifyFaction(playerFaction, "§ePending request from " + targetFaction.getName() + " has expired.");
            notifyFaction(targetFaction, "§eYour request to " + playerFaction.getName() + " has expired.");
            return;
        }

        relationManager.acceptRelation(relation, player);
        notifyFaction(playerFaction, "§aYou accepted the " + relation.getRelation() + " request from " + targetFaction.getName() + ".");
        notifyFaction(targetFaction, "§a" + playerFaction.getName() + " accepted your " + relation.getRelation() + " request.");
    }

    private void handleDeny(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /f deny <faction>");
            return;
        }

        Faction playerFaction = Factions.getFaction(player.getUniqueId());
        if (playerFaction == null) {
            player.sendMessage("§cYou must be in a faction to use this command.");
            return;
        }

        Faction targetFaction = Factions.getFactionByName(args[1]);
        if (targetFaction == null) {
            player.sendMessage("§cFaction not found: " + args[1]);
            return;
        }

        Relation relation = relationManager.getRelation(playerFaction, targetFaction);
        if (relation == null) {
            player.sendMessage("§eNo relation request from " + targetFaction.getName() + ".");
            return;
        }

        if (relation.getState() != RelationState.PENDING) {
            player.sendMessage("§eNo pending request from " + targetFaction.getName() + ".");
            return;
        }

        // Only the receiving faction can deny a request they received
        if (!relation.getRequestedBy().equals(targetFaction.getId())) {
            player.sendMessage("§cYou can only deny requests addressed to your faction.");
            return;
        }

        relationManager.denyRelation(relation, player);
        notifyFaction(playerFaction, "§eYou denied the " + relation.getRelation() + " request from " + targetFaction.getName() + ".");
        notifyFaction(targetFaction, "§e" + playerFaction.getName() + " denied your " + relation.getRelation() + " request.");
    }

    private void handleCancel(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /f cancel <faction>");
            return;
        }

        Faction playerFaction = Factions.getFaction(player.getUniqueId());
        if (playerFaction == null) {
            player.sendMessage("§cYou must be in a faction to use this command.");
            return;
        }

        Faction targetFaction = Factions.getFactionByName(args[1]);
        if (targetFaction == null) {
            player.sendMessage("§cFaction not found: " + args[1]);
            return;
        }

        Relation relation = relationManager.getRelation(playerFaction, targetFaction);
        if (relation == null) {
            player.sendMessage("§eNo relation request to " + targetFaction.getName() + ".");
            return;
        }

        if (relation.getState() != RelationState.PENDING) {
            player.sendMessage("§eNo pending request to " + targetFaction.getName() + ".");
            return;
        }

        // Must be the requester
        if (!relation.getRequestedBy().equals(playerFaction.getId())) {
            player.sendMessage("§cYou can only cancel your own requests.");
            return;
        }

        relationManager.cancelRequest(playerFaction, targetFaction, player);
        notifyFaction(playerFaction, "§eCancelled " + relation.getRelation() + " request to " + targetFaction.getName() + ".");
        notifyFaction(targetFaction, "§e" + playerFaction.getName() + " cancelled their " + relation.getRelation() + " request.");
    }

    private void handleRelations(Player player, String[] args) {
        Faction playerFaction = Factions.getFaction(player.getUniqueId());
        if (playerFaction == null) {
            player.sendMessage("§cYou must be in a faction to use this command.");
            return;
        }

        var relations = relationManager.getRelations(playerFaction);
        if (relations.isEmpty()) {
            player.sendMessage("§7Your faction has no relations.");
            return;
        }

        player.sendMessage("§6=== Your Faction's Relations ===");
        for (Relation r : relations) {
            Faction other = r.getOther(playerFaction);
            String color = r.getState() == RelationState.ACCEPTED ? "§a" : "§e";
            player.sendMessage(color + r.getRelation().name() + " §7with " + other.getName() + " [" + r.getState().name() + "]");
        }

        // Also list pending incoming requests separately?
        Set<Relation> pendingIncoming = relationManager.getPendingRequests(playerFaction);
        if (!pendingIncoming.isEmpty()) {
            player.sendMessage("§6=== Pending Requests (incoming) ===");
            for (Relation r : pendingIncoming) {
                Faction from = r.getOther(playerFaction); // actually the requester
                player.sendMessage("§e" + r.getRelation().name() + " from " + from.getName() + " (use /f accept/deny)");
            }
        }
    }

    private void notifyFaction(Faction faction, String message) {
        Set<UUID> members = faction.getMembers();
        for (UUID uuid : members) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(message);
            }
        }
    }

    private boolean isExpired(Relation relation) {
        // Check if relation is pending and older than expiration
        if (relation.getState() == RelationState.PENDING) {
            return System.currentTimeMillis() - relation.getTimestamp() > relationManager.getRequestExpiration();
        }
        return false;
    }
}
