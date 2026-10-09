package com.mamadou.sutura.idempotency.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mamadou.sutura.wallet.exception.WalletOperationException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class PaymentRequestIdentity {
    private final ObjectMapper mapper = new ObjectMapper();
    public String key(String operation, Long actorId, String rawKey) {
        if (rawKey == null || rawKey.isBlank()) return null;
        if (rawKey.length() > 128) throw new WalletOperationException("Idempotency key is too long");
        return operation + ":" + actorId + ":" + hash(rawKey.trim());
    }
    public String fingerprint(Object... fields) {
        try { return hash(mapper.writeValueAsString(fields)); }
        catch (Exception e) { throw new WalletOperationException("Unable to identify payment request"); }
    }
    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) { throw new WalletOperationException("Unable to identify payment request"); }
    }
}
