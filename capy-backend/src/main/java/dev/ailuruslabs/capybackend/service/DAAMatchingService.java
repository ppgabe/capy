package dev.ailuruslabs.capybackend.service;

import dev.ailuruslabs.capybackend.application.MatchingEngine;
import dev.ailuruslabs.capybackend.domain.MatchResult;
import dev.ailuruslabs.capybackend.domain.UserProfile;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class DAAMatchingService implements MatchingEngine {
    @Override
    public MatchResult calculateMatches(Collection<UserProfile> processingPool) {
        // WIP
        return null;
    }
}
