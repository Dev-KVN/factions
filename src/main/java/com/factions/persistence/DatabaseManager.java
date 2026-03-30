package com.factions.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database manager using HikariCP for connection pooling.
 * Supports both MySQL and SQLite.
 */
public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    private HikariDataSource dataSource;
    private final String type; // "mysql" or "sqlite"
    private final String database;
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    public enum DatabaseType {
        MYSQL,
        SQLITE
    }

    /**
     * Constructs a DatabaseManager with configuration.
     */
    public DatabaseManager(DatabaseType type, String host, int port, String database,
                          String username, String password) {
        this.type = type == DatabaseType.MYSQL ? "mysql" : "sqlite";
        this.database = database;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * Constructs a SQLite DatabaseManager with a file path.
     */
    public DatabaseManager(String filePath) {
        this.type = "sqlite";
        this.database = filePath;
        this.host = null;
        this.port = 0;
        this.username = null;
        this.password = null;
    }

    /**
     * Initializes the connection pool.
     */
    public void initialize() throws SQLException {
        HikariConfig config = new HikariConfig();

        if (type.equals("mysql")) {
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&characterEncoding=utf8",
                    host, port, database));
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        } else {
            config.setJdbcUrl(String.format("jdbc:sqlite:%s", database));
            config.setDriverClassName("org.sqlite.JDBC");
        }

        // Pool configuration
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        // Enable auto-commit for simplicity (can be overridden)
        config.setAutoCommit(true);

        dataSource = new HikariDataSource(config);
        LOGGER.info("Database connection pool initialized for " + type);
    }

    /**
     * Gets a connection from the pool.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Closes the connection pool.
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("Database connection pool closed");
        }
    }

    /**
     * Runs the schema initialization script.
     */
    public void initializeSchema() {
        String schema = getSchemaSql();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // SQLite needs foreign keys enabled
            if (type.equals("sqlite")) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }

            stmt.execute(schema);
            LOGGER.info("Database schema initialized successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize schema", e);
            throw new RuntimeException("Schema initialization failed", e);
        }
    }

    /**
     * Returns the SQL schema for all tables.
     */
    private String getSchemaSql() {
        if (type.equals("mysql")) {
            return getMySQLSchema();
        } else {
            return getSQLiteSchema();
        }
    }

    private String getMySQLSchema() {
        return ""
                + "CREATE TABLE IF NOT EXISTS factions (\n"
                + "  id VARCHAR(36) PRIMARY KEY,\n"
                + "  name VARCHAR(100) NOT NULL,\n"
                + "  tag VARCHAR(10) NOT NULL,\n"
                + "  description TEXT,\n"
                + "  motd TEXT,\n"
                + "  leader_id VARCHAR(36) NOT NULL,\n"
                + "  bank_balance DOUBLE DEFAULT 0.0,\n"
                + "  created_at BIGINT NOT NULL,\n"
                + "  last_seen BIGINT NOT NULL,\n"
                + "  power DOUBLE DEFAULT 0.0,\n"
                + "  max_claims INT DEFAULT 0,\n"
                + "  claim_count INT DEFAULT 0,\n"
                + "  home_world VARCHAR(100),\n"
                + "  home_x INT DEFAULT 0,\n"
                + "  home_y INT DEFAULT 0,\n"
                + "  home_z INT DEFAULT 0,\n"
                + "  peaceful BOOLEAN DEFAULT FALSE,\n"
                + "  permanent BOOLEAN DEFAULT FALSE,\n"
                + "  INDEX idx_tag (tag),\n"
                + "  INDEX idx_leader (leader_id)\n"
                + ");\n"
                + "\n"
                + "CREATE TABLE IF NOT EXISTS faction_members (\n"
                + "  faction_id VARCHAR(36) NOT NULL,\n"
                + "  player_id VARCHAR(36) NOT NULL,\n"
                + "  role VARCHAR(20) NOT NULL,\n"
                + "  joined_at VARCHAR(50) NOT NULL,\n"
                + "  last_online VARCHAR(50) NOT NULL,\n"
                + "  last_ip VARCHAR(45),\n"
                + "  contributed_power DOUBLE DEFAULT 0.0,\n"
                + "  banned BOOLEAN DEFAULT FALSE,\n"
                + "  PRIMARY KEY (faction_id, player_id),\n"
                + "  FOREIGN KEY (faction_id) REFERENCES factions(id) ON DELETE CASCADE,\n"
                + "  INDEX idx_player_id (player_id)\n"
                + ");\n"
                + "\n"
                + "CREATE TABLE IF NOT EXISTS faction_invites (\n"
                + "  faction_id VARCHAR(36) NOT NULL,\n"
                + "  player_id VARCHAR(36) NOT NULL,\n"
                + "  invited_at BIGINT NOT NULL,\n"
                + "  PRIMARY KEY (faction_id, player_id),\n"
                + "  FOREIGN KEY (faction_id) REFERENCES factions(id) ON DELETE CASCADE\n"
                + ");\n"
                + "\n"
                + "CREATE TABLE IF NOT EXISTS claims (\n"
                + "  id VARCHAR(100) PRIMARY KEY,\n"
                + "  faction_id VARCHAR(36) NOT NULL,\n"
                + "  world VARCHAR(100) NOT NULL,\n"
                + "  chunk_x INT NOT NULL,\n"
                + "  chunk_z INT NOT NULL,\n"
                + "  claimed_at BIGINT NOT NULL,\n"
                + "  claimed_by VARCHAR(36),\n"
                + "  FOREIGN KEY (faction_id) REFERENCES factions(id) ON DELETE CASCADE,\n"
                + "  UNIQUE KEY unique_claim (world, chunk_x, chunk_z),\n"
                + "  INDEX idx_faction_claims (faction_id)\n"
                + ");\n"
                + "\n"
                + "CREATE TABLE IF NOT EXISTS relations (\n"
                + "  faction_a VARCHAR(36) NOT NULL,\n"
                + "  faction_b VARCHAR(36) NOT NULL,\n"
                + "  type VARCHAR(20) NOT NULL,\n"
                + "  established_at VARCHAR(50) NOT NULL,\n"
                + "  expires_at VARCHAR(50),\n"
                + "  established_by VARCHAR(36),\n"
                + "  PRIMARY KEY (faction_a, faction_b),\n"
                + "  FOREIGN KEY (faction_a) REFERENCES factions(id) ON DELETE CASCADE,\n"
                + "  FOREIGN KEY (faction_b) REFERENCES factions(id) ON DELETE CASCADE\n"
                + ");\n"
                + "\n"
                + "CREATE TABLE IF NOT EXISTS banned_players (\n"
                + "  faction_id VARCHAR(36) NOT NULL,\n"
                + "  player_id VARCHAR(36) NOT NULL,\n"
                + "  banned_at BIGINT NOT NULL,\n"
                + "  PRIMARY KEY (faction_id, player_id),\n"
                + "  FOREIGN KEY (faction_id) REFERENCES factions(id) ON DELETE CASCADE\n"
                + ");\n"
                + "\n"
                + "CREATE TABLE IF NOT EXISTS player_power (\n"
                + "  player_id VARCHAR(36) PRIMARY KEY,\n"
                + "  power DOUBLE DEFAULT 0.0,\n"
                + "  max_power DOUBLE DEFAULT 1000.0,\n"
                + "  last_update BIGINT NOT NULL,\n"
                + "  last_death BIGINT DEFAULT 0,\n"
                + "  INDEX idx_power (power DESC)\n"
                + ");";
    }

    private String getSQLiteSchema() {
        return ""
                + "CREATE TABLE IF NOT EXISTS factions (\n"
                + "  id TEXT PRIMARY KEY,\n"
                + "  name TEXT NOT NULL,\n"
                + "  tag TEXT NOT NULL,\n"
                + "  description TEXT,\n"
                + "  motd TEXT,\n"
                + "  leader_id TEXT NOT NULL,\n"
                + "  bank_balance REAL DEFAULT 0.0,\n"
                + "  created_at INTEGER NOT NULL,\n"
                + "  last_seen INTEGER NOT NULL,\n"
                + "  power REAL DEFAULT 0.0,\n"
                + "  max_claims INTEGER DEFAULT 0,\n"
                + "  claim_count INTEGER DEFAULT 0,\n"
                + "  home_world TEXT,\n"
                + "  home_x INTEGER DEFAULT 0,\n"
                + "  home_y INTEGER DEFAULT 0,\n"
                + "  home_z INTEGER DEFAULT 0,\n"
                + "  peaceful INTEGER DEFAULT 0,\n"
                +  "permanent INTEGER DEFAULT 0\n"
                + ");\n"
                + "\n"
                + "CREATE TABLE IF NOT EXISTS faction_members (\n"
                + "  faction_id TEXT NOT NULL,\n"
                + "  player_id TEXT NOT NULL,\n"
                + "  role TEXT NOT NULL,\n"
                + "  joined_at TEXT NOT NULL,\n"
                + "  last_online TEXT NOT NULL,\n"
                + "  last_ip TEXT,\n"
                + "  contributed_power REAL DEFAULT 0.0,\n"
                + "  banned INTEGER DEFAULT 0,\n"
                + "  PRIMARY KEY (faction_id, player_id),\n"
                + "  FOREIGN KEY (faction_id) REFERENCES factions(id) ON DELETE CASCADE\n"
                + ");\n"
                + "\n"
                + "CREATE TABLE IF NOT EXISTS faction_invites (\n"
                + "  faction_id TEXT NOT NULL,\n"
                + "  player_id TEXT NOT NULL,\n"
                + "  invited_at INTEGER NOT NULL,\n"
                + "  PRIMARY KEY (faction_id, player_id),\n"
                + "  FOREIGN KEY (faction_id) REFERENCES factions(id) ON DELETE CASCADE\n"
                + ");\n"
                + "\n"
                + "CREATE TABLE IF NOT EXISTS claims (\n"
                + "  id TEXT PRIMARY KEY,\n"
                + "  faction_id TEXT NOT NULL,\n"
                + "  world TEXT NOT NULL,\n"
                + "  chunk_x INTEGER NOT NULL,\n"
                + "  chunk_z INTEGER NOT NULL,\n"
                + "  claimed_at INTEGER NOT NULL,\n"
                + "  claimed_by TEXT,\n"
                + "  FOREIGN KEY (faction_id) REFERENCES factions(id) ON DELETE CASCADE,\n"
                + "  UNIQUE(world, chunk_x, chunk_z)\n"
                + ");\n"
                + "\n"
                + "CREATE TABLE IF NOT EXISTS relations (\n"
                + "  faction_a TEXT NOT NULL,\n"
                + "  faction_b TEXT NOT NULL,\n"
                + "  type TEXT NOT NULL,\n"
                + "  established_at TEXT NOT NULL,\n"
                + "  expires_at TEXT,\n"
                + "  established_by TEXT,\n"
                + "  PRIMARY KEY (faction_a, faction_b)\n"
                + ");\n"
                + "\n"
                + "CREATE TABLE IF NOT EXISTS banned_players (\n"
                + "  faction_id TEXT NOT NULL,\n"
                + "  player_id TEXT NOT NULL,\n"
                + "  banned_at INTEGER NOT NULL,\n"
                + "  PRIMARY KEY (faction_id, player_id),\n"
                + "  FOREIGN KEY (faction_id) REFERENCES factions(id) ON DELETE CASCADE\n"
                + ");\n"
                + "\n"
                + "CREATE TABLE IF NOT EXISTS player_power (\n"
                + "  player_id TEXT PRIMARY KEY,\n"
                + "  power REAL DEFAULT 0.0,\n"
                + "  max_power REAL DEFAULT 1000.0,\n"
                + "  last_update INTEGER NOT NULL,\n"
                + "  last_death INTEGER DEFAULT 0\n"
                + ");";
    }
}
