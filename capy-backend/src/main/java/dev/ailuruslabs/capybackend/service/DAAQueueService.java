package dev.ailuruslabs.capybackend.service;

import dev.ailuruslabs.capybackend.application.MatchingEngine;
import dev.ailuruslabs.capybackend.application.QueueService;
import dev.ailuruslabs.capybackend.application.ScoringEngine;
import dev.ailuruslabs.capybackend.domain.MatchPair;
import dev.ailuruslabs.capybackend.domain.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DAAQueueService implements QueueService {
    private final Map<String, UserProfile> waitingPool = new ConcurrentHashMap<>();

    private final ScoringEngine scoringEngine;
    private final MatchingEngine matchingEngine;
    private final SimpMessagingTemplate messagingTemplate;

    public void addUserToQueue(UserProfile profile) {
        waitingPool.put(profile.id(), profile);
    }

    public void removeUserFromQueue(String userId) {
        waitingPool.remove(userId);
    }

    @Scheduled(fixedRate = 15000)
    public void processEpoch() {
        List<UserProfile> processingPool = new ArrayList<>();
        Iterator<UserProfile> iterator = waitingPool.values().iterator();

        // Safely transfer all UserProfiles in the queue
        while (iterator.hasNext()) {
            processingPool.add(iterator.next());
            iterator.remove();
        }

        var scoredUsers = scoringEngine.scorePool(processingPool);
        var matchResult = matchingEngine.calculateMatches(scoredUsers, processingPool);

        for (UserProfile unmatched : matchResult.unmatchedUsers()) {
            waitingPool.put(unmatched.id(), unmatched);
        }

        for (MatchPair pair : matchResult.optimalMatches()) {
            messagingTemplate.convertAndSend("/queue/match/" + pair.userA().id(), pair);

            messagingTemplate.convertAndSend("/queue/match/" + pair.userB().id(), pair);
        }
    }
}
