package com.mamadou.sutura.transaction.dto;

import com.mamadou.sutura.transaction.enums.TransactionStatus;
import com.mamadou.sutura.transaction.enums.TransactionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransactionFilterRequest {
    private Long walletId;
    private TransactionType type;
    private TransactionStatus status;
    private LocalDateTime from;
    private LocalDateTime to;
}
