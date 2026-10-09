package com.mamadou.sutura.merchant.dto;

import java.time.LocalDateTime;

public record MerchantRegistrationStepResponse(
        String registrationToken,
        int stage,
        String message,
        LocalDateTime expiresAt
) {}
