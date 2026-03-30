package com.factions.relation;

import com.factions.api.Faction;
import com.factions.api.Factions;
import com.factions.api.Relation;
import com.factions.api.RelationState;
import com.factions.api.RelationType;
import com.factions.event.RelationAcceptEvent;
import com.factions.event.RelationBreakEvent;
import com.factions.event.RelationDenyEvent;
import com.factions.event.RelationRequestEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * In-memory stub implementation of RelationManager.
 *
 * <p>This initial implementation stores relations in memory. Persistence and
 * database integration will be added in later beads.</p>
 */
public class RelationManagerImpl implements RelationManager {

    private static final long REQUEST_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L; // 7 days
    private final JavaPlugin plugin;

    // Store relations by canonical key (min UUID, max UUID)
    private final Map<UUID, Map<UUID, Relation>> relations = new HashMap<>();

    public RelationManagerImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Relation getRelation(Faction factionA, Faction factionB) {
        UUID a = factionA.getId();
        UUID b = factionB.getId();
        if (a.equals(b)) return null;
        UUID key = a.compareTo(b) < 0 ? a : b;
        UUID other = a.compareTo(b) < 0 ? b : a;
        Map<UUID, Relation> map = relations.get(key);
        if (map == null) return null;
        return map.get(other);
    }

    @Override
    public Relation requestRelation(Faction from, Faction to, RelationType type, Player requester) {
        UUID a = from.getId();
        UUID b = to.getId();
        if (a.equals(b)) return null;
        UUID key = a.compareTo(b) < 0 ? a : b;
        UUID other = a.compareTo(b) < 0 ? b : a;
        // Create pending relation, replace any existing
        RelationImpl relation = new RelationImpl(from, to, type, requester.getUniqueId(), RelationState.PENDING);
        relations.computeIfAbsent(key, k -> new HashMap<>()).put(other, relation);
        // Fire event
        Bukkit.getPluginManager().callEvent(new RelationRequestEvent(from, to, type, requester));
        return relation;
    }

    @Override
    public void acceptRelation(Relation relation, Player accepter) {
        if (relation instanceof RelationImpl impl) {
            Faction accepterFaction = Factions.getFaction(accepter.getUniqueId());
            if (accepterFaction == null) return;
            Faction otherFaction = impl.getOther(accepterFaction);
            if (otherFaction == null) return; // accepter not part of relation

            impl.setState(RelationState.ACCEPTED);
            impl.setTimestamp(System.currentTimeMillis());

            // Fire event
            Bukkit.getPluginManager().callEvent(new RelationAcceptEvent(accepterFaction, otherFaction, impl.getRelation(), accepter));
        }
    }

    @Override
    public void breakRelation(Relation relation, Player breaker) {
        if (relation instanceof RelationImpl impl) {
            Faction breakerFaction = Factions.getFaction(breaker.getUniqueId());
            RelationState previousState = impl.getState();

            // Fire break event before removal
            if (breakerFaction != null) {
                Faction otherFaction = impl.getOther(breakerFaction);
                if (otherFaction != null) {
                    Bukkit.getPluginManager().callEvent(new RelationBreakEvent(breakerFaction, otherFaction, impl.getRelation(), previousState, breaker));
                }
            }

            // Remove the relation entirely
            UUID a = impl.getFactionA().getId();
            UUID b = impl.getFactionB().getId();
            UUID key = a.compareTo(b) < 0 ? a : b;
            UUID other = a.compareTo(b) < 0 ? b : a;
            Map<UUID, Relation> map = relations.get(key);
            if (map != null) {
                map.remove(other);
                if (map.isEmpty()) {
                    relations.remove(key);
                }
            }
        }
    }

    @Override
    public Set<Relation> getRelations(Faction faction) {
        UUID id = faction.getId();
        Set<Relation> result = new HashSet<>();
        for (Map<UUID, Relation> map : relations.values()) {
            for (Relation r : map.values()) {
                if (r.getFactionA().getId().equals(id) || r.getFactionB().getId().equals(id)) {
                    result.add(r);
                }
            }
        }
        return result;
    }

    @Override
    public boolean hasAcceptedRelation(Faction factionA, Faction factionB, RelationType type) {
        Relation r = getRelation(factionA, factionB);
        return r != null && r.getState() == RelationState.ACCEPTED && r.getRelation() == type;
    }

    @Override
    public void denyRelation(Relation relation, Player denier) {
        if (relation instanceof RelationImpl impl) {
            Faction denierFaction = Factions.getFaction(denier.getUniqueId());
            if (denierFaction != null) {
                Faction otherFaction = impl.getOther(denierFaction);
                if (otherFaction != null) {
                    Bukkit.getPluginManager().callEvent(new RelationDenyEvent(denierFaction, otherFaction, impl.getRelation(), denier));
                }
            }

            // Remove the pending relation
            UUID a = impl.getFactionA().getId();
            UUID b = impl.getFactionB().getId();
            UUID key = a.compareTo(b) < 0 ? a : b;
            UUID other = a.compareTo(b) < 0 ? b : a;
            Map<UUID, Relation> map = relations.get(key);
            if (map != null) {
                map.remove(other);
                if (map.isEmpty()) {
                    relations.remove(key);
                }
            }
        }
    }

    @Override
    public void cancelRequest(Faction from, Faction to, Player canceller) {
        Relation relation = getRelation(from, to);
        if (relation instanceof RelationImpl impl) {
            // Only the requester can cancel
            if (!impl.getRequestedBy().equals(from.getId())) {
                // Not the requester; cannot cancel
                return;
            }
            if (impl.getState() != RelationState.PENDING) {
                return;
            }
            // Use breakRelation to fire event (treat as break)
            breakRelation(relation, canceller);
        }
    }

    @Override
    public Set<Relation> getPendingRequests(Faction faction) {
        UUID id = faction.getId();
        Set<Relation> result = new HashSet<>();
        for (Map<UUID, Relation> map : relations.values()) {
            for (Relation r : map.values()) {
                if (r.getState() == RelationState.PENDING &&
                    (r.getFactionA().getId().equals(id) || r.getFactionB().getId().equals(id))) {
                    result.add(r);
                }
            }
        }
        return result;
    }

    @Override
    public long getRequestExpiration() {
        return REQUEST_EXPIRATION_MS;
    }

    // Helper method to check if a relation is expired
    boolean isExpired(Relation relation) {
        return System.currentTimeMillis() - relation.getTimestamp() > REQUEST_EXPIRATION_MS;
    }
}
