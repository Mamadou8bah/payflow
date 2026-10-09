package com.mamadou.sutura.risk.dto;

import com.mamadou.sutura.risk.enums.RiskLevel;
import com.mamadou.sutura.risk.enums.RiskRuleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RiskFlagResponse(
    Long id,
    Long walletId,
    Long transactionId,
    RiskLevel riskLevel,
    RiskRuleType triggeringRule,
    BigDecimal riskScore,
    String reason,
    boolean resolved,
    String resolutionAction,
    LocalDateTime flaggedAt,
    LocalDateTime resolvedAt
) {}
