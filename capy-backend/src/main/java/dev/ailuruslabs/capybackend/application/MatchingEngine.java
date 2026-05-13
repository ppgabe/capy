package dev.ailuruslabs.capybackend.application;

import dev.ailuruslabs.capybackend.domain.MatchResult;
import dev.ailuruslabs.capybackend.domain.UserProfile;

import java.util.Collection;

public interface MatchingEngine {
    MatchResult calculateMatches(Collection<UserProfile> processingPool);
}
