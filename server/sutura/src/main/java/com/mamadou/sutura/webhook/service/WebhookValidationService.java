package com.mamadou.sutura.webhook.service;

import com.mamadou.sutura.webhook.validator.WebhookSignatureValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookValidationService {

    private final WebhookSignatureValidator webhookSignatureValidator;

    @Value("${sutura.webhooks.modempay.signature-header:x-modem-signature}")
    private String signatureHeader;

    public boolean hasValidSignature(String rawPayload, HttpHeaders headers) {
        return webhookSignatureValidator.isValid(rawPayload, headers.getFirst(signatureHeader));
    }
}
