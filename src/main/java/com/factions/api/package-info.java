/**
 * Factions Public API.
 *
 * <p>This package provides public interfaces and utilities for integrating with
 * the Factions plugin. Other plugins can use these APIs to retrieve faction
 * data, listen to events, and add custom placeholders.</p>
 *
 * <h2>Getting Started</h2>
 * <p>The main entry point is the {@link Factions} class, which provides static
 * methods to access faction data. For example:</p>
 *
 * <pre>{@code
 * Faction faction = Factions.getFaction(player.getUniqueId());
 * if (faction != null) {
 *     String name = faction.getName();
 *     // ...
 * }
 * }</pre>
 *
 * <h2>Event Hooks</h2>
 * <p>To listen for faction-related events, implement one of the listener
 * interfaces in the {@code extension} package and register it via
 * {@link Factions#registerListener(FactionListener)}.</p>
 *
 * <h2>PlaceholderAPI</h2>
 * <p>The plugin automatically registers placeholders when PlaceholderAPI is
 * present. Developers can also add custom expansions by implementing
 * {@link me.clip.placeholderapi.expansion.PlaceholderExpansion} and registering
 * through the API.</p>
 *
 * @since 1.0.0
 * @author Factions Team
 */
package com.factions.api;
