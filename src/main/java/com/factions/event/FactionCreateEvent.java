package com.factions.event;

import com.factions.api.Faction;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Called when a faction is created.
 */
public class FactionCreateEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Faction faction;

    public FactionCreateEvent(Player who, Faction faction) {
        super(who);
        this.faction = faction;
    }

    public Faction getFaction() {
        return faction;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
