package com.mamadou.sutura.kyc.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "kyc_verifications")
@Data
@NoArgsConstructor
public class KycVerification {
    @Id
    private Long userId;
    @Column(nullable = false, unique = true)
    private Long sentinelSessionId;
    @Column(nullable = false)
    private String externalCustomerId;
    private Long sentinelCustomerId;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false, length = 2048)
    private String hostedUrl;
    @Column(length = 4096)
    private String explanation;
    @Column(nullable = false)
    private Instant expiresAt;
    @Column(nullable = false)
    private Instant updatedAt;
}
