package dev.ailuruslabs.capybackend.service;

import dev.ailuruslabs.capybackend.application.MatchingEngine;
import dev.ailuruslabs.capybackend.domain.MatchPair;
import dev.ailuruslabs.capybackend.domain.MatchResult;
import dev.ailuruslabs.capybackend.domain.UserProfile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class DAAMatchingService implements MatchingEngine {

    private double bestScore;
    private Set<MatchPair> bestCombination;

    @Override
    public MatchResult calculateMatches(List<MatchPair> sortedPairs, List<UserProfile> allUsers) {
        this.bestScore = 0.0;
        this.bestCombination = new HashSet<>();

        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Future<?> future = executor.submit(() -> runBranchAndBound(sortedPairs));

            try {
                future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
            }
        }

        // If B&B timed out before finding any valid combination, fallback to a greedy approach
        if (bestCombination.isEmpty() && !sortedPairs.isEmpty()) {
            bestCombination = greedyMatch(sortedPairs);
        }

        Set<UserProfile> matchedUsers = new HashSet<>();
        for (MatchPair pair : bestCombination) {
            matchedUsers.add(pair.userA());
            matchedUsers.add(pair.userB());
        }

        Set<UserProfile> unmatchedUsers = new HashSet<>(allUsers);
        unmatchedUsers.removeAll(matchedUsers);

        return new MatchResult(bestCombination, unmatchedUsers);
    }

    private void runBranchAndBound(List<MatchPair> pairs) {
        // Pre-calculate maximum possible remaining scores to prune weak branches early
        double[] maxRemaining = new double[pairs.size()];
        double sum = 0;
        for (int i = pairs.size() - 1; i >= 0; i--) {
            sum += pairs.get(i).compatibilityScore();
            maxRemaining[i] = sum;
        }

        explore(0, 0.0, new HashSet<>(), new HashSet<>(), pairs, maxRemaining);
    }

    private void explore(int index, double currentScore, Set<MatchPair> currentPairs,
                         Set<String> usedUsers, List<MatchPair> pairs, double[] maxRemaining) {

        if (Thread.currentThread().isInterrupted()) {
            return; // Exit immediately if the 5-second timeout hit
        }

        if (index == pairs.size()) {
            if (currentScore > bestScore) {
                bestScore = currentScore;
                bestCombination = new HashSet<>(currentPairs);
            }
            return;
        }

        // Bounding mechanism: Prune if this branch mathematically cannot beat the best score
        if (currentScore + maxRemaining[index] <= bestScore) {
            return;
        }

        MatchPair pair = pairs.get(index);

        // Branch 1: Try adding the current pair to the combination
        if (!usedUsers.contains(pair.userA().id()) && !usedUsers.contains(pair.userB().id())) {
            usedUsers.add(pair.userA().id());
            usedUsers.add(pair.userB().id());
            currentPairs.add(pair);

            explore(index + 1, currentScore + pair.compatibilityScore(), currentPairs, usedUsers, pairs, maxRemaining);

            // Backtrack
            usedUsers.remove(pair.userA().id());
            usedUsers.remove(pair.userB().id());
            currentPairs.remove(pair);
        }

        // Branch 2: Skip the current pair
        explore(index + 1, currentScore, currentPairs, usedUsers, pairs, maxRemaining);
    }

    private Set<MatchPair> greedyMatch(List<MatchPair> sortedPairs) {
        Set<MatchPair> matches = new HashSet<>();
        Set<String> used = new HashSet<>();
        for (MatchPair pair : sortedPairs) {
            if (!used.contains(pair.userA().id()) && !used.contains(pair.userB().id())) {
                matches.add(pair);
                used.add(pair.userA().id());
                used.add(pair.userB().id());
            }
        }
        return matches;
    }
}
