package dev.ailuruslabs.capybackend.controller;

import dev.ailuruslabs.capybackend.application.QueueService;
import dev.ailuruslabs.capybackend.domain.UserProfile;
import dev.ailuruslabs.capybackend.repository.UserProfileRepository;
import dev.ailuruslabs.capybackend.service.DemoLlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
class DemoController {
    private static final String DEMO_EMAIL_DOMAIN = "demo.capy.local";
    private static final List<String> SKILLS = List.of(
            "Java",
            "Python",
            "React",
            "Spring",
            "SQL",
            "C++",
            "JavaScript",
            "UI Design",
            "Data Structures",
            "Algorithms",
            "DevOps",
            "Docker",
            "Machine Learning",
            "Product Management",
            "Public Speaking"
    );

    private final UserProfileRepository userProfileRepository;
    private final QueueService queueService;
    private final DemoLlmClient demoLlmClient;
    private final Random random = new Random();

    @PostMapping("/seed")
    public ResponseEntity<Void> seedDemoUsers(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestParam(defaultValue = "20") int count
    ) {
        if (principal == null) return ResponseEntity.status(401).build();

        int safeCount = Math.max(1, Math.min(count, 200));
        List<UserProfile> demoUsers = new ArrayList<>(safeCount);

        for (int i = 0; i < safeCount; i++) {
            demoUsers.add(buildDemoProfile());
        }

        var saved = userProfileRepository.saveAll(demoUsers);
        for (UserProfile profile : saved) {
            queueService.addUserToQueue(profile);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/clear")
    public ResponseEntity<Void> clearDemoUsers(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        List<UserProfile> demoUsers = userProfileRepository.findByEmailEndingWith(DEMO_EMAIL_DOMAIN);
        for (UserProfile profile : demoUsers) {
            queueService.removeUserFromQueue(profile.id());
        }
        userProfileRepository.deleteAll(demoUsers);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/llm/verify")
    public ResponseEntity<DemoLlmClient.VerificationResult> verifyLlm(
            @AuthenticationPrincipal OAuth2User principal
    ) {
        if (principal == null) return ResponseEntity.status(401).build();

        return ResponseEntity.ok(demoLlmClient.verifyConnection());
    }

    private UserProfile buildDemoProfile() {
        String id = null;
        String name = "Demo User";
        String email = "demo-" + UUID.randomUUID() + "@" + DEMO_EMAIL_DOMAIN;
        String picture = "demo";

        Set<String> offeredSkills = pickRandomSkills(2, 4);
        Set<String> requestedSkills = pickRandomSkills(2, 4);

        return new UserProfile(id, name, email, picture, offeredSkills, requestedSkills);
    }

    private Set<String> pickRandomSkills(int min, int max) {
        int count = min + random.nextInt(max - min + 1);
        Set<String> selected = new java.util.HashSet<>();
        while (selected.size() < count) {
            selected.add(SKILLS.get(random.nextInt(SKILLS.size())));
        }
        return selected;
    }
}
