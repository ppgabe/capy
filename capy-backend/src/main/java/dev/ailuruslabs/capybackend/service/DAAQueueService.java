package dev.ailuruslabs.capybackend.service;

import dev.ailuruslabs.capybackend.application.MatchingEngine;
import dev.ailuruslabs.capybackend.application.QueueService;
import dev.ailuruslabs.capybackend.application.ScoringEngine;
import dev.ailuruslabs.capybackend.domain.MatchPair;
import dev.ailuruslabs.capybackend.domain.UserProfile;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(DAAQueueService.class);
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
        logger.atInfo().log("Processing epoch...");

        List<UserProfile> processingPool = new ArrayList<>();
        Iterator<UserProfile> iterator = waitingPool.values().iterator();

        // Safely transfer all UserProfiles in the queue
        while (iterator.hasNext()) {
            processingPool.add(iterator.next());
            iterator.remove();
        }

        logger.atInfo().log("Processing pool has " + processingPool.size() + " users.");

        var scoredUsers = scoringEngine.scorePool(processingPool);
        var matchResult = matchingEngine.calculateMatches(scoredUsers, processingPool);

        for (UserProfile unmatched : matchResult.unmatchedUsers()) {
            waitingPool.put(unmatched.id(), unmatched);
        }

        for (MatchPair pair : matchResult.optimalMatches()) {
            messagingTemplate.convertAndSend("/queue/match/" + pair.userA().id(), pair);
            messagingTemplate.convertAndSend("/queue/match/" + pair.userB().id(), pair);

            // Re-add demo users so they never run out
            if (isDemoUser(pair.userA())) {
                waitingPool.put(pair.userA().id(), pair.userA());
            }
            if (isDemoUser(pair.userB())) {
                waitingPool.put(pair.userB().id(), pair.userB());
            }
        }
    }

    private boolean isDemoUser(UserProfile profile) {
        return profile.email() != null && profile.email().endsWith("@demo.capy.local");
    }
}
