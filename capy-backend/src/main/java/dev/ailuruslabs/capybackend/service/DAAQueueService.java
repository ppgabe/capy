package dev.ailuruslabs.capybackend.service;

import com.fasterxml.jackson.databind.deser.DataFormatReaders;
import dev.ailuruslabs.capybackend.application.MatchingEngine;
import dev.ailuruslabs.capybackend.application.QueueService;
import dev.ailuruslabs.capybackend.application.ScoringEngine;
import dev.ailuruslabs.capybackend.domain.MatchPair;
import dev.ailuruslabs.capybackend.domain.UserProfile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DAAQueueService implements QueueService {
    private final Set<UserProfile> waitingPool = ConcurrentHashMap.newKeySet();

    private final ScoringEngine scoringEngine;
    private final MatchingEngine matchingEngine;

    public DAAQueueService(ScoringEngine scoringEngine, MatchingEngine matchingEngine) {
        this.scoringEngine = scoringEngine;
        this.matchingEngine = matchingEngine;
    }

    public void addUserToQueue(UserProfile profile) {
        waitingPool.add(profile);
    }

    public void removeUserFromQueue(UserProfile profile) {
        waitingPool.remove(profile);
    }

    @Scheduled(fixedRate = 15000)
    public Set<MatchPair> processEpoch() {
        List<UserProfile> processingPool = new ArrayList<>();
        Iterator<UserProfile> iterator = waitingPool.iterator();

        // Safely transfer all UserProfiles in the queue
        while (iterator.hasNext()) {
            processingPool.add(iterator.next());
            iterator.remove();
        }

        var scoredUsers = scoringEngine.scorePool(processingPool);
        var matchResult = matchingEngine.calculateMatches(scoredUsers);

        waitingPool.addAll(matchResult.unmatchedUsers());

        return matchResult.optimalMatches();
    }
}
