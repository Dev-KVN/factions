package com.factions.api;

import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Represents a member of a faction with role and permissions.
 */
public class FactionMember {

    public enum Role {
        LEADER,
        OFFICER,
        MEMBER,
        RECRUIT;

        public boolean canInvite() {
            return this == LEADER || this == OFFICER;
        }

        public boolean canClaim() {
            return this == LEADER || this == OFFICER || this == MEMBER;
        }

        public boolean canSetHome() {
            return this == LEADER || this == OFFICER;
        }

        public boolean canSetRelation() {
            return this == LEADER || this == OFFICER;
        }
    }

    private final UUID playerId;
    private Role role;
    private LocalDateTime joinedAt;
    private LocalDateTime lastOnline;
    private String lastIp;
    private double contributedPower;
    private boolean banned;

    public FactionMember(UUID playerId) {
        this.playerId = playerId;
        this.role = Role.RECRUIT;
        this.joinedAt = LocalDateTime.now();
        this.lastOnline = LocalDateTime.now();
        this.lastIp = null;
        this.contributedPower = 0.0;
        this.banned = false;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public LocalDateTime getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(LocalDateTime lastOnline) {
        this.lastOnline = lastOnline;
    }

    public String getLastIp() {
        return lastIp;
    }

    public void setLastIp(String lastIp) {
        this.lastIp = lastIp;
    }

    public double getContributedPower() {
        return contributedPower;
    }

    public void setContributedPower(double contributedPower) {
        this.contributedPower = contributedPower;
    }

    public void addContributedPower(double amount) {
        this.contributedPower += amount;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    /**
     * Promote the member to the next role.
     */
    public boolean promote() {
        switch (role) {
            case RECRUIT:
                role = Role.MEMBER;
                return true;
            case MEMBER:
                role = Role.OFFICER;
                return true;
            case OFFICER:
                role = Role.LEADER;
                return true;
            case LEADER:
            default:
                return false;
        }
    }

    /**
     * Demote the member to the previous role.
     */
    public boolean demote() {
        switch (role) {
            case LEADER:
                role = Role.OFFICER;
                return true;
            case OFFICER:
                role = Role.MEMBER;
                return true;
            case MEMBER:
                role = Role.RECRUIT;
                return true;
            case RECRUIT:
            default:
                return false;
        }
    }

    /**
     * Checks if this member can kick another member.
     */
    public boolean canKick(Role targetRole, Role selfRole) {
        if (selfRole == Role.LEADER) {
            return true;
        }
        if (selfRole == Role.OFFICER) {
            return targetRole.ordinal() < Role.OFFICER.ordinal();
        }
        return false;
    }

    /**
     * Checks if this member can invite new members.
     */
    public boolean canInvite() {
        return role == Role.LEADER || role == Role.OFFICER;
    }

    /**
     * Checks if this member can set relation with other factions.
     */
    public boolean canSetRelation() {
        return role == Role.LEADER || role == Role.OFFICER;
    }

    /**
     * Checks if this member can modify faction home.
     */
    public boolean canSetHome() {
        return role == Role.LEADER || role == Role.OFFICER;
    }

    /**
     * Checks if this member can claim land.
     */
    public boolean canClaim() {
        return role == Role.LEADER || role == Role.OFFICER || role == Role.MEMBER;
    }
}
