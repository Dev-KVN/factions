# Factions API Documentation

## Overview

The Factions API provides a clean interface for other plugins to interact with the Factions plugin. It exposes:

- Faction lookup and membership checks
- Claim and territory queries
- Event listeners for faction, claim, and economy changes
- PlaceholderAPI integration for chat, scoreboards, and other plugins

The API is versioned and maintained with backward compatibility in mind.

---

## Getting Started

### Dependency

If you're developing a plugin that depends on Factions API, add it as a dependency:

```xml
<dependency>
    <groupId>com.factions</groupId>
    <artifactId>factions-api</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

You also need the core Factions plugin runtime on the server.

### Basic Usage

```java
import com.factions.api.Factions;
import com.factions.api.Faction;
import com.factions.api.Claim;

// Get a player's faction
Faction faction = Factions.getFaction(player.getUniqueId());

// Check if player belongs to a faction
if (faction != null) {
    String name = faction.getName();
    String tag = faction.getTag();
    double power = faction.getPower();

    // Get member count
    int members = faction.getMembers().size();

    // Get role of a specific player
    String role = faction.getRole(player.getUniqueId());
}

// Get faction by name
Faction byName = Factions.getFactionByName("MyFaction");

// Get faction that owns a chunk
Chunk chunk = player.getLocation().getChunk();
Faction owner = Factions.getFactionAt(chunk);

// Check if location is claimed
boolean claimed = Factions.isClaimed(player.getLocation());
```

---

## Core Classes

### Factions (Facade)

The main entry point. All API methods are static. The facade ensures that the core plugin is loaded before delegating calls.

**Key Methods:**

- `getFaction(UUID playerId)` - Get faction a player belongs to
- `getFactionByName(String name)` - Lookup by name
- `getFactionByTag(String tag)` - Lookup by tag
- `getFactionAt(Chunk chunk)` - Get faction owning a chunk
- `isFactionMember(UUID playerId, Faction faction)` - Membership test
- `isClaimed(Location loc)` - Territory check
- `getAllFactions()` - Get all factions

**Listener Registration:**

- `registerListener(FactionListener listener)`
- `registerListener(ClaimListener listener)`
- `registerListener(EconomyListener listener)`
- `unregisterListener(...)`

**PlaceholderAPI:**

- `registerPlaceholderExpansion(PlaceholderExpansion expansion)` - Add custom placeholders

---

### Faction Interface

Represents a faction entity.

**Properties:**
- `UUID getId()`
- `String getName()`
- `String getTag()`
- `String getDescription()`
- `String getMotd()`
- `double getPower()`
- `int getMaxClaims()`
- `int getClaimsCount()`
- `Location getHome()`

**Membership:**
- `boolean isMember(UUID playerId)`
- `Set<UUID> getMembers()`
- `UUID getLeader()`
- `Set<UUID> getOfficers()`
- `String getRole(UUID playerId)`

**Diplomacy:**
- `boolean isAlliedWith(Faction other)`
- `boolean isEnemy(Faction other)`
- `String getRelation(Faction other)`

**Claims:**
- `Set<Claim> getClaims()`

**Economy:**
- `double getBankBalance()`

---

### Claim Interface

Represents a land claim (chunk).

**Location:**
- `String getWorld()`
- `int getChunkX()`
- `int getChunkZ()`
- `Chunk getChunk()`

**Ownership:**
- `Faction getFaction()`
- `long getCreatedAt()`

**Territory Type:**
- `TerritoryType getType()` (enum: WILDERNESS, SAFEZONE, WARZONE, CLAIM)
- `boolean isBuffer()`

**Access Checks:**
- `boolean canBuild(Player player)`
- `boolean canInteract(Player player)`
- `boolean isPvPEnabled()`

---

## Event Listeners

### FactionListener

Handle faction lifecycle and membership changes.

```java
Factions.registerListener(new FactionListener() {
    @Override
    public void onFactionCreate(Faction faction) {
        getLogger().info("New faction: " + faction.getName());
    }

    @Override
    public void onPlayerJoin(Faction faction, UUID playerId) {
        // handle join
    }

    // Override only the events you need
});
```

Available callbacks: `onFactionCreate`, `onFactionDisband`, `onFactionRename`, `onPlayerJoin`, `onPlayerLeave`, `onPlayerInvite`, `onPlayerInviteAccept`, `onPlayerInviteDeny`, `onPlayerKick`, `onPlayerBan`, `onPlayerUnban`, `onRoleChange`, `onDescriptionChange`, `onTagChange`, `onMotdChange`, `onHomeSet`, `onBankOpen`, `onBankDeposit`, `onBankWithdraw`.

### ClaimListener

Handle claim events.

```java
Factions.registerListener(new ClaimListener() {
    @Override
    public void onClaimCreated(Faction faction, Claim claim, Player claimedBy) {
        // handle claim
    }

    @Override
    public void onOverclaim(Faction attacker, Faction defender, Claim claim, Player overclaimedBy) {
        // handle raid
    }
});
```

Available callbacks: `onClaimCreated`, `onClaimRemoved`, `onOverclaim`, `onTerritoryTypeChange`, `onBorderVisualize`, `onBuildDenied`, `onClaimLimitReached`.

### EconomyListener

Track economy transactions.

```java
Factions.registerListener(new EconomyListener() {
    @Override
    public void onDeposit(Faction faction, Player player, BigDecimal amount) {
        getLogger().info("Deposit: " + amount);
    }
});
```

Available: `onDeposit`, `onWithdraw`, `onBalanceChange`, `onDebtStatusChange`, `onClaimPayment`.

---

## PlaceholderAPI

Placeholders are automatically registered when PlaceholderAPI is available.

| Placeholder | Description |
|-------------|-------------|
| `%factions_faction%` | Player's faction name or "No Faction" |
| `%factions_faction_tag%` | Player's faction tag |
| `%factions_faction_power%` | Faction power total |
| `%factions_faction_max_claims%` | Maximum claim count |
| `%factions_faction_claims%` | Current claim count |
| `%factions_faction_members%` | Member count |
| `%factions_faction_balance%` | Bank balance formatted as currency |
| `%factions_territory%` | Territory type (SAFEZONE, WARZONE, faction name, or WILDERNESS) |
| `%factions_owner_faction%` | Faction that owns current chunk |
| `%factions_role%` | Player's role in faction |

Use in chat plugins, scoreboards, or any PlaceholderAPI-enabled context:

```
[%factions_faction%] %player_name%
```

---

## Best Practices

1. **Check availability**: Before using the API, verify it's loaded:
   ```java
   if (Factions.isAvailable()) { ... }
   ```

2. **Thread safety**: API calls should be made from the main server thread. If you're on an async thread, use BukkitScheduler to run on main thread.

3. **Handle nulls**: Many methods return `null` when data doesn't exist (no faction, wilderness, etc.). Always null-check.

4. **Metadata is read-only**: The Faction and Claim interfaces provide read-only access. Modifications must go through the core plugin's commands or API-specific write methods (if exposed).

5. **Unregister listeners**: When your plugin disables, unregister any listeners you added to avoid memory leaks:
   ```java
   Factions.unregisterListener(myListener);
   ```

---

## Version History

- **1.0.0** - Initial release
  - Full faction lookup API
  - Claim territory queries
  - Faction, Claim, and Economy listeners
  - PlaceholderAPI integration
  - Read-only access to faction properties

---

*For more information, see the specification at {@code FACTIONS_SPEC.md}.*
