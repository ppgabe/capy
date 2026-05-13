package dev.ailuruslabs.capybackend.application;

import dev.ailuruslabs.capybackend.domain.UserProfile;

import java.util.Collection;

public interface ScoringEngine {
    Collection<UserProfile> scorePool(Collection<UserProfile> processingPool);
}
