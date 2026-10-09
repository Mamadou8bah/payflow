package com.mamadou.sutura.risk.dto;

import com.mamadou.sutura.risk.enums.RiskLevel;

import java.math.BigDecimal;
import java.util.List;

public record RiskEvaluationResult(
    Long transactionId,
    Long walletId,
    RiskLevel riskLevel,
    BigDecimal riskScore,
    List<RiskRuleResult> triggeredRules,
    boolean shouldBlock,
    String summary
) {}
