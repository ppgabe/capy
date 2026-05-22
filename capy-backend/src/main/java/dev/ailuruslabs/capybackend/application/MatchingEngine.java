package dev.ailuruslabs.capybackend.application;

import dev.ailuruslabs.capybackend.domain.MatchPair;
import dev.ailuruslabs.capybackend.domain.MatchResult;
import dev.ailuruslabs.capybackend.domain.UserProfile;

import java.util.Collection;
import java.util.List;

public interface MatchingEngine {
    MatchResult calculateMatches(List<MatchPair> scoredPairs, List<UserProfile> processingPool);
}
