package dev.ailuruslabs.capybackend.service;

import dev.ailuruslabs.capybackend.domain.ChatMessage;
import dev.ailuruslabs.capybackend.domain.UserProfile;
import dev.ailuruslabs.capybackend.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DemoChatService {
    private static final String DEMO_EMAIL_DOMAIN = "demo.capy.local";
    private static final Duration ACTIVE_WINDOW = Duration.ofMinutes(5);
    private static final Duration MIN_BOT_REPLY_GAP = Duration.ofSeconds(6);

    private final UserProfileRepository userProfileRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final DemoLlmClient demoLlmClient;
    private final Map<String, ActiveMatch> activeMatches = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public void handleIncoming(String matchId, ChatMessage message) {
        Optional<ActiveMatch> match = resolveMatch(matchId);
        if (match.isEmpty()) return;

        ActiveMatch activeMatch = match.get();
        activeMatch.lastActivity = Instant.now();

        if (!message.senderId().equals(activeMatch.realUser.id())) return;
        if (!activeMatch.canSendBotReply()) return;

        sendBotReply(matchId, activeMatch, message.content());
    }

    @Scheduled(fixedRateString = "${demo.llm.chatterIntervalMs:12000}")
    public void sendPeriodicChatter() {
        if (!demoLlmClient.isEnabled()) return;

        Instant now = Instant.now();
        for (Map.Entry<String, ActiveMatch> entry : activeMatches.entrySet()) {
            ActiveMatch match = entry.getValue();

            if (Duration.between(match.lastActivity, now).compareTo(ACTIVE_WINDOW) > 0) {
                activeMatches.remove(entry.getKey());
                continue;
            }

            if (!match.canSendBotReply()) continue;

            if (random.nextBoolean()) {
                sendBotReply(entry.getKey(), match, "Say something encouraging and short.");
            }
        }
    }

    private Optional<ActiveMatch> resolveMatch(String matchId) {
        ActiveMatch cached = activeMatches.get(matchId);
        if (cached != null) return Optional.of(cached);

        String[] parts = matchId.split("-");
        if (parts.length < 2) return Optional.empty();

        Optional<UserProfile> first = userProfileRepository.findById(parts[0]);
        Optional<UserProfile> second = userProfileRepository.findById(parts[1]);

        if (first.isEmpty() || second.isEmpty()) return Optional.empty();

        UserProfile userA = first.get();
        UserProfile userB = second.get();

        boolean aDemo = isDemoUser(userA);
        boolean bDemo = isDemoUser(userB);

        if (aDemo == bDemo) return Optional.empty();

        String demoUserId = aDemo ? userA.id() : userB.id();
        String realUserId = aDemo ? userB.id() : userA.id();
        UserProfile demoProfile = aDemo ? userA : userB;
        UserProfile realProfile = aDemo ? userB : userA;

        ActiveMatch match = new ActiveMatch(demoProfile, realProfile, Instant.now());
        activeMatches.put(matchId, match);

        return Optional.of(match);
    }

    private boolean isDemoUser(UserProfile profile) {
        return profile.email() != null && profile.email().endsWith(DEMO_EMAIL_DOMAIN);
    }

    private void sendBotReply(String matchId, ActiveMatch match, String userMessage) {
        Set<String> requested = match.realUser.requestedSkills();
        Set<String> offered = match.demoUser.offeredSkills();
        Set<String> realOffered = match.realUser.offeredSkills();
        Set<String> demoRequested = match.demoUser.requestedSkills();

        String reply = demoLlmClient.generateReply(
                        userMessage,
                        match.realUser.name(),
                        requested,
                        offered,
                        realOffered,
                        demoRequested
                )
                .orElseGet(() -> demoLlmClient.getFallbackReply());

        ChatMessage botMessage = new ChatMessage(match.demoUser.id(), reply, Instant.now());
        messagingTemplate.convertAndSend("/topic/chat/" + matchId, botMessage);

        if (match != null) {
            match.lastBotMessage = Instant.now();
        }
    }

    private static class ActiveMatch {
        private final UserProfile demoUser;
        private final UserProfile realUser;
        private Instant lastActivity;
        private Instant lastBotMessage;

        private ActiveMatch(UserProfile demoUser, UserProfile realUser, Instant lastActivity) {
            this.demoUser = demoUser;
            this.realUser = realUser;
            this.lastActivity = lastActivity;
            this.lastBotMessage = Instant.EPOCH;
        }

        private boolean canSendBotReply() {
            return Duration.between(lastBotMessage, Instant.now()).compareTo(MIN_BOT_REPLY_GAP) > 0;
        }
    }
}
