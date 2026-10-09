package com.mamadou.sutura.webhook.sentinel;

import com.mamadou.sutura.deposit.entity.Deposit;
import com.mamadou.sutura.deposit.repository.DepositRepository;
import com.mamadou.sutura.withdrawal.entity.Withdrawal;
import com.mamadou.sutura.withdrawal.repository.WithdrawalRepository;
import com.mamadou.sutura.transaction.entity.Transaction;
import com.mamadou.sutura.transaction.repository.TransactionRepository;
import com.mamadou.sutura.transaction.enums.TransactionStatus;
import com.mamadou.sutura.transaction.service.TransactionService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Matched decisions release provider deposits or authorise subsequent cash/payout completion. */
@Service
@RequiredArgsConstructor
public class SettlementReviewService {
    private final DepositRepository deposits;
    private final WithdrawalRepository withdrawals;
    private final TransactionRepository transactions;
    private final TransactionService transactionService;
    private final com.mamadou.sutura.webhook.service.ProviderDepositSettlementService providerDeposits;

    public void resolve(Transaction candidate, String decision) {
        Deposit deposit = deposits.findForCompletion(candidate.getReference()).orElse(null);
        Withdrawal withdrawal = deposit == null ? withdrawals.findForCompletion(candidate.getReference()).orElseThrow() : null;
        var tx = transactions.findForUpdateById(candidate.getId()).orElseThrow();
        if (tx.getStatus() != TransactionStatus.PENDING || !"review".equalsIgnoreCase(tx.getSentinelRecommendation())) return;
        if ("ESCALATE".equals(decision)) return;
        boolean approve = "APPROVE".equals(decision);
        if (approve) {
            tx.setSentinelRecommendation("allow"); tx.setSentinelApprovedAt(LocalDateTime.now()); tx.setFailureReason(null); transactions.save(tx);
        } else transactionService.markFailed(tx, "Sentinel case rejected");
        if (deposit != null) {
            deposit.setStatus(approve ? Deposit.DepositStatus.APPROVED : Deposit.DepositStatus.FAILED); deposits.save(deposit);
            if (approve && deposit.getPaymentMethod() != null) {
                providerDeposits.complete(deposit.getReference(), deposit.getWallet().getId(), deposit.getAmount(),
                        deposit.getCurrency(), deposit.getExternalPaymentId());
            }
        } else {
            withdrawal.setStatus(approve ? Withdrawal.WithdrawalStatus.APPROVED : Withdrawal.WithdrawalStatus.FAILED); withdrawals.save(withdrawal);
        }
    }
}
