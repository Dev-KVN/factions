package com.factions.event;

import com.factions.api.Faction;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Called when a player leaves a faction.
 */
public class FactionLeaveEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Faction faction;
    private final boolean wasKicked;

    public FactionLeaveEvent(Player who, Faction faction, boolean wasKicked) {
        super(who);
        this.faction = faction;
        this.wasKicked = wasKicked;
    }

    public Faction getFaction() {
        return faction;
    }

    public boolean wasKicked() {
        return wasKicked;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
