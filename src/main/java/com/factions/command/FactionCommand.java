package com.factions.command;

import com.factions.FactionsPlugin;
import com.factions.api.*;
import com.factions.event.FactionDisbandEvent;
import com.factions.service.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Main command executor for /f commands.
 * Handles all faction management subcommands.
 */
public class FactionCommand implements CommandExecutor, TabExecutor {

    private static final String NO_PERMISSION = "§cYou don't have permission to use this command.";
    private static final String ONLY_PLAYERS = "§cOnly players can use this command.";
    private static final String PREFIX = "§7[§bFactions§7] §r";

    private final FactionsPlugin plugin;

    public FactionCommand(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // This handles the main command, subcommands are handled via Bukkit's command system
        // For simplicity, we'll implement subcommands manually
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        Player player = sender instanceof Player ? (Player) sender : null;

        try {
            switch (subCommand) {
                case "create":
                    handleCreate(sender, args);
                    break;
                case "disband":
                    handleDisband(sender, args);
                    break;
                case "rename":
                    handleRename(sender, args);
                    break;
                case "tag":
                    handleTag(sender, args);
                    break;
                case "desc":
                case "description":
                    handleDescription(sender, args);
                    break;
                case "motd":
                    handleMotd(sender, args);
                    break;
                case "join":
                    handleJoin(sender, args);
                    break;
                case "leave":
                case "quit":
                    handleLeave(sender, args);
                    break;
                case "invite":
                    handleInvite(sender, args);
                    break;
                case "accept":
                    handleAccept(sender, args);
                    break;
                case "deny":
                case "decline":
                    handleDeny(sender, args);
                    break;
                case "kick":
                    handleKick(sender, args);
                    break;
                case "ban":
                    handleBan(sender, args);
                    break;
                case "unban":
                    handleUnban(sender, args);
                    break;
                case "promote":
                    handlePromote(sender, args);
                    break;
                case "demote":
                    handleDemote(sender, args);
                    break;
                case "who":
                case "info":
                    handleWho(sender, args);
                    break;
                case "list":
                    handleList(sender, args);
                    break;
                case "show":
                    handleShow(sender, args);
                    break;
                case "map":
                    handleMap(sender, args);
                    break;
                case "top":
                    handleTop(sender, args);
                    break;
                case "claim":
                    handleClaim(sender, args);
                    break;
                case "unclaim":
                    handleUnclaim(sender, args);
                    break;
                case "unclaimall":
                    handleUnclaimAll(sender, args);
                    break;
                case "sethome":
                    handleSetHome(sender, args);
                    break;
                case "home":
                    handleHome(sender, args);
                    break;
                case "ally":
                    handleAlly(sender, args);
                    break;
                case "enemy":
                    handleEnemy(sender, args);
                    break;
                case "neutral":
                    handleNeutral(sender, args);
                    break;
                case "truce":
                    handleTruce(sender, args);
                    break;
                case "bounty":
                    handleBounty(sender, args);
                    break;
                default:
                    sender.sendMessage(PREFIX + "§cUnknown subcommand: " + subCommand);
                    sendHelp(sender);
                    break;
            }
        } catch (SQLException e) {
            sender.sendMessage(PREFIX + "§cDatabase error occurred.");
            plugin.getLogger().log(Level.WARNING, "Database error in command " + subCommand, e);
        } catch (Exception e) {
            sender.sendMessage(PREFIX + "§cAn error occurred.");
            plugin.getLogger().log(Level.WARNING, "Error executing command " + subCommand, e);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(PREFIX + "§7--- §bFactions Help§7 ---");
        sender.sendMessage("§e/f create <name> <tag> §7- Create a faction");
        sender.sendMessage("§e/f disband §7- Disband your faction");
        sender.sendMessage("§e/f rename <name> §7- Rename your faction");
        sender.sendMessage("§e/f tag <tag> §7- Change faction tag");
        sender.sendMessage("§e/f desc <text> §7- Set description");
        sender.sendMessage("§e/f motd <text> §7- Set message of the day");
        sender.sendMessage("§e/f invite <player> §7- Invite a player");
        sender.sendMessage("§e/f accept §7- Accept an invitation");
        sender.sendMessage("§e/f deny §7- Deny an invitation");
        sender.sendMessage("§e/f leave [confirm] §7- Leave your faction");
        sender.sendMessage("§e/f kick <player> §7- Kick a member");
        sender.sendMessage("§e/f ban <player> §7- Ban a player");
        sender.sendMessage("§e/f unban <player> §7- Unban a player");
        sender.sendMessage("§e/f promote <player> §7- Promote a member");
        sender.sendMessage("§e/f demote <player> §7- Demote a member");
        sender.sendMessage("§e/f who [player/faction] §7- View faction info");
        sender.sendMessage("§e/f list §7- List all factions");
        sender.sendMessage("§e/f claim §7- Claim current chunk");
        sender.sendMessage("§e/f unclaim §7- Unclaim current chunk");
        sender.sendMessage("§e/f sethome §7- Set faction home");
        sender.sendMessage("§e/f home §7- Teleport to faction home");
        sender.sendMessage("§e/f ally <faction> §7- Ally a faction");
        sender.sendMessage("§e/f enemy <faction> §7- Set enemy");
        sender.sendMessage("§e/f neutral <faction> §7- Set neutral");
        sender.sendMessage("§e/f map §7- Show territory map");
        sender.sendMessage("§e/f top §7- Show top factions");
    }

    private Faction getPlayerFaction(Player player) throws SQLException {
        for (Faction faction : plugin.getFactionService().getAllFactions()) {
            if (faction.hasMember(player.getUniqueId())) {
                return faction;
            }
        }
        return null;
    }

    private String formatRelation(RelationState state) {
        switch (state) {
            case ALLY: return "§a[ALLY]";
            case ENEMY: return "§c[ENEMY]";
            case NEUTRAL: return "§e[NEUTRAL]";
            case TRUCE: return "§b[TRUCE]";
            default: return "§f[UNKNOWN]";
        }
    }

    private FactionMember.Role getPlayerRole(Faction faction, Player player) throws SQLException {
        UUID playerId = player.getUniqueId();
        if (faction.getLeaderId().equals(playerId)) {
            return FactionMember.Role.LEADER;
        }
        // Query member role from database
        return plugin.getFactionService().getMemberRole(faction, playerId);
    }

    // Subcommand handlers

    private void handleCreate(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ONLY_PLAYERS);
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(PREFIX + "§e/f create <name> <tag>");
            return;
        }

