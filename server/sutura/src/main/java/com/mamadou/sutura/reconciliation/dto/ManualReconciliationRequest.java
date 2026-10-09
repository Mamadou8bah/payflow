package com.mamadou.sutura.reconciliation.dto;

import com.mamadou.sutura.reconciliation.enums.ReconciliationType;

public record ManualReconciliationRequest(
    ReconciliationType reconciliationType,
    String reason,
    Long walletId
) {}
