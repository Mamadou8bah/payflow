package com.mamadou.sutura.webhook.service;

import com.mamadou.sutura.webhook.validator.WebhookSignatureValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WebhookSignatureService {
    private final WebhookSignatureValidator validator;
    public boolean verifySignature(String payload, String signature) {
        return validator.isValid(payload, signature);
    }
}
