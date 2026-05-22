package dev.ailuruslabs.capybackend.domain;

import java.util.Objects;

public record MatchPair(
        UserProfile userA,
        UserProfile userB,
        double compatibilityScore
) implements Comparable<MatchPair> {

    public MatchPair {
        Objects.requireNonNull(userA);
        Objects.requireNonNull(userB);
    }

    @Override
    public int compareTo(MatchPair o) {
        return Double.compare(o.compatibilityScore, this.compatibilityScore);
    }
}
