package com.factions;

import com.factions.api.FactionsAPI;
import com.factions.config.PowerConfiguration;
import com.factions.listeners.BountyListener;
import com.factions.listeners.PowerLossListener;
import com.factions.persistence.DatabaseManager;
import com.factions.service.BountyService;
import com.factions.service.ClaimService;
import com.factions.service.FactionService;
import com.factions.service.PowerService;
import com.factions.service.RelationService;
import com.factions.task.PowerTask;
import com.factions.command.FactionCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main Factions Plugin class.
 */
public class FactionsPlugin extends JavaPlugin {

    private static final Logger LOGGER = Logger.getLogger(FactionsPlugin.class.getName());

    private DatabaseManager databaseManager;
    private FactionService factionService;
    private PowerService powerService;
    private ClaimService claimService;
    private RelationService relationService;
    private BountyService bountyService;
    private PowerTask powerTask;

    @Override
    public void onEnable() {
        getLogger().info("Enabling FactionsPlugin...");

        // Create config file if it doesn't exist
        saveDefaultConfig();

        // Initialize database
        try {
            initializeDatabase();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize services
        initializeServices();

        // Register commands
        getCommand("f").setExecutor(new FactionCommand(this));

        // Register events
        getServer().getPluginManager().registerEvents(new com.factions.listeners.PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new com.factions.listeners.ChunkListener(this), this);
        getServer().getPluginManager().registerEvents(new BountyListener(this), this);

        // Initialize API
        FactionsAPI.setPlugin(this);

        getLogger().info("FactionsPlugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling FactionsPlugin...");

        if (powerTask != null) {
            powerTask.stop();
        }

        if (powerService != null) {
            powerService.shutdown();
        }

        if (databaseManager != null) {
            databaseManager.shutdown();
        }

        getLogger().info("FactionsPlugin disabled.");
    }

    /**
     * Initializes the database connection and schema.
     */
    private void initializeDatabase() throws SQLException {
        String dbType = getConfig().getString("database.type", "sqlite").toLowerCase();
        databaseManager = new DatabaseManager(
                dbType.equals("mysql") ? DatabaseManager.DatabaseType.MYSQL : DatabaseManager.DatabaseType.SQLITE,
                getConfig().getString("database.host", "localhost"),
                getConfig().getInt("database.port", 3306),
                getConfig().getString("database.name", "factions"),
                getConfig().getString("database.username", "factions"),
                getConfig().getString("database.password", "")
        );

        File dataFolder = getDataFolder();
        if (dbType.equals("sqlite")) {
            File dbFile = new File(dataFolder, "factions.db");
            databaseManager = new DatabaseManager(dbFile.getAbsolutePath());
        }

        databaseManager.initialize();
        databaseManager.initializeSchema();
    }

    /**
     * Initializes all services.
     */
    private void initializeServices() {
        // Build power configuration from plugin config
        PowerConfiguration powerConfig = buildPowerConfiguration();

        // Initialize power service with configuration
        powerService = new PowerService(databaseManager, powerConfig);
        factionService = new FactionService(databaseManager, powerService);
        relationService = new RelationService(databaseManager);
        claimService = new ClaimService(databaseManager, powerService, factionService);
        bountyService = new BountyService(databaseManager, factionService);

        // Register power loss listener
        getServer().getPluginManager().registerEvents(new PowerLossListener(this), this);

        // Start periodic power update task (every 5 minutes)
        powerTask = new PowerTask(powerService, this, 5);
        powerTask.start();
    }

    /**
     * Builds PowerConfiguration from the plugin's config.yml.
     */
    private PowerConfiguration buildPowerConfiguration() {
        double maxPower = getConfig().getDouble("power.max-power", 1000.0);
        double gainRate = getConfig().getDouble("power.regen-rate", 1.0);
        double deathPenaltyPercent = getConfig().getDouble("power.death-penalty-percent", 10.0);
        double offlineDecayRate = getConfig().getDouble("power.offline-decay-rate", 0.5);
        double minClaimsPerPower = getConfig().getDouble("power.min-claims-per-power", 0.01);

        // Parse offline mode
        String offlineModeStr = getConfig().getString("power.offline-mode", "decay").toLowerCase();
        PowerConfiguration.OfflineMode offlineMode = PowerConfiguration.OfflineMode.DECAY;
        try {
            offlineMode = PowerConfiguration.OfflineMode.valueOf(offlineModeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid offline-mode in config: " + offlineModeStr + ", defaulting to DECAY");
        }

        // The spec mentions minutesPerPoint and incrementPerDay, but config doesn't have them.
        // We'll use gainRate as power per minute and daysPlayed not implemented yet.
        // These can be added later if needed.
        double minutesPerPoint = 1.0 / gainRate; // 1 point per minute if gainRate=1
        double incrementPerDay = 0; // Not used in current design

        return new PowerConfiguration(
            maxPower,
            gainRate,
            deathPenaltyPercent,
            offlineMode,
            offlineDecayRate,
            minutesPerPoint,
            incrementPerDay,
            minClaimsPerPower
        );
    }

    // Getters for services
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public FactionService getFactionService() {
        return factionService;
    }

    public PowerService getPowerService() {
        return powerService;
    }

    public ClaimService getClaimService() {
        return claimService;
    }

    public RelationService getRelationService() {
        return relationService;
    }

    public BountyService getBountyService() {
        return bountyService;
    }
}
