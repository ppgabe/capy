package dev.ailuruslabs.capybackend.controller;

import dev.ailuruslabs.capybackend.domain.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;

@Controller
@RequiredArgsConstructor
class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{matchId}")
    public void routeMessage(@DestinationVariable String matchId, @Payload ChatMessage message) {
        // Stamp the server time to ensure chronological ordering
        ChatMessage stampedMessage = new ChatMessage(
                message.senderId(),
                message.content(),
                Instant.now()
        );

        // Broadcast the message to anyone subscribed to this specific match's chat room
        messagingTemplate.convertAndSend("/topic/chat/" + matchId, stampedMessage);
    }
}
