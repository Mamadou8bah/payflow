package com.mamadou.sutura.transfer.dto;

import com.mamadou.sutura.transaction.dto.TransactionResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferResponse {
    private TransactionResponse transaction;
    private boolean idempotentReplay;
}
