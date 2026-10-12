package com.orbit.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.orbit.dao.ConnectionDao;
import com.orbit.dao.UserDao;
import com.orbit.model.MatchResult;
import com.orbit.model.User;

/**
 * Service Class: MatchService
 * 
 * RESPONSIBILITY (Core Differentiating Feature):
 * Encapsulates the "ORBIT MATCH" algorithm:
 * 1. Fetches candidate users from the database.
 * 2. Compares the logged-in user's comma-separated interests with candidate interests in Java memory.
 * 3. Extracts common interests using trim() and equalsIgnoreCase().
 * 4. Calculates dynamic Orbit Match compatibility score (0 - 100%).
 * 5. Attaches connection status (NONE, PENDING_SENT, PENDING_RECEIVED, ACCEPTED).
 * 6. Sorts candidates by compatibility so best matches appear first.
 */
public class MatchService {

    private final UserDao userDao = new UserDao();
    private final ConnectionDao connectionDao = new ConnectionDao();

    /**
     * Finds matches for the specified user against all other registered users in Orbit.
     * 
     * @param currentUser The currently authenticated user.
     * @return List of MatchResult objects ordered from highest to lowest compatibility.
     */
    public List<MatchResult> findMatchesForUser(User currentUser) {
        List<MatchResult> results = new ArrayList<>();
        if (currentUser == null) {
            return results;
        }

        // Get all candidate users (everyone except currentUser)
        List<User> candidates = userDao.getAllUsersExcept(currentUser.getId());
        List<String> myInterests = currentUser.getInterestsList();

        for (User candidate : candidates) {
            List<String> theirInterests = candidate.getInterestsList();
            List<String> commonInterests = new ArrayList<>();

            // Compare individual interests using trim() and equalsIgnoreCase()
            for (String myItem : myInterests) {
                for (String theirItem : theirInterests) {
                    if (myItem.trim().equalsIgnoreCase(theirItem.trim())) {
                        if (!commonInterests.contains(myItem.trim())) {
                            commonInterests.add(myItem.trim());
                        }
                    }
                }
            }

            // Calculate dynamic compatibility score
            int maxInterests = Math.max(myInterests.size(), theirInterests.size());
            int score = 0;
            if (maxInterests > 0 && !commonInterests.isEmpty()) {
                score = (int) Math.round(((double) commonInterests.size() / maxInterests) * 100);
            }

            // Determine connection relationship
            String status = connectionDao.getConnectionStatus(currentUser.getId(), candidate.getId());

            MatchResult match = new MatchResult(candidate, score, commonInterests, status);
            results.add(match);
        }

        // Sort descending: highest match percentage first
        results.sort((m1, m2) -> Integer.compare(m2.getMatchPercentage(), m1.getMatchPercentage()));

        return results;
    }
}
