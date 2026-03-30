# Factions Plugin

A full Minecraft Factions plugin built from scratch with clean architecture and proper separation of concerns.

## API for Developers

This plugin exposes a public API that other plugins can use to integrate with factions data, events, and placeholders.

### Quick Example

```java
import com.factions.api.Factions;
import com.factions.api.Faction;

// Get a player's faction
Faction faction = Factions.getFaction(player.getUniqueId());

if (faction != null) {
    String name = faction.getName();
    String tag = faction.getTag();
    double power = faction.getPower();
    int claims = faction.getClaimsCount();
}
```

### Documentation

For full API documentation, see [docs/API.md](docs/API.md).

### PlaceholderAPI

If PlaceholderAPI is installed, you can use these placeholders in chat, scoreboards, and other plugins:

- `%factions_faction%` - Player's faction name
- `%factions_faction_tag%` - Faction tag
- `%factions_faction_power%` - Faction power
- `%factions_territory%` - Territory type at player location
- `%factions_owner_faction%` - Owner faction of current chunk

### Event Listeners

Plugins can register listeners for faction events:

```java
Factions.registerListener(new FactionListener() {
    @Override
    public void onFactionCreate(Faction faction) {
        getLogger().info("Faction created: " + faction.getName());
    }
});
```

See [docs/API.md](docs/API.md) for all available events.

---

## License

MIT