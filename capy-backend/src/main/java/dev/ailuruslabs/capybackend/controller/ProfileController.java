package dev.ailuruslabs.capybackend.controller;

import dev.ailuruslabs.capybackend.domain.UserProfile;
import dev.ailuruslabs.capybackend.repository.UserProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserProfileRepository repository;

    public ProfileController(UserProfileRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfile> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        String email = principal.getAttribute("email");
        return repository.findByEmail(email)
                         .map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/skills")
    public ResponseEntity<UserProfile> updateSkills(
            @AuthenticationPrincipal OAuth2User principal,
            @RequestBody SkillUpdateRequest request) {

        if (principal == null) return ResponseEntity.status(401).build();

        String email = principal.getAttribute("email");
        return repository.findByEmail(email).map(existing -> {
            // Instantiate a new record to keep it immutable
            UserProfile updated = new UserProfile(
                    existing.id(),
                    existing.name(),
                    existing.email(),
                    existing.picture(),
                    request.offeredSkills(),
                    request.requestedSkills()
            );
            return ResponseEntity.ok(repository.save(updated));
        }).orElse(ResponseEntity.notFound().build());
    }
}

record SkillUpdateRequest(Set<String> offeredSkills, Set<String> requestedSkills) {}
