package com.mamadou.sutura.reconciliation.service;

import com.mamadou.sutura.ledger.exception.LedgerException;
import com.mamadou.sutura.ledger.service.LedgerBalanceComputationService;
import com.mamadou.sutura.reconciliation.entity.ReconciliationMismatch;
import com.mamadou.sutura.reconciliation.entity.ReconciliationReport;
import com.mamadou.sutura.reconciliation.repository.ReconciliationMismatchRepository;
import com.mamadou.sutura.wallet.entity.Wallet;
import com.mamadou.sutura.wallet.entity.WalletTransaction;
import com.mamadou.sutura.wallet.repository.WalletRepository;
import com.mamadou.sutura.wallet.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletReconciliationService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final LedgerBalanceComputationService ledgerBalanceComputationService;
    private final ReconciliationMismatchRepository mismatchRepository;



    private static final int BATCH_SIZE = 100;

    @Transactional
    public void reconcileWalletVsLedger(ReconciliationReport report) {
        log.info("Starting wallet vs ledger reconciliation for report {}", report.getId());

        List<Wallet> wallets = walletRepository.findAll();
        wallets.sort(Comparator.comparing(Wallet::getId));
        long mismatches = 0;
        for (Wallet candidate : wallets) {
            // Keep lazy entities, report writes and reads in this transaction.
            Wallet wallet = walletRepository.findWithLockById(candidate.getId()).orElseThrow();
            BigDecimal ledgerBalance = computeLedgerBalance(wallet);
            BigDecimal transactionBalance = computeTransactionBalance(wallet.getId());
            if (ledgerBalance.compareTo(transactionBalance) != 0) {
                createMismatch(report, wallet, transactionBalance, ledgerBalance);
                mismatches++;
            }
        }
        report.setTotalRecordsChecked(wallets.size());
        report.setMismatchesFound(mismatches);
    }

    private BigDecimal computeLedgerBalance(Wallet wallet) {
        if (wallet.getLedgerAccount() == null) {
            throw new LedgerException("Wallet " + wallet.getId() + " has no ledger account");
        }
        return ledgerBalanceComputationService.computeBalance(wallet.getLedgerAccount());
    }

    private BigDecimal computeTransactionBalance(Long walletId) {
        List<WalletTransaction> transactions = walletTransactionRepository.findByWalletId(walletId);
        BigDecimal balance = BigDecimal.ZERO;
        for (WalletTransaction txn : transactions) {
            if ("CREDIT".equalsIgnoreCase(txn.getTransactionType())) {
                balance = balance.add(txn.getAmount());
            } else if ("DEBIT".equalsIgnoreCase(txn.getTransactionType())) {
                balance = balance.subtract(txn.getAmount());
            }
        }
        return balance;
    }

    private void createMismatch(
            ReconciliationReport report,
            Wallet wallet,
            BigDecimal transactionBalance,
            BigDecimal ledgerBalance
    ) {
        BigDecimal variance = ledgerBalance.subtract(transactionBalance).abs();

        ReconciliationMismatch mismatch = ReconciliationMismatch.builder()
            .report(report)
            .mismatchType("BALANCE_VARIANCE")
            .entityId(wallet.getId())
            .entityType("WALLET")
            .expectedValue(ledgerBalance.toPlainString())
            .actualValue(transactionBalance.toPlainString())
            .variance(variance)
            .description("Ledger balance does not match wallet transaction sum: ledger="
                + ledgerBalance + ", transactions=" + transactionBalance)
            .resolved(false)
            .build();

        mismatchRepository.save(mismatch);
        log.warn("Mismatch found for wallet {}: ledger={}, transactions={}",
            wallet.getId(), ledgerBalance, transactionBalance);
    }

    private record ReconciliationResult(boolean hasMismatch, Wallet wallet) {
    }
}
