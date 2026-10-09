package com.mamadou.sutura.kyc.service;

import com.mamadou.sutura.kyc.repository.KycVerificationRepository;
import com.mamadou.sutura.wallet.exception.WalletOperationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KycPrivilegeService {
    private final KycVerificationRepository repository;

    public void requireVerified(Long userId) {
        if (userId == null || !repository.findById(userId)
                .map(record -> "VERIFIED".equals(record.getStatus())).orElse(false)) {
            throw new WalletOperationException("Verified identity is required for this wallet privilege");
        }
    }
}
