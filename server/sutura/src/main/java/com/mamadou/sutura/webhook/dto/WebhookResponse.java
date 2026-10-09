package com.mamadou.sutura.webhook.dto;

public record WebhookResponse(
        boolean accepted,
        Long eventId,
        String status,
        String message
) {
}
