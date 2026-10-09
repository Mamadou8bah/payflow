package com.mamadou.sutura.webhook.sentinel;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequiredArgsConstructor
public class SentinelWebhookController {
    private final SentinelWebhookService service;
    @PostMapping("/api/v1/webhooks/sentinel")
    public ResponseEntity<Void> receive(@RequestHeader(value="X-Sentinel-Event-Id", required=false) String eventId,
            @RequestHeader(value="X-Sentinel-Timestamp", required=false) String timestamp,
            @RequestHeader(value="X-Sentinel-Signature", required=false) String signature,@RequestBody byte[] payload) {
        service.process(eventId,timestamp,signature,payload);
        return ResponseEntity.noContent().build();
    }
}
