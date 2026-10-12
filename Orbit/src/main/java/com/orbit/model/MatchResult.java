package com.orbit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model / DTO Class: MatchResult
 * 
 * RESPONSIBILITY:
 * Encapsulates the dynamic "Orbit Match" compatibility result between 
 * the logged-in user and another user.
 * 
 * WHY IS THIS NEEDED?
 * As per architecture rules, there is NO 'matches' table in the database.
 * The Match score is computed dynamically in Java memory (Service layer).
 * This class holds the computation result:
 * - The matched User
 * - Orbit Match score (e.g., 75%)
 * - List of common interests (e.g., ["Java", "AI"])
 * - Current connection status (e.g., "NONE", "PENDING", "ACCEPTED")
 */
public class MatchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private User targetUser;
    private int matchPercentage; // 0 to 100
    private List<String> commonInterests = new ArrayList<>();
    private String connectionStatus; // NONE, PENDING, ACCEPTED, etc.

    public MatchResult() {
    }

    public MatchResult(User targetUser, int matchPercentage, List<String> commonInterests, String connectionStatus) {
        this.targetUser = targetUser;
        this.matchPercentage = matchPercentage;
        this.commonInterests = commonInterests;
        this.connectionStatus = connectionStatus;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }

    public int getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(int matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public List<String> getCommonInterests() {
        return commonInterests;
    }

    public void setCommonInterests(List<String> commonInterests) {
        this.commonInterests = commonInterests;
    }

    public String getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    @Override
    public String toString() {
        return "MatchResult [targetUser=" + (targetUser != null ? targetUser.getuName() : "null")
                + ", matchPercentage=" + matchPercentage + "%, commonInterests=" + commonInterests
                + ", connectionStatus=" + connectionStatus + "]";
    }
}
