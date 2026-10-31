package com.escala.authservice.controller;

import com.escala.authservice.service.ChatbotAssistantService;
import com.escala.authservice.service.CurrentUserService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks/chatbot")
@RequiredArgsConstructor
public class ChatbotWebhookController {
    @Value("${application.integrations.chatbot.webhook-secret:}")
    private String webhookSecret;

    private final ChatbotAssistantService chatbotAssistantService;
    private final CurrentUserService currentUserService;

    @Data
    public static class ChatbotWebhookRequest {
        private String senderEmail;
        private String message;
        private String phone;
    }

    @Data
    @Builder
    public static class ChatbotWebhookResponse {
        private String status;
        private String action;
        private String response;
        private List<String> suggestions;
    }

    @PostMapping
    public ResponseEntity<ChatbotWebhookResponse> processWebhook(
            @RequestBody ChatbotWebhookRequest request,
            @RequestHeader(value = "X-Webhook-Secret", required = false) String providedSecret,
            Authentication authentication
    ) {
        if (webhookSecret.isBlank() || !secretMatches(providedSecret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Webhook secret invalido");
        }
        UUID callerCompanyId = currentUserService.requireCurrentUser(authentication.getName()).getCompany().getId();

        ChatbotAssistantService.Reply reply = chatbotAssistantService.reply(
                callerCompanyId, request.getSenderEmail(), request.getMessage());

        return ResponseEntity.ok(ChatbotWebhookResponse.builder()
                .status("success")
                .action(reply.action())
                .response(reply.response())
                .suggestions(reply.suggestions())
                .build());
    }

    private boolean secretMatches(String providedSecret) {
        if (providedSecret == null) {
            return false;
        }
        return MessageDigest.isEqual(
                webhookSecret.getBytes(StandardCharsets.UTF_8),
                providedSecret.getBytes(StandardCharsets.UTF_8));
    }
}
