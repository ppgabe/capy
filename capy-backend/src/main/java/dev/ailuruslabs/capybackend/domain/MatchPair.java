package dev.ailuruslabs.capybackend.domain;

import java.util.Objects;

public record MatchPair(
        UserProfile userA,
        UserProfile userB,
        double compatibilityScore
) {

    public MatchPair {
        Objects.requireNonNull(userA);
        Objects.requireNonNull(userB);
    }

}
