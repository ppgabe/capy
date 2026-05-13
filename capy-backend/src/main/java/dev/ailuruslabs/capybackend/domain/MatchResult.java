package dev.ailuruslabs.capybackend.domain;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record MatchResult(
        Set<MatchPair> optimalMatches,
        Set<UserProfile> unmatchedUsers
) {
    public MatchResult {
        Objects.requireNonNull(optimalMatches);
        Objects.requireNonNull(unmatchedUsers);
    }
}
