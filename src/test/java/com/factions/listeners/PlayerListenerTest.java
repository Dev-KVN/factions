package com.factions.listeners;

import com.factions.FactionsPlugin;
import com.factions.api.Faction;
import com.factions.service.FactionService;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlayerListener chat prefix behavior.
 * Verifies that faction chat messages are formatted with the correct [TAG] prefix.
 */
public class PlayerListenerTest {

    private UUID playerUuid = UUID.randomUUID();
    private String factionTag = "FA";

    /**
     * Test that when a player in a faction with tag "FA" sends a chat message,
     * the event format is set to "[FA] PlayerName: message".
     * This verifies the faction chat prefix functionality.
     */
    @Test
    public void testFactionChatPrefix_WhenPlayerInFaction_FormatsWithTag() {
        // Arrange: create mocks
        FactionsPlugin mockPlugin = mock(FactionsPlugin.class);
        FactionService mockFactionService = mock(FactionService.class);
        Faction mockFaction = mock(Faction.class);
        Player mockPlayer = mock(Player.class);
        AsyncPlayerChatEvent mockEvent = mock(AsyncPlayerChatEvent.class);
        ArgumentCaptor<String> formatCaptor = ArgumentCaptor.forClass(String.class);

        when(mockPlugin.getFactionService()).thenReturn(mockFactionService);
        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockPlayer.getName()).thenReturn("TestPlayer");
        when(mockEvent.getPlayer()).thenReturn(mockPlayer);
        when(mockFaction.getTag()).thenReturn(factionTag);
        when(mockFaction.hasMember(playerUuid)).thenReturn(true);
        when(mockFactionService.getAllFactions()).thenReturn(Collections.singletonList(mockFaction));

        PlayerListener listener = new PlayerListener(mockPlugin);

        String message = "Hello faction!";

        // Act
        listener.onChat(mockEvent);

        // Assert: verify that setFormat was called with the expected prefix
        verify(mockEvent).setFormat(formatCaptor.capture());
        String appliedFormat = formatCaptor.getValue();

        // The format string should be: "[FA] %1$s: %2$s"
        // where %1$s = player name, %2$s = message
        assertEquals("[FA] %1$s: %2$s", appliedFormat,
                "Chat format should include the faction tag as [TAG] prefix");
    }

    /**
     * Test that when a player NOT in a faction sends a chat message,
     * the event format is NOT modified by the listener.
     */
    @Test
    public void testFactionChatPrefix_WhenPlayerNotInFaction_DoesNotModifyFormat() {
        // Arrange: create mocks
        FactionsPlugin mockPlugin = mock(FactionsPlugin.class);
        FactionService mockFactionService = mock(FactionService.class);
        Player mockPlayer = mock(Player.class);
        AsyncPlayerChatEvent mockEvent = mock(AsyncPlayerChatEvent.class);

        when(mockPlugin.getFactionService()).thenReturn(mockFactionService);
        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockEvent.getPlayer()).thenReturn(mockPlayer);
        when(mockFactionService.getAllFactions()).thenReturn(Collections.emptyList());

        PlayerListener listener = new PlayerListener(mockPlugin);

        // Act
        listener.onChat(mockEvent);

        // Assert: setFormat should never be called
        verify(mockEvent, never()).setFormat(anyString());
    }

    /**
     * Test that the prefix correctly handles different faction tags.
     * This ensures the prefix logic dynamically uses the faction's tag.
     */
    @Test
    public void testFactionChatPrefix_WithDifferentTag_UsesCorrectTag() {
        // Arrange: faction with tag "XYZ"
        String otherTag = "XYZ";

        FactionsPlugin mockPlugin = mock(FactionsPlugin.class);
        FactionService mockFactionService = mock(FactionService.class);
        Faction mockFaction = mock(Faction.class);
        Player mockPlayer = mock(Player.class);
        AsyncPlayerChatEvent mockEvent = mock(AsyncPlayerChatEvent.class);
        ArgumentCaptor<String> formatCaptor = ArgumentCaptor.forClass(String.class);

        when(mockPlugin.getFactionService()).thenReturn(mockFactionService);
        when(mockPlayer.getUniqueId()).thenReturn(playerUuid);
        when(mockEvent.getPlayer()).thenReturn(mockPlayer);
        when(mockFaction.getTag()).thenReturn(otherTag);
        when(mockFaction.hasMember(playerUuid)).thenReturn(true);
        when(mockFactionService.getAllFactions()).thenReturn(Collections.singletonList(mockFaction));

        PlayerListener listener = new PlayerListener(mockPlugin);

        // Act
        listener.onChat(mockEvent);

        // Assert
        verify(mockEvent).setFormat(formatCaptor.capture());
        assertEquals("[" + otherTag + "] %1$s: %2$s", formatCaptor.getValue());
    }
}
