package dev.ailuruslabs.capybackend.application;

import dev.ailuruslabs.capybackend.domain.UserProfile;

public interface QueueService {
    void addUserToQueue(UserProfile profile);
    void removeUserFromQueue(UserProfile profile);
    void processEpoch();
}
