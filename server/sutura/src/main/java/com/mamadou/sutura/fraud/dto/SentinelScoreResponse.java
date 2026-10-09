package com.mamadou.sutura.fraud.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SentinelScoreResponse(
        String externalTransactionId,
        String transactionId,
        Double anomalyScore,
        String recommendation,
        List<String> ruleFlags,
        String explanation,
        List<Map<String, Object>> shapTopFeatures,
        String externalCustomerId,
        String caseId) {}
