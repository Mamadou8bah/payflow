package com.mamadou.sutura.reconciliation.dto;

import com.mamadou.sutura.reconciliation.enums.ReconciliationStatus;
import com.mamadou.sutura.reconciliation.enums.ReconciliationType;

import java.time.LocalDateTime;

public record ReconciliationReportResponse(
    Long id,
    ReconciliationType reconciliationType,
    ReconciliationStatus status,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    long totalRecordsChecked,
    long mismatchesFound,
    long mismatchesResolved,
    String summary,
    String errorDetails,
    String triggeredBy,
    boolean automated,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
