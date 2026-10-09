package com.mamadou.sutura.kyc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SentinelKycResponse(Long sessionId, String status, String externalCustomerId,
        Long customerId, String kycStatus, Double faceMatchScore, Double livenessScore,
        String explanation, String hostedUrl, Instant expiresAt) {}
