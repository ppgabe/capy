package dev.ailuruslabs.capybackend.service;

import dev.ailuruslabs.capybackend.domain.MatchPair;
import dev.ailuruslabs.capybackend.domain.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DAAScoringServiceTest {

    private DAAScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new DAAScoringService();
    }

    @Test
    void testProposalScoringAndSorting() {
        UserProfile userA = new UserProfile("A", "UserA", Set.of("Java"), Set.of("Python"));
        UserProfile userB = new UserProfile("B", "UserB", Set.of("Python"), Set.of("Java"));
        UserProfile userC = new UserProfile("C", "UserC", Set.of("Java", "JavaScript", "Rust"), Set.of("Design"));
        UserProfile userD = new UserProfile("D", "UserD", Set.of("Design"), Set.of("Java", "JavaScript", "Rust"));
        UserProfile userE = new UserProfile("E", "UserE", Set.of("Design"), Set.of("C++"));

        List<UserProfile> pool = List.of(userA, userB, userC, userD, userE);

        List<MatchPair> scoredPairs = scoringService.scorePool(pool);

        // The algorithm finds 5 valid combinations where score > 0 (A-B, C-D, C-E, A-D, B-C)
        assertEquals(5, scoredPairs.size());

        MatchPair rank1 = scoredPairs.get(0);
        MatchPair rank2 = scoredPairs.get(1);

        // Verify Pair A-B: Expected 20 pts
        assertEquals("A", rank1.userA().id());
        assertEquals("B", rank1.userB().id());
        assertEquals(20.0, rank1.compatibilityScore(), 0.01);

        // Verify Pair C-D: Expected 13.33 pts
        assertEquals("C", rank2.userA().id());
        assertEquals("D", rank2.userB().id());
        assertEquals(13.33, rank2.compatibilityScore(), 0.01);

        // Ranks 3, 4, and 5 (C-E, A-D, B-C) are all one-sided matches tied at 5.0 pts
        assertEquals(5.0, scoredPairs.get(2).compatibilityScore(), 0.01);
        assertEquals(5.0, scoredPairs.get(3).compatibilityScore(), 0.01);
        assertEquals(5.0, scoredPairs.get(4).compatibilityScore(), 0.01);
    }
}
