package com.mamadou.sutura.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentOperationResponse {
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime completedAt;
    private String operation;
    private String reference;
    private Long walletId;
    private String walletName;
    private BigDecimal amount;
    private String currency;
    private String status;
    private Long userId;
    private String merchantName;
}
