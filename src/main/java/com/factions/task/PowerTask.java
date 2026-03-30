package com.factions.task;

import com.factions.service.PowerService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Scheduled task that processes power updates for online players.
 * Runs at a fixed interval (default 5 minutes) to increment power based on time played.
 */
public class PowerTask implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(PowerTask.class.getName());

    private final PowerService powerService;
    private final Plugin plugin;
    private final long intervalTicks; // Bukkit ticks (20 ticks = 1 second)
    private BukkitTask bukkitTask;

    /**
     * @param powerService the power service to update
     * @param plugin the plugin instance for scheduling
     * @param intervalMinutes how often to run (in minutes)
     */
    public PowerTask(PowerService powerService, Plugin plugin, long intervalMinutes) {
        this.powerService = powerService;
        this.plugin = plugin;
        this.intervalTicks = intervalMinutes * 60 * 20; // minutes -> seconds -> ticks
    }

    /**
     * Starts the repeating task.
     */
    public void start() {
        bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, this, intervalTicks, intervalTicks);
        LOGGER.info("PowerTask scheduled to run every " + intervalTicks / 20 / 60 + " minutes");
    }

    /**
     * Stops the repeating task.
     */
    public void stop() {
        if (bukkitTask != null && !bukkitTask.isCancelled()) {
            bukkitTask.cancel();
            LOGGER.info("PowerTask stopped");
        }
    }

    @Override
    public void run() {
        try {
            powerService.updateOnlinePower();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error updating online power", e);
        }
    }
}
