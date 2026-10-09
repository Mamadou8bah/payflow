package com.mamadou.sutura.ledger.dto;

import com.mamadou.sutura.ledger.enums.LedgerAccountType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LedgerAccountResponse {
    private Long id;
    private String code;
    private String name;
    private LedgerAccountType type;
    private String currency;
    private boolean active;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
