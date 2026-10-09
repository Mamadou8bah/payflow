package com.mamadou.sutura.risk.dto;

import com.mamadou.sutura.risk.enums.RiskRuleType;

import java.math.BigDecimal;

public record RiskRuleResult(
    RiskRuleType ruleType,
    boolean triggered,
    BigDecimal riskScore,
    String reason
) {}
