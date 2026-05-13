package dev.ailuruslabs.capybackend.application;

import dev.ailuruslabs.capybackend.domain.MatchPair;
import dev.ailuruslabs.capybackend.domain.UserProfile;

import java.util.Collection;
import java.util.Set;

public interface QueueService {
    void addUserToQueue(UserProfile profile);
    void removeUserFromQueue(UserProfile profile);
    Set<MatchPair> processEpoch();
}
