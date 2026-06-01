package dev.ailuruslabs.capybackend.controller;

import dev.ailuruslabs.capybackend.application.QueueService;
import dev.ailuruslabs.capybackend.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matchmaking")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
class MatchmakingController {
    private final QueueService queueService;
    private final UserProfileRepository userProfileRepository;

    public record QueueJoinResponse(String matchQueueTopic) {}

    @PostMapping("/join")
    public ResponseEntity<QueueJoinResponse> joinQueue(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        String email = principal.getAttribute("email");
        return userProfileRepository.findByEmail(email)
                .map(profile -> {
                    queueService.addUserToQueue(profile);
                    return ResponseEntity.ok(new QueueJoinResponse("/queue/match/" + profile.id()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/leave")
    public ResponseEntity<Object> leaveQueue(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        String email = principal.getAttribute("email");
        return userProfileRepository.findByEmail(email)
                .map(profile -> {
                    queueService.removeUserFromQueue(profile.id());
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
