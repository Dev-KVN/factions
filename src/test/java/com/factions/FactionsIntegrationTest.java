package com.factions;

import com.factions.api.*;
import com.factions.config.PowerConfiguration;
import com.factions.persistence.*;
import com.factions.service.*;
import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Factions plugin.
 * Tests the full workflow from data model through services.
 */
public class FactionsIntegrationTest {

    private static DatabaseManager db;
    private static FactionService factionService;
    private static PowerService powerService;
    private static ClaimService claimService;
    private static RelationService relationService;

    @BeforeAll
    public static void setUp() throws SQLException {
        // Use file-based SQLite database for test isolation
        File tempDb = new File("target/test-factions.db");
        // Delete any existing test database to start fresh
        if (tempDb.exists()) {
            tempDb.delete();
        }
        db = new DatabaseManager(tempDb.getAbsolutePath());
        db.initialize();
        db.initializeSchema();

        PowerConfiguration powerConfig = new PowerConfiguration(
            1000.0,   // maxPower
            1.0,      // gainRate
            10.0,     // deathPenaltyPercent
            PowerConfiguration.OfflineMode.DECAY,
            0.5,      // offlineDecayRate
            1.0,      // minutesPerPoint
            0.0,      // incrementPerDay
            0.01      // minClaimsPerPower
        );
        powerService = new PowerService(db, powerConfig);
        factionService = new FactionService(db, powerService);
        relationService = new RelationService(db);
        claimService = new ClaimService(db, powerService, factionService);
    }

    @AfterAll
    public static void tearDown() {
        if (db != null) {
            db.shutdown();
        }
    }

    @Test
    public void testCreateFaction() throws SQLException {
        UUID leaderId = UUID.randomUUID();
        Faction faction = factionService.createFaction("TestFaction", "TEST", leaderId);  // TEST is 4 chars, valid

        assertNotNull(faction);
        assertEquals("TestFaction", faction.getName());
        assertEquals("TEST", faction.getTag());
        assertEquals(leaderId, faction.getLeaderId());
        assertTrue(faction.hasMember(leaderId));
        assertEquals(1, faction.getMembers().size());
    }

    @Test
    public void testPowerCalculation() throws SQLException {
        UUID leaderId = UUID.randomUUID();
        Faction faction = factionService.createFaction("PowerFaction", "PWR", leaderId);  // PWR is 3 chars

        UUID memberId = UUID.randomUUID();
        factionService.addMember(faction, memberId);

        double power = powerService.recalculateFactionPower(faction);
        assertTrue(power >= 0);
    }

    @Test
    public void testClaimChunk() throws SQLException {
        UUID leaderId = UUID.randomUUID();
        Faction faction = factionService.createFaction("ClaimFaction", "CLM", leaderId);  // CLM is 3 chars

        boolean claimed = claimService.claimChunk(faction, "world", 0, 0, "TestPlayer");
        assertTrue(claimed);
        assertEquals(1, faction.getClaimCount());

        // Try to claim same chunk again
        boolean claimedAgain = claimService.claimChunk(faction, "world", 0, 0, "TestPlayer");
        assertFalse(claimedAgain);

        // Unclaim
        boolean unclaimed = claimService.unclaimChunk(faction, "world", 0, 0);
        assertTrue(unclaimed);
        assertEquals(0, faction.getClaimCount());
    }

    @Test
    public void testClaimLimit() throws SQLException {
        UUID leaderId = UUID.randomUUID();
        Faction faction = factionService.createFaction("LimitFaction", "LMT", leaderId);  // LMT is 3 chars

        // Set a low max claims
        faction.setMaxClaims(2);

        boolean claim1 = claimService.claimChunk(faction, "world", 0, 0, "TestPlayer");
        assertTrue(claim1);

        boolean claim2 = claimService.claimChunk(faction, "world", 1, 0, "TestPlayer");
        assertTrue(claim2);

        boolean claim3 = claimService.claimChunk(faction, "world", 2, 0, "TestPlayer");
        assertFalse(claim3); // Exceeded limit
    }

    @Test
    public void testRelationManagement() throws SQLException {
        UUID leader1 = UUID.randomUUID();
        Faction factionA = factionService.createFaction("FactionA", "FA", leader1);

        UUID leader2 = UUID.randomUUID();
        Faction factionB = factionService.createFaction("FactionB", "FB", leader2);

        boolean allySet = relationService.setRelation(factionA, factionB, RelationState.ALLY, leader1);
        assertTrue(allySet);
        assertEquals(RelationState.ALLY, relationService.getRelation(factionA, factionB));
        assertEquals(RelationState.ALLY, relationService.getRelation(factionB, factionA));

        boolean neutralSet = relationService.setRelation(factionA, factionB, RelationState.NEUTRAL, leader1);
        assertTrue(neutralSet);
        assertEquals(RelationState.NEUTRAL, relationService.getRelation(factionA, factionB));
    }

    @Test
    public void testMemberManagement() throws SQLException {
        UUID leaderId = UUID.randomUUID();
        Faction faction = factionService.createFaction("MemberFaction", "MEM", leaderId);

        UUID memberId = UUID.randomUUID();
        boolean added = factionService.addMember(faction, memberId);
        assertTrue(added);
        assertTrue(faction.hasMember(memberId));
        assertEquals(2, faction.getMembers().size());

        boolean removed = factionService.removeMember(faction, memberId);
        assertTrue(removed);
        assertFalse(faction.hasMember(memberId));
    }

    @Test
    public void testFactionPersistence() throws SQLException {
        UUID leaderId = UUID.randomUUID();
        Faction faction1 = factionService.createFaction("Persist1", "P1", leaderId);
        UUID faction1Id = faction1.getId();

        // Clear cache
        factionService.clearCache();

        // Reload from database
        Faction reloaded = factionService.getFaction(faction1Id);
        assertNotNull(reloaded);
        assertEquals("Persist1", reloaded.getName());
        assertEquals("P1", reloaded.getTag());
    }

    @Test
    public void testPowerRegeneration() {
        UUID playerId = UUID.randomUUID();
        powerService.setOnline(playerId, true);
        double initialPower = powerService.getPower(playerId);
        assertTrue(initialPower >= 0);

        powerService.setOnline(playerId, false);
        double afterOffline = powerService.getPower(playerId);
        // Power should be same or slightly less due to decay simulation
        assertTrue(afterOffline >= 0);
    }
}
