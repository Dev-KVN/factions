package com.factions;

import com.factions.api.Factions;
import com.factions.command.FactionCommand;
import com.factions.listener.FriendlyFireListener;
import com.factions.listener.ChatRelationListener;
import com.factions.relation.RelationManager;
import com.factions.relation.RelationManagerImpl;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main plugin class for Factions.
 */
public final class FactionsPlugin extends JavaPlugin {

    private static FactionsPlugin instance;
    private RelationManager relationManager;

    @Override
    public void onEnable() {
        instance = this;
        this.relationManager = new RelationManagerImpl(this);

        // Register the main faction command
        getCommand("f").setExecutor(new FactionCommand(relationManager));

        // Register event listeners
        getServer().getPluginManager().registerEvents(new FriendlyFireListener(relationManager), this);
        getServer().getPluginManager().registerEvents(new ChatRelationListener(this, relationManager), this);

        // Set the core implementation for the API
        Factions.setCore(new FactionsCoreImpl(relationManager));

        getLogger().info("Factions plugin enabled");
    }

    @Override
    public void onDisable() {
        Factions.clearCore();
        getLogger().info("Factions plugin disabled");
    }

    public static FactionsPlugin getInstance() {
        return instance;
    }

    public RelationManager getRelationManager() {
        return relationManager;
    }
}