        Player player = (Player) sender;
        String name = args[1];
        String tag = args[2];

        if (tag.length() > 4) {
            sender.sendMessage(PREFIX + "§cTag must be 4 characters or less.");
            return;
        }

        FactionService fs = plugin.getFactionService();
        Faction faction = fs.createFaction(name, tag.toUpperCase(), player.getUniqueId());
        sender.sendMessage(PREFIX + "§aFaction created! §7" + faction.getTag());
    }

    private void handleDisband(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ONLY_PLAYERS);
            return;
        }

        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        if (!faction.getLeaderId().equals(player.getUniqueId())) {
            sender.sendMessage(PREFIX + "§cOnly the leader can disband the faction.");
            return;
        }

        // Fire disband event for bounty refunds and cleanup
        plugin.getServer().getPluginManager().callEvent(new FactionDisbandEvent(player, faction));

        plugin.getFactionService().deleteFaction(faction.getId());
        sender.sendMessage(PREFIX + "§aFaction disbanded.");
    }

    private void handleRename(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f rename <new name>");
            return;
        }

        faction.setName(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        plugin.getFactionService().saveFaction(faction);
        sender.sendMessage(PREFIX + "§aFaction renamed to §f" + faction.getName());
    }

    private void handleTag(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f tag <new tag>");
            return;
        }

        String newTag = args[1].toUpperCase();
        if (newTag.length() > 4) {
            sender.sendMessage(PREFIX + "§cTag must be 4 characters or less.");
            return;
        }

        // Check tag availability
        Faction existing = plugin.getFactionService().getFactionByTag(newTag);
        if (existing != null && !existing.getId().equals(faction.getId())) {
            sender.sendMessage(PREFIX + "§cThat tag is already taken.");
            return;
        }

        faction.setTag(newTag);
        plugin.getFactionService().saveFaction(faction);
        sender.sendMessage(PREFIX + "§aFaction tag changed to §f" + newTag);
    }

    private void handleDescription(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f desc <description>");
            return;
        }

        String desc = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        faction.setDescription(desc);
        plugin.getFactionService().saveFaction(faction);
        sender.sendMessage(PREFIX + "§aDescription updated.");
    }

    private void handleMotd(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f motd <message>");
            return;
        }

        String motd = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        faction.setMotd(motd);
        plugin.getFactionService().saveFaction(faction);
        sender.sendMessage(PREFIX + "§aMOTD updated.");
    }

    private void handleInvite(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        FactionMember.Role role = getPlayerRole(faction, player);
        if (role == null || !role.canInvite()) {
            sender.sendMessage(PREFIX + "§cYou don't have permission to invite.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f invite <player>");
            return;
        }

        // Resolve target player by name (must be online)
        String targetName = args[1];
        Player target = plugin.getServer().getPlayerExact(targetName);
        if (target == null) {
            sender.sendMessage(PREFIX + "§cPlayer §f" + targetName + " §cis not online.");
            return;
        }

        UUID targetId = target.getUniqueId();

        // Validate invite
        if (targetId.equals(player.getUniqueId())) {
            sender.sendMessage(PREFIX + "§cYou cannot invite yourself.");
            return;
        }

        if (faction.hasMember(targetId)) {
            sender.sendMessage(PREFIX + "§cThat player is already in your faction.");
            return;
        }

        if (faction.getInvites().contains(targetId)) {
            sender.sendMessage(PREFIX + "§cThat player has already been invited.");
            return;
        }

        // Send invite
        boolean success = plugin.getFactionService().invitePlayer(faction, targetId);
        if (success) {
            sender.sendMessage(PREFIX + "§aInvite sent to §f" + target.getName());
            target.sendMessage(PREFIX + "§aYou have been invited to join §f" + faction.getTag() + "§a!");
            target.sendMessage(PREFIX + "§eType §b/f accept §eto join or §b/f deny §eto decline.");
        } else {
            sender.sendMessage(PREFIX + "§cFailed to send invite.");
        }
    }

    private void handleAccept(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = plugin.getFactionService().findFactionWithInvite(player.getUniqueId());
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou have no pending invitations.");
            return;
        }

        plugin.getFactionService().acceptInvite(faction, player.getUniqueId());
        sender.sendMessage(PREFIX + "§aYou joined §f" + faction.getTag() + "§a!");
    }

    private void handleDeny(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = plugin.getFactionService().findFactionWithInvite(player.getUniqueId());
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou have no pending invitations.");
            return;
        }

        plugin.getFactionService().denyInvite(faction, player.getUniqueId());
        sender.sendMessage(PREFIX + "§eInvitation denied.");
    }

    private void handleJoin(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;

        // Check if already in a faction
        if (getPlayerFaction(player) != null) {
            player.sendMessage(PREFIX + "§cYou are already in a faction. Leave first with §e/f leave§c.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(PREFIX + "§e/f join <faction>");
            return;
        }

        Faction target = plugin.getFactionService().getFactionByTag(args[1].toUpperCase());
        if (target == null) {
            player.sendMessage(PREFIX + "§cFaction not found.");
            return;
        }

        // Verify the player has an invite from this specific faction
        if (!target.getInvites().contains(player.getUniqueId())) {
            player.sendMessage(PREFIX + "§cYou don't have an invitation from §f" + target.getTag() + "§c.");
            return;
        }

        plugin.getFactionService().acceptInvite(target, player.getUniqueId());
        player.sendMessage(PREFIX + "§aYou joined §f" + target.getTag() + "§a!");
    }

    private void handleLeave(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        if (faction.getLeaderId().equals(player.getUniqueId())) {
            sender.sendMessage(PREFIX + "§cThe leader cannot leave. Transfer leadership or disband.");
            return;
        }

        // Require confirmation
        if (args.length == 0 || !"confirm".equalsIgnoreCase(args[0])) {
            player.sendMessage(PREFIX + "§eTo leave your faction, type §b/f leave confirm §7(irreversible)");
            return;
        }

        plugin.getFactionService().removeMember(faction, player.getUniqueId());
        player.sendMessage(PREFIX + "§aYou left §f" + faction.getTag() + "§a.");
    }

    private void handleKick(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f kick <player>");
            return;
        }

        String targetName = args[1];
        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
        if (!offlineTarget.hasPlayedBefore()) {
            sender.sendMessage(PREFIX + "§cPlayer §f" + targetName + " §cnot found.");
            return;
        }
        UUID targetId = offlineTarget.getUniqueId();

        // Cannot kick yourself or leader
        if (targetId.equals(faction.getLeaderId())) {
            sender.sendMessage(PREFIX + "§cYou cannot kick the faction leader.");
            return;
        }
        if (targetId.equals(player.getUniqueId())) {
            sender.sendMessage(PREFIX + "§cYou cannot kick yourself. Use §e/f leave§c.");
            return;
        }

        // Check target is in this faction
        if (!faction.hasMember(targetId)) {
            sender.sendMessage(PREFIX + "§cPlayer is not in your faction.");
            return;
        }

        // Permission check using role hierarchy
        FactionMember.Role actorRole = getPlayerRole(faction, player);
        FactionMember.Role targetRole = plugin.getFactionService().getMemberRole(faction, targetId);
        if (actorRole == null || targetRole == null) {
            sender.sendMessage(PREFIX + "§cError retrieving roles.");
            return;
        }
        // Role hierarchy: LEADER(0) > OFFICER(1) > MEMBER(2) > RECRUIT(3)
        // Actor can kick if their role ordinal is lower (higher rank) than target
        if (actorRole.ordinal() >= targetRole.ordinal()) {
            sender.sendMessage(PREFIX + "§cYour role is not high enough to kick this player.");
            return;
        }

        // Execute kick
        plugin.getFactionService().removeMember(faction, targetId);
        sender.sendMessage(PREFIX + "§aKicked §f" + offlineTarget.getName() + "§a from the faction.");

        // Notify target if online
        Player targetOnline = Bukkit.getPlayer(targetId);
        if (targetOnline != null && targetOnline.isOnline()) {
            targetOnline.sendMessage(PREFIX + "§cYou have been kicked from §f" + faction.getTag());
        }
    }

    private void handleBan(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f ban <player>");
            return;
        }

        String targetName = args[1];
        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
        if (!offlineTarget.hasPlayedBefore()) {
            sender.sendMessage(PREFIX + "§cPlayer §f" + targetName + " §cnot found.");
            return;
        }
        UUID targetId = offlineTarget.getUniqueId();

        // Cannot ban yourself
        if (targetId.equals(player.getUniqueId())) {
            sender.sendMessage(PREFIX + "§cYou cannot ban yourself.");
            return;
        }

        // Permission check using role hierarchy (same as kick)
        FactionMember.Role actorRole = getPlayerRole(faction, player);
        FactionMember.Role targetRole = faction.hasMember(targetId) ? plugin.getFactionService().getMemberRole(faction, targetId) : null;
        if (actorRole == null) {
            sender.sendMessage(PREFIX + "§cError retrieving your role.");
            return;
        }
        // If target is a member, check permission
        if (targetRole != null) {
            if (actorRole.ordinal() >= targetRole.ordinal()) {
                sender.sendMessage(PREFIX + "§cYour role is not high enough to ban this player.");
                return;
            }
        }

        // If target is currently in the faction, kick them first
        if (faction.hasMember(targetId)) {
            plugin.getFactionService().removeMember(faction, targetId);
        }

        // Apply ban
        plugin.getFactionService().banPlayer(faction, targetId);
        sender.sendMessage(PREFIX + "§aBanned §f" + offlineTarget.getName() + "§a from the faction.");

        // Notify target if online
        Player targetOnline = Bukkit.getPlayer(targetId);
        if (targetOnline != null && targetOnline.isOnline()) {
            targetOnline.sendMessage(PREFIX + "§cYou have been banned from §f" + faction.getTag());
        }
    }

    private void handleUnban(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f unban <player>");
            return;
        }

        String targetName = args[1];
        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
        if (!offlineTarget.hasPlayedBefore()) {
            sender.sendMessage(PREFIX + "§cPlayer §f" + targetName + " §cnot found.");
            return;
        }
        UUID targetId = offlineTarget.getUniqueId();

        // Check if actually banned
        if (!faction.getBanned().contains(targetId)) {
            sender.sendMessage(PREFIX + "§cPlayer is not banned from this faction.");
            return;
        }

        // Unban
        plugin.getFactionService().unbanPlayer(faction, targetId);
        sender.sendMessage(PREFIX + "§aUnbanned §f" + offlineTarget.getName() + "§a from the faction.");
    }

    private void handlePromote(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f promote <player>");
            return;
        }

        String targetName = args[1];
        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
        if (!offlineTarget.hasPlayedBefore()) {
            sender.sendMessage(PREFIX + "§cPlayer §f" + targetName + " §cnot found.");
            return;
        }
        UUID targetId = offlineTarget.getUniqueId();

        // Target must be a faction member
        if (!faction.hasMember(targetId)) {
            sender.sendMessage(PREFIX + "§cPlayer is not in your faction.");
            return;
        }

        // Permission: only LEADER can promote to any role, OFFICER can promote RECRUIT→MEMBER and MEMBER→OFFICER?
        // In typical hierarchy, OFFICER cannot promote to LEADER. LEADER only can promote to LEADER.
        // We'll allow promotion if actor's role is higher than target's current role after promotion target.
        FactionMember.Role actorRole = getPlayerRole(faction, player);
        FactionMember.Role targetRole = plugin.getFactionService().getMemberRole(faction, targetId);
        if (actorRole == null || targetRole == null) {
            sender.sendMessage(PREFIX + "§cError retrieving roles.");
            return;
        }

        // Determine next role
        FactionMember.Role newRole;
        switch (targetRole) {
            case RECRUIT: newRole = FactionMember.Role.MEMBER; break;
            case MEMBER: newRole = FactionMember.Role.OFFICER; break;
            case OFFICER: newRole = FactionMember.Role.LEADER; break;
            case LEADER:
            default:
                sender.sendMessage(PREFIX + "§cThat player is already at the highest rank.");
                return;
        }

        // Only LEADER can promote to LEADER. Officers can promote to OFFICER max.
        if (newRole == FactionMember.Role.LEADER && actorRole != FactionMember.Role.LEADER) {
            sender.sendMessage(PREFIX + "§cOnly the faction leader can promote to leader.");
            return;
        }
        // Officer can promote up to officer (member->officer); cannot promote officer->leader (handled above).
        // For simplicity, require actor role to be higher than target's current role.
        if (actorRole.ordinal() >= targetRole.ordinal()) {
            sender.sendMessage(PREFIX + "§cYour role is not high enough to promote this player.");
            return;
        }

        // Apply promotion
        plugin.getFactionService().setMemberRole(faction, targetId, newRole);
        sender.sendMessage(PREFIX + "§aPromoted §f" + offlineTarget.getName() + "§a to §f" + newRole + "§a.");

        // Notify target
        Player targetOnline = Bukkit.getPlayer(targetId);
        if (targetOnline != null && targetOnline.isOnline()) {
            targetOnline.sendMessage(PREFIX + "§aYou have been promoted to §f" + newRole + "§a in §f" + faction.getTag());
        }
    }

    private void handleDemote(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f demote <player>");
            return;
        }

        String targetName = args[1];
        OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(targetName);
        if (!offlineTarget.hasPlayedBefore()) {
            sender.sendMessage(PREFIX + "§cPlayer §f" + targetName + " §cnot found.");
            return;
        }
        UUID targetId = offlineTarget.getUniqueId();

        // Target must be a faction member
        if (!faction.hasMember(targetId)) {
            sender.sendMessage(PREFIX + "§cPlayer is not in your faction.");
            return;
        }

        // Permission check: actor must have higher role than target's current role
        FactionMember.Role actorRole = getPlayerRole(faction, player);
        FactionMember.Role targetRole = plugin.getFactionService().getMemberRole(faction, targetId);
        if (actorRole == null || targetRole == null) {
            sender.sendMessage(PREFIX + "§cError retrieving roles.");
            return;
        }

        // Determine new (lower) role
        FactionMember.Role newRole;
        switch (targetRole) {
            case LEADER:
                sender.sendMessage(PREFIX + "§cYou cannot demote the faction leader.");
                return;
            case OFFICER: newRole = FactionMember.Role.MEMBER; break;
            case MEMBER: newRole = FactionMember.Role.RECRUIT; break;
            case RECRUIT:
            default:
                sender.sendMessage(PREFIX + "§cThat player is already at the lowest rank.");
                return;
        }

        // Only LEADER can demote officers. Officers can demote members. Members can demote recruits.
        // Check actor role is higher (lower ordinal)
        if (actorRole.ordinal() >= targetRole.ordinal()) {
            sender.sendMessage(PREFIX + "§cYour role is not high enough to demote this player.");
            return;
        }

        // Apply demotion
        plugin.getFactionService().setMemberRole(faction, targetId, newRole);
        sender.sendMessage(PREFIX + "§aDemoted §f" + offlineTarget.getName() + "§a to §f" + newRole + "§a.");

        // Notify target
        Player targetOnline = Bukkit.getPlayer(targetId);
        if (targetOnline != null && targetOnline.isOnline()) {
            targetOnline.sendMessage(PREFIX + "§cYou have been demoted to §f" + newRole + "§c in §f" + faction.getTag());
        }
    }

    private void handleWho(CommandSender sender, String[] args) throws SQLException {
        Player player = sender instanceof Player ? (Player) sender : null;
        Faction faction = null;
        String targetName = null;

        if (args.length == 1) {
            // No target: show own faction if player
            if (player == null) {
                sender.sendMessage(PREFIX + "§cConsole must specify a player or faction.");
                return;
            }
            faction = getPlayerFaction(player);
            if (faction == null) {
                sender.sendMessage(PREFIX + "§cYou are not in a faction.");
                return;
            }
            targetName = faction.getTag();
        } else {
            targetName = args[1];
            // Try as faction tag first
            faction = plugin.getFactionService().getFactionByTag(targetName.toUpperCase());
            if (faction == null) {
                // Try as player name
                if (player != null && targetName.equalsIgnoreCase(player.getName())) {
                    faction = getPlayerFaction(player);
                } else {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetName);
                    if (offlinePlayer != null && offlinePlayer.hasPlayedBefore()) {
                        UUID playerId = offlinePlayer.getUniqueId();
                        for (Faction f : plugin.getFactionService().getAllFactions()) {
                            if (f.hasMember(playerId)) {
                                faction = f;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (faction == null) {
            sender.sendMessage(PREFIX + "§cFaction or player not found: §f" + targetName);
            return;
        }

        // Display detailed faction information
        sender.sendMessage(PREFIX + "§7--- §b" + faction.getTag() + "§7 ---");
        sender.sendMessage("§fName: §7" + faction.getName());
        UUID leaderId = faction.getLeaderId();
        String leaderName = Bukkit.getOfflinePlayer(leaderId).getName();
        sender.sendMessage("§fLeader: §7" + (leaderName != null ? leaderName : leaderId.toString()));
        sender.sendMessage("§fDescription: §7" + (faction.getDescription().isEmpty() ? "None" : faction.getDescription()));
        sender.sendMessage("§fMOTD: §7" + (faction.getMotd().isEmpty() ? "None" : faction.getMotd()));
        sender.sendMessage("§fMembers (" + faction.getMembers().size() + "):");
        for (UUID memberId : faction.getMembers()) {
            FactionMember.Role role = plugin.getFactionService().getMemberRole(faction, memberId);
            String roleStr = role != null ? role.name() : "UNKNOWN";
            OfflinePlayer offline = Bukkit.getOfflinePlayer(memberId);
            String name = offline.getName();
            if (name == null) name = memberId.toString();
            sender.sendMessage("§7 - §f" + name + " §e[" + roleStr + "]");
        }
        double power = plugin.getPowerService().recalculateFactionPower(faction);
        sender.sendMessage(String.format("§fPower: §7%.1f", power));
        sender.sendMessage(String.format("§fClaims: §7%d/%d", faction.getClaimCount(), faction.getMaxClaims()));
        if (faction.hasHome()) {
            sender.sendMessage(String.format("§fHome: §7%s %d %d %d",
                    faction.getHomeWorld(), faction.getHomeX(), faction.getHomeY(), faction.getHomeZ()));
        } else {
            sender.sendMessage("§fHome: §7Not set");
        }
        if (faction.getBankBalance() > 0) {
            sender.sendMessage(String.format("§fBank: §a$%.2f", faction.getBankBalance()));
        }
    }

    private void handleList(CommandSender sender, String[] args) throws SQLException {
        List<Faction> factions = plugin.getFactionService().getAllFactions()
                .stream()
                .sorted((a, b) -> {
                    double powerA = plugin.getPowerService().recalculateFactionPower(a);
                    double powerB = plugin.getPowerService().recalculateFactionPower(b);
                    return Double.compare(powerB, powerA);
                })
                .collect(Collectors.toList());

        sender.sendMessage(PREFIX + "§7--- §bFactions List§7 (" + factions.size() + ") ---");
        Player player = sender instanceof Player ? (Player) sender : null;
        Faction playerFaction = null;
        if (player != null) {
            playerFaction = getPlayerFaction(player);
        }
        for (int i = 0; i < factions.size(); i++) {
            Faction f = factions.get(i);
            String relationStr = "";
            if (playerFaction != null && !playerFaction.getId().equals(f.getId())) {
                RelationState rel = plugin.getRelationService().getRelation(playerFaction, f);
                relationStr = " " + formatRelation(rel);
            }
            sender.sendMessage(String.format("§f%d. %s%s §7- §e%d members §7- §a%.1f power §7- §c%d claims",
                    i + 1, f.getTag(), relationStr, f.getMembers().size(),
                    plugin.getPowerService().recalculateFactionPower(f),
                    f.getClaimCount()));
        }
    }

    private void handleShow(CommandSender sender, String[] args) throws SQLException {
        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f show <faction>");
            return;
        }

        Faction faction = plugin.getFactionService().getFactionByTag(args[1].toUpperCase());
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cFaction not found.");
            return;
        }

        sender.sendMessage(PREFIX + "§7--- §b" + faction.getTag() + "§7 ---");
        sender.sendMessage("§fName: §7" + faction.getName());
        sender.sendMessage("§fLeader: §7" + faction.getLeaderId());
        sender.sendMessage("§fDescription: §7" + faction.getDescription());
        sender.sendMessage("§fMembers: §7" + faction.getMembers().size());
        sender.sendMessage("§fPower: §7" + String.format("%.1f", plugin.getPowerService().recalculateFactionPower(faction)));
        sender.sendMessage("§fClaims: §7" + faction.getClaimCount() + "/" + faction.getMaxClaims());
    }

    private void handleMap(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) {
            sender.sendMessage(PREFIX + "§cOnly players can use this command.");
            return;
        }
        Player player = (Player) sender;
        Faction playerFaction = getPlayerFaction(player);
        if (playerFaction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        // Determine map center (player's chunk)
        int centerX = player.getLocation().getBlockX() >> 4; // chunk X
        int centerZ = player.getLocation().getBlockZ() >> 4; // chunk Z
        String world = player.getWorld().getName();

        int radius = 3; // 7x7 map centered on player
        sender.sendMessage(PREFIX + "§7--- §bFaction Map§7 (§f" + world + " §7chunk §f" + centerX + "," + centerZ + "§7) ---");

        // Build map rows from top to bottom (north to south in Minecraft Z- direction)
        for (int dz = -radius; dz <= radius; dz++) {
            StringBuilder row = new StringBuilder();
            for (int dx = -radius; dx <= radius; dx++) {
                int cx = centerX + dx;
                int cz = centerZ + dz;
                Optional<Faction> optFaction = plugin.getClaimService().getOwningFaction(world, cx, cz);
                char ch = '·';
                ChatColor color = ChatColor.GRAY;
                if (optFaction.isPresent()) {
                    Faction f = optFaction.get();
                    if (f.getId().equals(playerFaction.getId())) {
                        ch = '█';
                        color = ChatColor.AQUA;
                    } else {
                        RelationState rel = plugin.getRelationService().getRelation(playerFaction, f);
                        switch (rel) {
                            case ALLY:
                                ch = '░';
                                color = ChatColor.GREEN;
                                break;
                            case ENEMY:
                                ch = '▓';
                                color = ChatColor.RED;
                                break;
                            case TRUCE:
                                ch = '○';
                                color = ChatColor.BLUE;
                                break;
                            case NEUTRAL:
                            default:
                                ch = '▒';
                                color = ChatColor.YELLOW;
                                break;
                        }
                    }
                }
                row.append(color).append(ch);
            }
            sender.sendMessage(row.toString());
        }

        sender.sendMessage(PREFIX + "§7Legend: §b█§7=Yours §a░§7=Ally §c▓§7=Enemy §b○§7=Truce §e▒§7=Neutral §7·=Wilderness");
        sender.sendMessage(PREFIX + "§7North ↑ (top of map)");
    }

    private void handleTop(CommandSender sender, String[] args) throws SQLException {
        sender.sendMessage(PREFIX + "§7--- §bTop Factions§7 ---");
        // Similar to list but can show different metrics
        handleList(sender, args);
    }

    private void handleClaim(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ONLY_PLAYERS);
            return;
        }

        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        FactionMember.Role role = getPlayerRole(faction, player);
        if (!role.canClaim()) {
            sender.sendMessage(PREFIX + "§cYour rank cannot claim land.");
            return;
        }

        int chunkX = player.getLocation().getBlockX() >> 4;
        int chunkZ = player.getLocation().getBlockZ() >> 4;
        String world = player.getWorld().getName();

        boolean success = plugin.getClaimService().claimChunk(faction, world, chunkX, chunkZ, player.getName());
        if (success) {
            sender.sendMessage(PREFIX + "§aChunk claimed!");
        } else {
            sender.sendMessage(PREFIX + "§cFailed to claim chunk. Check power limit or adjacency.");
        }
    }

    private void handleUnclaim(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ONLY_PLAYERS);
            return;
        }

        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        int chunkX = player.getLocation().getBlockX() >> 4;
        int chunkZ = player.getLocation().getBlockZ() >> 4;
        String world = player.getWorld().getName();

        boolean success = plugin.getClaimService().unclaimChunk(faction, world, chunkX, chunkZ);
        if (success) {
            sender.sendMessage(PREFIX + "§aChunk unclaimed.");
        } else {
            sender.sendMessage(PREFIX + "§cThis chunk is not claimed by your faction.");
        }
    }

    private void handleUnclaimAll(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        plugin.getClaimService().unclaimAllForFaction(faction);
        sender.sendMessage(PREFIX + "§aAll claims cleared.");
    }

    private void handleSetHome(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        FactionMember.Role role = getPlayerRole(faction, player);
        if (!role.canSetHome()) {
            sender.sendMessage(PREFIX + "§cYour rank cannot set the home.");
            return;
        }

        faction.setHomeWorld(player.getWorld().getName());
        faction.setHomeX(player.getLocation().getBlockX());
        faction.setHomeY(player.getLocation().getBlockY());
        faction.setHomeZ(player.getLocation().getBlockZ());
        plugin.getFactionService().saveFaction(faction);
        sender.sendMessage(PREFIX + "§aFaction home set!");
    }

    private void handleHome(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        if (!faction.hasHome()) {
            sender.sendMessage(PREFIX + "§cNo home set. Use §e/f sethome§c.");
            return;
        }

        // Teleport logic would go here
        sender.sendMessage(PREFIX + "§aTeleporting to faction home...");
    }

    // Diplomacy commands

    private void handleAlly(CommandSender sender, String[] args) throws SQLException {
        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f ally <faction>");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ONLY_PLAYERS);
            return;
        }

        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        FactionMember.Role role = getPlayerRole(faction, player);
        if (!role.canSetRelation()) {
            sender.sendMessage(PREFIX + "§cYour rank cannot manage diplomacy.");
            return;
        }

        Faction target = plugin.getFactionService().getFactionByTag(args[1].toUpperCase());
        if (target == null) {
            sender.sendMessage(PREFIX + "§cFaction not found.");
            return;
        }

        if (target.getId().equals(faction.getId())) {
            sender.sendMessage(PREFIX + "§cYou cannot ally with your own faction.");
            return;
        }

        // Check if already allied
        RelationState current = plugin.getRelationService().getRelation(faction, target);
        if (current == RelationState.ALLY) {
            sender.sendMessage(PREFIX + "§eYou are already allied with §f" + target.getTag() + "§e.");
            return;
        }

        boolean success = plugin.getRelationService().setRelation(faction, target, RelationState.ALLY, player.getUniqueId());
        if (success) {
            sender.sendMessage(PREFIX + "§aYou are now allied with §f" + target.getTag() + "§a!");
        } else {
            sender.sendMessage(PREFIX + "§cFailed to set relation.");
        }
    }

    private void handleEnemy(CommandSender sender, String[] args) throws SQLException {
        // Similar to ally
        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f enemy <faction>");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ONLY_PLAYERS);
            return;
        }

        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        Faction target = plugin.getFactionService().getFactionByTag(args[1].toUpperCase());
        if (target == null) {
            sender.sendMessage(PREFIX + "§cFaction not found.");
            return;
        }

        if (target.getId().equals(faction.getId())) {
            sender.sendMessage(PREFIX + "§cYou cannot declare war on your own faction.");
            return;
        }

        RelationState current = plugin.getRelationService().getRelation(faction, target);
        if (current == RelationState.ENEMY) {
            sender.sendMessage(PREFIX + "§eYou are already at war with §f" + target.getTag() + "§e.");
            return;
        }

        boolean success = plugin.getRelationService().setRelation(faction, target, RelationState.ENEMY, player.getUniqueId());
        if (success) {
            sender.sendMessage(PREFIX + "§aYou are now at war with §f" + target.getTag() + "§a!");
        } else {
            sender.sendMessage(PREFIX + "§cFailed to set relation.");
        }
    }

    private void handleNeutral(CommandSender sender, String[] args) throws SQLException {
        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f neutral <faction>");
            return;
        }

        if (!(sender instanceof Player)) return;
        Player player = (Player) sender;
        Faction faction = getPlayerFaction(player);
        if (faction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        Faction target = plugin.getFactionService().getFactionByTag(args[1].toUpperCase());
        if (target == null) {
            sender.sendMessage(PREFIX + "§cFaction not found.");
            return;
        }

        boolean success = plugin.getRelationService().setRelation(faction, target, RelationState.NEUTRAL, player.getUniqueId());
        if (success) {
            sender.sendMessage(PREFIX + "§aSet §f" + target.getTag() + " §ato neutral.");
        } else {
            sender.sendMessage(PREFIX + "§cFailed to set relation.");
        }
    }

    private void handleTruce(CommandSender sender, String[] args) throws SQLException {
        if (args.length < 2) {
            sender.sendMessage(PREFIX + "§e/f truce <faction>");
            return;
        }
        // Similar to ally/enemy
        sender.sendMessage(PREFIX + "§eTruce feature coming soon.");
    }

    private void handleBounty(CommandSender sender, String[] args) throws SQLException {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ONLY_PLAYERS);
            return;
        }

        Player player = (Player) sender;
        Faction placerFaction = getPlayerFaction(player);
        if (placerFaction == null) {
            sender.sendMessage(PREFIX + "§cYou are not in a faction.");
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(PREFIX + "§e/f bounty <faction> <amount>");
            return;
        }

        String targetTag = args[1].toUpperCase();
        Faction targetFaction = plugin.getFactionService().getFactionByTag(targetTag);
        if (targetFaction == null) {
            sender.sendMessage(PREFIX + "§cFaction not found.");
            return;
        }

        if (targetFaction.getId().equals(placerFaction.getId())) {
            sender.sendMessage(PREFIX + "§cYou cannot place a bounty on your own faction.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(PREFIX + "§cInvalid amount: must be a number.");
            return;
        }

        if (amount <= 0) {
            sender.sendMessage(PREFIX + "§cAmount must be positive.");
            return;
        }

        boolean success = plugin.getBountyService().placeBounty(placerFaction, targetFaction, amount);
        if (success) {
            sender.sendMessage(PREFIX + "§aBounty placed! §e" + amount + " §7on §f" + targetFaction.getTag());
        } else {
            sender.sendMessage(PREFIX + "§cFailed to place bounty. Check funds and try again.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0],
                    Arrays.asList("create", "disband", "rename", "tag", "desc", "motd",
                            "invite", "accept", "deny", "join", "leave", "kick", "ban", "unban",
                            "promote", "demote", "who", "list", "show", "map", "top",
                            "claim", "unclaim", "unclaimall", "sethome", "home",
                            "ally", "enemy", "neutral", "truce", "bounty"),
                    results);
        }

        return results;
    }
}
