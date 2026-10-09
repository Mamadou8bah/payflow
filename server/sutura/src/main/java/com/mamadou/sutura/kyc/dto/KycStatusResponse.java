package com.mamadou.sutura.kyc.dto;

import com.mamadou.sutura.kyc.entity.KycVerification;
import java.time.Instant;

public record KycStatusResponse(String status, Long sessionId, String hostedUrl,
        String explanation, Instant expiresAt, Instant updatedAt) {
    public static KycStatusResponse from(KycVerification v) {
        if (v == null) return new KycStatusResponse("NOT_STARTED", null, null, null, null, null);
        String status = "PENDING".equals(v.getStatus()) && !v.getExpiresAt().isAfter(Instant.now())
                ? "EXPIRED" : v.getStatus();
        return new KycStatusResponse(status, v.getSentinelSessionId(), v.getHostedUrl(),
                v.getExplanation(), v.getExpiresAt(), v.getUpdatedAt());
    }
}
