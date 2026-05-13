package dev.ailuruslabs.capybackend.domain;

import java.util.Objects;
import java.util.Set;

public record UserProfile(
        String id,
        String username,
        Set<String> offeredSkills,
        Set<String> requestedSkills
) {
    public UserProfile {
        Objects.requireNonNull(id);
        Objects.requireNonNull(username);
        Objects.requireNonNull(offeredSkills);
        Objects.requireNonNull(requestedSkills);

        if (id.isBlank()) {
            throw new IllegalArgumentException("id cannot be blank");
        }

        if (username.isBlank()) {
            throw new IllegalArgumentException("username cannot be blank");
        }
    }
}
