package com.mamadou.sutura.fraud.controller;

import com.mamadou.sutura.common.response.ApiResponse;
import com.mamadou.sutura.fraud.client.FraudDetectionClient;
import com.mamadou.sutura.fraud.config.FraudDetectionProperties;
import com.mamadou.sutura.fraud.dto.FraudDecisionResponse;
import com.mamadou.sutura.fraud.entity.FraudEvaluationLog;
import com.mamadou.sutura.fraud.service.FraudDetectionService;
import com.mamadou.sutura.fraud.service.FraudEvaluationLogService;
import com.mamadou.sutura.user.entity.User;
import com.mamadou.sutura.wallet.entity.Wallet;
import com.mamadou.sutura.wallet.exception.WalletNotFoundException;
import com.mamadou.sutura.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudDetectionService fraudDetectionService;
    private final FraudDetectionClient fraudDetectionClient;
    private final FraudDetectionProperties properties;
    private final WalletRepository walletRepository;
    private final FraudEvaluationLogService fraudEvaluationLogService;

    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        boolean healthy = fraudDetectionClient.isHealthy();
        return ResponseEntity.ok(ApiResponse.success(
            "Fraud detection service status",
            Map.of(
                "enabled", properties.isEnabled(),
                "healthy", healthy,
                "baseUrl", properties.getBaseUrl()
            )
        ));
    }

    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> config() {
        return ResponseEntity.ok(ApiResponse.success(
            "Fraud detection configuration",
            Map.of(
                "enabled", properties.isEnabled(),
                "baseUrl", properties.getBaseUrl(),
                "timeoutMs", properties.getTimeoutMs(),
                "failOpen", false,
                "blockOnReview", true,
                "defaultCountry", properties.getDefaultCountry()
            )
        ));
    }

    @PostMapping("/score")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FraudDecisionResponse>> score(
            @RequestParam Long walletId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String transactionId
    ) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));
        User user = wallet.getUser();

        String txnId = transactionId != null && !transactionId.isBlank()
            ? transactionId
            : "admin-eval-" + System.currentTimeMillis();

        FraudDecisionResponse decision = fraudDetectionService.evaluateTransaction(
            txnId,
            user,
            wallet,
            amount,
            "admin_eval",
            null
        );

        return ResponseEntity.ok(ApiResponse.success("Fraud score computed", decision));
    }

    @GetMapping("/records/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> recordStats() {
        return ResponseEntity.ok(ApiResponse.success(
            "Fraud evaluation log statistics",
            fraudEvaluationLogService.getStats()
        ));
    }

    @PutMapping("/records/{logId}/label")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FraudEvaluationLog>> labelRecord(
            @PathVariable Long logId,
            @RequestParam boolean confirmedFraud,
            @RequestParam(defaultValue = "admin") String labelSource
    ) {
        FraudEvaluationLog labeled = fraudEvaluationLogService.labelEvaluation(logId, confirmedFraud, labelSource);
        return ResponseEntity.ok(ApiResponse.success("Fraud evaluation labeled", labeled));
    }
}
