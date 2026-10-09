package com.mamadou.sutura.webhook.dto;

import jakarta.validation.constraints.NotNull;

public record WebhookReplayRequest(
        @NotNull Long webhookEventId
) {
}
