package com.mamadou.sutura.kyc.controller;

import com.mamadou.sutura.common.response.ApiResponse;
import com.mamadou.sutura.kyc.dto.KycStatusResponse;
import com.mamadou.sutura.kyc.service.KycService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/kyc")
@PreAuthorize("hasAnyRole('USER','MERCHANT')")
@RequiredArgsConstructor
public class KycController {
    private final KycService service;

    @GetMapping
    public ApiResponse<KycStatusResponse> status() {
        return ApiResponse.success("Identity verification status", service.status());
    }

    @PostMapping("/sessions")
    public ApiResponse<KycStatusResponse> start() {
        return ApiResponse.success("Identity verification session", service.start());
    }

    @PostMapping("/refresh")
    public ApiResponse<KycStatusResponse> refresh() {
        return ApiResponse.success("Identity verification status refreshed", service.refresh());
    }
}
