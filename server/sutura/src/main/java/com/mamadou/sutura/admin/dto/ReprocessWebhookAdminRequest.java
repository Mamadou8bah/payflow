package com.mamadou.sutura.admin.dto;

public record ReprocessWebhookAdminRequest(
    Long webhookId,
    String reason
) {}
