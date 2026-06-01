package dev.ailuruslabs.capybackend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DemoLlmClient {
    private static final Logger logger = LoggerFactory.getLogger(DemoLlmClient.class);
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Random random = new Random();

    @Value("${demo.llm.enabled:false}")
    private boolean enabled;

    @Value("${demo.llm.cloudflare.accountId:}")
    private String accountId;

    @Value("${demo.llm.cloudflare.apiToken:}")
    private String apiToken;

    @Value("${demo.llm.model:@cf/meta/llama-3-8b-instruct}")
    private String model;

    @Value("${demo.llm.systemPrompt:You are a friendly study buddy. Reply in 1-2 sentences.}")
    private String systemPrompt;

    public boolean isEnabled() {
        return enabled && !accountId.isBlank() && !apiToken.isBlank();
    }

    public VerificationResult verifyConnection() {
        if (!enabled) {
            return new VerificationResult(false, "disabled", "DEMO_LLM_ENABLED is false");
        }
        if (accountId.isBlank() || apiToken.isBlank()) {
            return new VerificationResult(false, "missing_config", "Missing Cloudflare account id or API token");
        }

        Optional<String> reply = generateReply(
                "Reply with OK.",
                "demo",
                Set.of("any"),
                Set.of("any"),
                Set.of("any"),
                Set.of("any")
        );

        if (reply.isEmpty()) {
            return new VerificationResult(false, "request_failed", "Workers AI call failed or returned no response");
        }

        return new VerificationResult(true, "ok", reply.get());
    }

    public Optional<String> generateReply(String userMessage, String userName, Set<String> userRequested, Set<String> demoOffered) {
        return generateReply(
                userMessage,
                userName,
                userRequested,
                demoOffered,
                Set.of(),
                Set.of()
        );
    }

    public Optional<String> generateReply(
            String userMessage,
            String userName,
            Set<String> userRequested,
            Set<String> demoOffered,
            Set<String> userOffered,
            Set<String> demoRequested
    ) {
        if (!isEnabled()) {
            logger.warn("Demo LLM disabled or missing config (enabled={}, accountIdSet={}, apiTokenSet={})",
                    enabled,
                    !accountId.isBlank(),
                    !apiToken.isBlank());
            return Optional.empty();
        }

        try {
            String url = "https://api.cloudflare.com/client/v4/accounts/" + accountId + "/ai/run/" + model;
            String context = buildContext(userName, userRequested, demoOffered, userOffered, demoRequested);
            Map<String, Object> payload = Map.of(
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt + "\n" + context),
                            Map.of("role", "user", "content", userMessage)
                    ),
                    "max_tokens", 80
            );

            String body = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(8))
                    .header("Authorization", "Bearer " + apiToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                logger.warn("Workers AI request failed with status {} and body: {}",
                        response.statusCode(),
                        trim(response.body()));
                return Optional.empty();
            }

            CloudflareResponse parsed = objectMapper.readValue(response.body(), CloudflareResponse.class);
            if (parsed == null || parsed.result == null || parsed.result.response == null) {
                return Optional.empty();
            }

            return Optional.of(parsed.result.response.trim());
        } catch (Exception e) {
            logger.warn("Workers AI request failed", e);
            return Optional.empty();
        }
    }

    private String trim(String body) {
        if (body == null) return "(empty)";
        if (body.length() <= 500) return body;
        return body.substring(0, 500) + "...";
    }

    private String buildContext(
            String userName,
            Set<String> userRequested,
            Set<String> demoOffered,
            Set<String> userOffered,
            Set<String> demoRequested
    ) {
        String safeName = (userName == null || userName.isBlank()) ? "the learner" : userName;
        String requested = userRequested == null || userRequested.isEmpty()
                ? "(not provided)"
                : String.join(", ", userRequested);
        String offered = demoOffered == null || demoOffered.isEmpty()
                ? "(not provided)"
                : String.join(", ", demoOffered);
        String learnerOffered = userOffered == null || userOffered.isEmpty()
                ? "(not provided)"
                : String.join(", ", userOffered);
        String demoWanted = demoRequested == null || demoRequested.isEmpty()
                ? "(not provided)"
                : String.join(", ", demoRequested);

        return "Context: You are chatting in Capy, a peer learning app. "
                + "The learner is " + safeName + ". "
                + "They want to learn: " + requested + ". "
                + "They can teach: " + learnerOffered + ". "
                + "You can teach: " + offered + ". "
                + "You want to learn: " + demoWanted + ".";
    }

    public Optional<String> generateReply(String userMessage) {
        return generateReply(userMessage, null, Set.of(), Set.of(), Set.of(), Set.of());
    }

    public String getFallbackReply() {
        String[] replies = {
                "That makes sense. Want to try a small example together?",
                "Nice! I can help. What part should we tackle first?",
                "Cool topic. I think a quick recap could help.",
                "Let us break it into steps and keep it simple.",
                "Got it. I can suggest a few tips if you want."
        };
        return replies[random.nextInt(replies.length)];
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class CloudflareResponse {
        public Result result;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Result {
        public String response;
    }

    public record VerificationResult(boolean ok, String status, String detail) {}
}
