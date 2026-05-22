package dev.ailuruslabs.capybackend.service;

import dev.ailuruslabs.capybackend.domain.MatchPair;
import dev.ailuruslabs.capybackend.domain.MatchResult;
import dev.ailuruslabs.capybackend.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DAAMatchingServiceTest {

    private DAAMatchingService matchingService;

    @BeforeEach
    void setUp() {
        matchingService = new DAAMatchingService();
    }

    @Test
    void testBranchAndBoundOptimalMatching() {
        // Given
        UserProfile userA = new UserProfile("A", "UserA", Set.of("Java"), Set.of("Python"));
        UserProfile userB = new UserProfile("B", "UserB", Set.of("Python"), Set.of("Java"));
        UserProfile userC = new UserProfile("C", "UserC", Set.of("Java", "JavaScript", "Rust"), Set.of("Design"));
        UserProfile userD = new UserProfile("D", "UserD", Set.of("Design"), Set.of("Java", "JavaScript", "Rust"));
        UserProfile userE = new UserProfile("E", "UserE", Set.of("Design"), Set.of("C++"));

        List<UserProfile> allUsers = List.of(userA, userB, userC, userD, userE);

        // Mocking the sorted output from the ScoringEngine
        MatchPair pairAB = new MatchPair(userA, userB, 20.0);
        MatchPair pairCD = new MatchPair(userC, userD, 13.33);
        MatchPair pairCE = new MatchPair(userC, userE, 5.0);
        List<MatchPair> sortedPairs = List.of(pairAB, pairCD, pairCE);

        // When
        MatchResult result = matchingService.calculateMatches(sortedPairs, allUsers);

        // Then
        // The optimal combination should be A-B and C-D (Total 33.33 pts)
        assertEquals(2, result.optimalMatches().size());
        assertTrue(result.optimalMatches().contains(pairAB));
        assertTrue(result.optimalMatches().contains(pairCD));

        // User C cannot be paired with E because C is already paired with D
        assertTrue(!result.optimalMatches().contains(pairCE));

        // User E should be placed in the unmatched pool to be returned to the Queue
        assertEquals(1, result.unmatchedUsers().size());
        assertTrue(result.unmatchedUsers().contains(userE));
    }
}
