package com.mamadou.sutura.transfer.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {
    /** Optional — primary wallet is used when omitted */
    private Long sourceWalletId;

    /** Used by internal collection flows; customer transfers use recipientPhone. */
    private Long destinationWalletId;

    @Size(max = 40, message = "Recipient phone number is too long")
    private String recipientPhone;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @jakarta.validation.constraints.Digits(integer = 13, fraction = 4)
    @jakarta.validation.constraints.DecimalMax("1000000000000")
    private BigDecimal amount;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @Size(max = 80, message = "Reference cannot exceed 80 characters")
    private String reference;

    @Size(max = 120, message = "Idempotency key cannot exceed 120 characters")
    private String idempotencyKey;
}
