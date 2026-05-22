package dev.ailuruslabs.capybackend.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record ChatMessage(
        String senderId,
        String content,
        Instant timestamp
) {
}
