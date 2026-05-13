package dev.ailuruslabs.capybackend.service;

import dev.ailuruslabs.capybackend.application.ScoringEngine;
import dev.ailuruslabs.capybackend.domain.UserProfile;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class DAAScoringService implements ScoringEngine {
    @Override
    public Collection<UserProfile> scorePool(Collection<UserProfile> processingPool) {
        // WIP
        return List.of();
    }
}
