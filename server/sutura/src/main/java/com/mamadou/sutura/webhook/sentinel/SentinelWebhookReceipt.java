package com.mamadou.sutura.webhook.sentinel;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;
import org.springframework.data.domain.Persistable;

@Entity @Table(name="sentinel_webhook_receipts") @Getter @NoArgsConstructor
public class SentinelWebhookReceipt implements Persistable<String> {
    @Id @Column(length=64) private String id;
    @Column(nullable=false,length=64) private String payloadHash;
    @Column(nullable=false) private Instant processedAt;
    public SentinelWebhookReceipt(String id,String payloadHash) {
        this.id=id; this.payloadHash=payloadHash; this.processedAt=Instant.now();
    }
    @Override @Transient public boolean isNew() { return true; }
}
