package com.mamadou.sutura.admin.dto;

public record ReverseTransactionAdminRequest(
    Long transactionId,
    String reason,
    boolean refundToWallet
) {}
