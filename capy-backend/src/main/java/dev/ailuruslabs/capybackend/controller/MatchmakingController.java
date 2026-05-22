package dev.ailuruslabs.capybackend.controller;

import dev.ailuruslabs.capybackend.application.QueueService;
import dev.ailuruslabs.capybackend.domain.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matchmaking")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
class MatchmakingController {
    private final QueueService queueService;

    public record QueueJoinResponse(String matchQueueTopic) {}

    @PostMapping("/join")
    public ResponseEntity<QueueJoinResponse> joinQueue(@RequestBody UserProfile profile) {
        queueService.addUserToQueue(profile);

        return ResponseEntity.ok(new QueueJoinResponse("/queue/match/" + profile.id()));
    }

    @PostMapping("/leave")
    public ResponseEntity<Void> leaveQueue(@RequestBody UserProfile profile) {
        queueService.removeUserFromQueue(profile);
        return ResponseEntity.ok().build();
    }
}
