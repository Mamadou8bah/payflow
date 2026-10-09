package com.mamadou.sutura.merchant.dto;

import com.mamadou.sutura.auth.dto.AuthResponse;

public record MerchantRegistrationCompleteResponse(
        AuthResponse auth,
        String businessName,
        String verificationStatus,
        String message
) {}
