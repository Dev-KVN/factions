package com.factions.listener;

import com.factions.api.Faction;
import com.factions.api.Factions;
import com.factions.api.RelationState;
import com.factions.api.RelationType;
import com.factions.relation.RelationManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.JavaPlugin;

import java.util.Set;

/**
 * Formats chat messages with a prefix indicating the relation between the sender and each recipient.
 *
 * <p>For each recipient, the message is prefixed with [ALLY], [ENEMY], [TRUCE] based on the
 * diplomatic relation between the sender's faction and the recipient's faction, if any.</p>
 *
 * <p>Requires that both sender and recipient are in factions. If either is not in a faction,
 * no prefix is added.</p>
 */
public class ChatRelationListener implements Listener {

    private final RelationManager relationManager;
    private final JavaPlugin plugin;

    public ChatRelationListener(JavaPlugin plugin, RelationManager relationManager) {
        this.plugin = plugin;
        this.relationManager = relationManager;
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player sender = event.getPlayer();
        Faction senderFaction = Factions.getFaction(sender.getUniqueId());
        if (senderFaction == null) {
            return; // Sender has no faction; default formatting ok
        }

        Set<Player> recipients = event.getRecipients();
        String originalFormat = event.getFormat(); // e.g., "%s: %s"
        String message = event.getMessage();

        // Cancel default broadcast; we'll send manually
        event.setCancelled(true);

        for (Player recipient : recipients) {
            String formattedMessage;
            if (recipient.equals(sender)) {
                // Self: just default
                formattedMessage = String.format(originalFormat, sender.getDisplayName(), message);
            } else {
                Faction recipientFaction = Factions.getFaction(recipient.getUniqueId());
                String prefix = "";
                if (recipientFaction != null) {
                    var relation = relationManager.getRelation(senderFaction, recipientFaction);
                    if (relation != null && relation.getState() == RelationState.ACCEPTED) {
                        RelationType type = relation.getRelation();
                        if (type == RelationType.ALLY) {
                            prefix = "§a[ALLY] ";
                        } else if (type == RelationType.ENEMY) {
                            prefix = "§c[ENEMY] ";
                        } else if (type == RelationType.TRUCE) {
                            prefix = "§e[TRUCE] ";
                        }
                        // NEUTRAL no prefix
                    }
                }
                formattedMessage = String.format(originalFormat, sender.getDisplayName(), prefix + message);
            }

            // Send message on main thread
            Bukkit.getScheduler().runTask(plugin, () -> recipient.sendMessage(formattedMessage));
        }
    }
}
