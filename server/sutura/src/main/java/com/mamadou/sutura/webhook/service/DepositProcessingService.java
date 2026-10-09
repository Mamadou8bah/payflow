package com.mamadou.sutura.webhook.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mamadou.sutura.wallet.entity.Wallet;
import com.mamadou.sutura.wallet.exception.WalletOperationException;
import com.mamadou.sutura.webhook.dto.DepositWebhookRequest;
import com.mamadou.sutura.webhook.entity.WebhookEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DepositProcessingService {

    private final ObjectMapper objectMapper;
    private final ProviderDepositSettlementService settlement;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(WebhookEvent event) {
        DepositWebhookRequest request = parse(event.getRawPayload());
        validate(request);
        if (!isSuccessfulDeposit(request)) {
            event.setStatus("IGNORED");
            event.setFailureReason("Deposit status is not successful: " + request.status());
            return;
        }
        settlement.complete(request.reference(), request.walletId(), request.amount(), request.currency(), request.providerTransactionId());
    }

    public DepositWebhookRequest parse(String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, DepositWebhookRequest.class);
        } catch (Exception ex) {
            throw new WalletOperationException("Invalid deposit webhook payload");
        }
    }

    private void validate(DepositWebhookRequest request) {
        if (request.eventId() == null || request.eventId().isBlank()) {
            throw new WalletOperationException("Webhook event id is required");
        }
        if (request.walletId() == null) {
            throw new WalletOperationException("Wallet id is required");
        }
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletOperationException("Deposit amount must be greater than zero");
        }
        if (request.currency() == null || request.currency().isBlank()) {
            throw new WalletOperationException("Deposit currency is required");
        }
    }

    private boolean isSuccessfulDeposit(DepositWebhookRequest request) {
        if (request.status() == null) {
            return false;
        }
        String status = request.status().trim().toUpperCase();
        return status.equals("SUCCESS") || status.equals("SUCCEEDED") || status.equals("COMPLETED") || status.equals("PAID");
    }

}
