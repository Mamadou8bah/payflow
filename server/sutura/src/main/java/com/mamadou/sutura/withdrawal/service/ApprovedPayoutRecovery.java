package com.mamadou.sutura.withdrawal.service;

import com.mamadou.sutura.withdrawal.entity.Withdrawal;
import com.mamadou.sutura.withdrawal.repository.WithdrawalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Only dispatch approved requests; unknown provider outcomes require reconciliation. */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "sutura.payments.approved-payout-recovery-enabled", havingValue = "true", matchIfMissing = true)
public class ApprovedPayoutRecovery {
    private final WithdrawalRepository withdrawals;
    private final WithdrawalService service;

    @Scheduled(fixedDelayString = "${sutura.payments.approved-payout-recovery-delay-ms:10000}", initialDelayString = "${sutura.payments.approved-payout-recovery-delay-ms:10000}")
    public void recover() {
        for (var withdrawal : withdrawals.findTop50ByStatusAndWithdrawalMethodIsNotNullOrderByCreatedAtAsc(Withdrawal.WithdrawalStatus.APPROVED)) {
            try { service.resumeApproved(withdrawal.getReference()); }
            catch (RuntimeException ex) {
                log.warn("Approved payout {} could not resume; retained for recovery", withdrawal.getReference());
            }
        }
    }
}
