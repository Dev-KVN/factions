package com.factions.event;

import com.factions.api.Faction;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Called when a faction claims or unclaims a chunk.
 */
public class ClaimEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Faction faction;
    private final Chunk chunk;
    private final boolean isClaim; // true = claim, false = unclaim

    public ClaimEvent(Player who, Faction faction, Chunk chunk, boolean isClaim) {
        super(who);
        this.faction = faction;
        this.chunk = chunk;
        this.isClaim = isClaim;
    }

    public Faction getFaction() {
        return faction;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public boolean isClaim() {
        return isClaim;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
