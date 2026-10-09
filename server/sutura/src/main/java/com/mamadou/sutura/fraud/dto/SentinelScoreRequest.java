package com.mamadou.sutura.fraud.dto;

import java.math.BigDecimal;
import java.time.Instant;

/** Matches Sentinel's tenant-authenticated transaction scoring contract. */
public record SentinelScoreRequest(
        String externalCustomerId,
        String externalTransactionId,
        BigDecimal amount,
        String currency,
        Instant timestamp,
        String channel,
        String location,
        String counterparty) {}
