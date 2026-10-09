package com.mamadou.sutura.ledger.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity
@Table(name = "ledger_traces")
@Getter
@NoArgsConstructor
public class LedgerTrace implements org.springframework.data.domain.Persistable<String> {
    @Id
    @Column(length = 80)
    private String id;
    @Column(nullable = false, length = 3)
    private String currency;
    @Column(nullable = false)
    private Instant createdAt;

    public LedgerTrace(String id, String currency) {
        this.id = id;
        this.currency = currency;
        this.createdAt = Instant.now();
    }

    @Override
    @Transient
    public boolean isNew() { return true; }
}
