package com.factions.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Implementation of the Faction interface.
 * Uses thread-safe collections for concurrent access.
 */
public class FactionImpl implements Faction {

    private final UUID id;
    private final AtomicReference<String> name;
    private final AtomicReference<String> tag;
    private final AtomicReference<String> description;
    private final AtomicReference<String> motd;
    private final AtomicReference<UUID> leaderId;
    private final AtomicReference<String> homeWorld;
    private final AtomicReference<Integer> homeX;
    private final AtomicReference<Integer> homeY;
    private final AtomicReference<Integer> homeZ;
    private final AtomicReference<Double> bankBalance;
    private final AtomicReference<Long> createdAt;
    private final AtomicReference<Long> lastSeen;
    private final AtomicReference<Double> power;
    private final AtomicReference<Integer> maxClaims;
    private final AtomicReference<Integer> claimCount;
    private final AtomicReference<Boolean> peaceful;
    private final AtomicReference<Boolean> permanent;

    private final Set<UUID> members;
    private final Set<UUID> invites;
    private final Set<UUID> banned;
    private final Set<Claim> claims;

    // Relation tracking: key = other faction ID, value = relation state
    private final ConcurrentHashMap<UUID, RelationState> relations;

    public FactionImpl(UUID id, String name, String tag) {
        this.id = id;
        this.name = new AtomicReference<>(name);
        this.tag = new AtomicReference<>(tag);
        this.description = new AtomicReference<>("");
        this.motd = new AtomicReference<>("");
        this.leaderId = new AtomicReference<>(null);
        this.homeWorld = new AtomicReference<>(null);
        this.homeX = new AtomicReference<>(0);
        this.homeY = new AtomicReference<>(0);
        this.homeZ = new AtomicReference<>(0);
        this.bankBalance = new AtomicReference<>(0.0);
        this.createdAt = new AtomicReference<>(System.currentTimeMillis());
        this.lastSeen = new AtomicReference<>(System.currentTimeMillis());
        this.power = new AtomicReference<>(0.0);
        this.maxClaims = new AtomicReference<>(0);
        this.claimCount = new AtomicReference<>(0);
        this.peaceful = new AtomicReference<>(false);
        this.permanent = new AtomicReference<>(false);

        this.members = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.invites = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.banned = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.claims = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.relations = new ConcurrentHashMap<>();
    }

    public FactionImpl(UUID id) {
        this(id, "Unnamed", "???");
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getTag() {
        return tag.get();
    }

    @Override
    public void setTag(String tag) {
        this.tag.set(tag);
    }

    @Override
    public String getDescription() {
        return description.get();
    }

    @Override
    public void setDescription(String description) {
        this.description.set(description);
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public String getMotd() {
        return motd.get();
    }

    @Override
    public void setMotd(String motd) {
        this.motd.set(motd);
    }

    @Override
    public UUID getLeaderId() {
        return leaderId.get();
    }

    @Override
    public void setLeaderId(UUID leaderId) {
        this.leaderId.set(leaderId);
    }

    @Override
    public double getBankBalance() {
        return bankBalance.get();
    }

    @Override
    public void setBankBalance(double balance) {
        this.bankBalance.set(balance);
    }

    @Override
    public long getCreatedAt() {
        return createdAt.get();
    }

    @Override
    public void setCreatedAt(long timestamp) {
        this.createdAt.set(timestamp);
    }

    @Override
    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    @Override
    public void addMember(UUID playerId) {
        members.add(playerId);
        recalculatePower();
    }

    @Override
    public void removeMember(UUID playerId) {
        members.remove(playerId);
        recalculatePower();
    }

    @Override
    public boolean hasMember(UUID playerId) {
        return members.contains(playerId);
    }

    @Override
    public Set<UUID> getInvites() {
        return Collections.unmodifiableSet(invites);
    }

    @Override
    public void addInvite(UUID playerId) {
        invites.add(playerId);
    }

    @Override
    public void removeInvite(UUID playerId) {
        invites.remove(playerId);
    }

    @Override
    public Set<UUID> getBanned() {
        return Collections.unmodifiableSet(banned);
    }

    @Override
    public void ban(UUID playerId) {
        banned.add(playerId);
    }

    @Override
    public void unban(UUID playerId) {
        banned.remove(playerId);
    }

    @Override
    public RelationState getRelation(Faction other) {
        return relations.getOrDefault(other.getId(), RelationState.NEUTRAL);
    }

    @Override
    public void setRelation(Faction other, RelationState state) {
        if (state == RelationState.NEUTRAL) {
            relations.remove(other.getId());
        } else {
            relations.put(other.getId(), state);
        }
    }

    @Override
    public double getPower() {
        return power.get();
    }

    @Override
    public void setPower(double power) {
        this.power.set(power);
    }

    /**
     * Recalculates faction power from member data.
     * This would typically integrate with the PowerService.
     * For now, power is accumulated when members are added/removed.
     */
    private void recalculatePower() {
        // Placeholder: power would be sum of all member power
        // The actual implementation would query PowerService
    }

    @Override
    public int getMaxClaims() {
        return maxClaims.get();
    }

    @Override
    public void setMaxClaims(int maxClaims) {
        this.maxClaims.set(maxClaims);
    }

    @Override
    public int getClaimCount() {
        return claimCount.get();
    }

    @Override
    public void setClaimCount(int claimCount) {
        this.claimCount.set(claimCount);
    }

    @Override
    public Set<Claim> getClaims() {
        return Collections.unmodifiableSet(claims);
    }

    @Override
    public void addClaim(Claim claim) {
        claims.add(claim);
        claimCount.set(claims.size());
    }

    @Override
    public void removeClaim(Claim claim) {
        claims.remove(claim);
        claimCount.set(claims.size());
    }

    @Override
    public String getHomeWorld() {
        return homeWorld.get();
    }

    @Override
    public void setHomeWorld(String world) {
        this.homeWorld.set(world);
    }

    @Override
    public int getHomeX() {
        return homeX.get();
    }

    @Override
    public void setHomeX(int x) {
        this.homeX.set(x);
    }

    @Override
    public int getHomeY() {
        return homeY.get();
    }

    @Override
    public void setHomeY(int y) {
        this.homeY.set(y);
    }

    @Override
    public int getHomeZ() {
        return homeZ.get();
    }

    @Override
    public void setHomeZ(int z) {
        this.homeZ.set(z);
    }

    @Override
    public boolean hasHome() {
        return homeWorld.get() != null;
    }

    @Override
    public boolean isPeaceful() {
        return peaceful.get();
    }

    @Override
    public void setPeaceful(boolean peaceful) {
        this.peaceful.set(peaceful);
    }

    @Override
    public boolean isPermanent() {
        return permanent.get();
    }

    @Override
    public void setPermanent(boolean permanent) {
        this.permanent.set(permanent);
    }

    @Override
    public long getLastSeen() {
        return lastSeen.get();
    }

    @Override
    public void setLastSeen(long timestamp) {
        this.lastSeen.set(timestamp);
    }

    /**
     * Updates the last seen timestamp to now.
     */
    public void touch() {
        this.lastSeen.set(System.currentTimeMillis());
    }

    /**
     * Gets the internal claims set (use with caution).
     */
    Set<Claim> getClaimsInternal() {
        return claims;
    }

    /**
     * Gets the internal relations map (use with caution).
     */
    ConcurrentHashMap<UUID, RelationState> getRelationsInternal() {
        return relations;
    }
}
