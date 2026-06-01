package dev.ailuruslabs.capybackend.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Objects;
import java.util.Set;

@Document(collection = "users")
public record UserProfile(
        @Id String id,
        String name,
        String email,
        String picture,
        Set<String> offeredSkills,
        Set<String> requestedSkills
) {
    public UserProfile {
        if (id != null && id.isBlank()) {
            throw new IllegalArgumentException("id cannot be blank");
        }

        Objects.requireNonNull(email);
        Objects.requireNonNull(name);
        Objects.requireNonNull(picture);
        Objects.requireNonNull(offeredSkills);
        Objects.requireNonNull(requestedSkills);
    }
}
