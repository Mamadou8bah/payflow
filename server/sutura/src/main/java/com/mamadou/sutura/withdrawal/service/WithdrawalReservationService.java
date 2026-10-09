package com.mamadou.sutura.withdrawal.service;

import com.mamadou.sutura.ledger.dto.LedgerPostingRequest;
import com.mamadou.sutura.ledger.enums.*;
import com.mamadou.sutura.ledger.service.*;
import com.mamadou.sutura.transaction.enums.TransactionStatus;
import com.mamadou.sutura.transaction.repository.TransactionRepository;
import com.mamadou.sutura.transaction.service.TransactionService;
import com.mamadou.sutura.wallet.entity.Wallet;
import com.mamadou.sutura.wallet.enums.WalletStatus;
import com.mamadou.sutura.wallet.exception.WalletOperationException;
import com.mamadou.sutura.wallet.repository.WalletRepository;
import com.mamadou.sutura.wallet.service.WalletLimitService;
import com.mamadou.sutura.withdrawal.entity.Withdrawal;
import com.mamadou.sutura.withdrawal.repository.WithdrawalRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** A pending provider payout moves spendable liability into a reserved liability. */
@Service
@RequiredArgsConstructor
public class WithdrawalReservationService {
    private final WalletRepository wallets;
    private final WalletLimitService limits;
    private final LedgerBalanceComputationService balances;
    private final LedgerAccountService accounts;
    private final LedgerService ledger;
    private final WithdrawalRepository withdrawals;
    private final TransactionRepository transactions;
    private final TransactionService transactionService;
    private final com.mamadou.sutura.wallet.repository.WalletTransactionRepository walletTransactions;

    @Transactional
    public void reserve(Withdrawal withdrawal) {
        Wallet wallet = wallets.findWithLockById(withdrawal.getWallet().getId()).orElseThrow();
        if (wallet.getStatus() != WalletStatus.ACTIVE) throw new WalletOperationException("Wallet is not active");
        limits.validateDebit(wallet, withdrawal.getAmount());
        if (balances.computeBalance(wallet.getLedgerAccount()).compareTo(withdrawal.getAmount()) < 0)
            throw new WalletOperationException("Insufficient wallet balance");
        String reserved = reserveAccount(withdrawal.getCurrency());
        String trace = "hold:" + withdrawal.getReference();
        post(trace, withdrawal, wallet.getLedgerAccount().getCode(), reserved);
        recordMovement(wallet, withdrawal, trace, "DEBIT");
        limits.trackDebitUsage(wallet.getWalletLimit(), withdrawal.getAmount());
        withdrawal.setReservationTraceId(trace);
        withdrawal.setStatus(Withdrawal.WithdrawalStatus.PROCESSING);
        withdrawals.save(withdrawal);
    }

    @Transactional
    public void confirm(String reference, BigDecimal amount, String currency, String providerId) {
        Withdrawal withdrawal = withdrawals.findForCompletion(reference).orElseThrow(() -> new WalletOperationException("Unknown payout reference"));
        match(withdrawal, amount, currency, providerId);
        if (withdrawal.getStatus() == Withdrawal.WithdrawalStatus.COMPLETED) return;
        requirePendingReservation(withdrawal);
        String cash = accounts.getOrCreateAccountEntity("MODEMPAY:CLEARING:" + currency.toUpperCase(),
                "ModemPay clearing", LedgerAccountType.ASSET, currency).getCode();
        String trace = "settle:" + withdrawal.getReference();
        post(trace, withdrawal, reserveAccount(currency), cash);
        var tx = transactions.findForUpdateByReference(withdrawal.getReference()).orElseThrow();
        if (tx.getStatus() != TransactionStatus.PENDING) throw new WalletOperationException("Payout product is not pending");
        // The hold is the trace containing the actual wallet debit; settlement has its own trace.
        transactionService.markCompleted(tx, withdrawal.getReservationTraceId());
        withdrawal.setSettlementTraceId(trace);
        withdrawal.setExternalPaymentId(providerId);
        withdrawal.setStatus(Withdrawal.WithdrawalStatus.COMPLETED);
        withdrawal.setCompletedAt(LocalDateTime.now());
        withdrawals.save(withdrawal);
    }

    @Transactional
    public void release(String reference, BigDecimal amount, String currency, String providerId, String reason) {
        Withdrawal withdrawal = withdrawals.findForCompletion(reference).orElseThrow(() -> new WalletOperationException("Unknown payout reference"));
        match(withdrawal, amount, currency, providerId);
        if (withdrawal.getStatus() == Withdrawal.WithdrawalStatus.FAILED && withdrawal.getReleaseTraceId() != null) return;
        requirePendingReservation(withdrawal);
        Wallet wallet = wallets.findWithLockById(withdrawal.getWallet().getId()).orElseThrow();
        String trace = "release:" + withdrawal.getReference();
        post(trace, withdrawal, reserveAccount(currency), wallet.getLedgerAccount().getCode());
        recordMovement(wallet, withdrawal, trace, "CREDIT");
        var tx = transactions.findForUpdateByReference(withdrawal.getReference()).orElseThrow();
        transactionService.markFailed(tx, reason == null ? "Provider payout failed" : reason);
        withdrawal.setReleaseTraceId(trace);
        withdrawal.setExternalPaymentId(providerId);
        withdrawal.setStatus(Withdrawal.WithdrawalStatus.FAILED);
        withdrawal.setRejectionReason(reason);
        withdrawals.save(withdrawal);
        // Retain conservative usage counters: release may occur in a later usage window.
    }

    private void recordMovement(Wallet wallet, Withdrawal withdrawal, String reference, String side) {
        walletTransactions.save(com.mamadou.sutura.wallet.entity.WalletTransaction.builder().wallet(wallet)
                .amount(withdrawal.getAmount()).transactionType(side).reference(reference)
                .description("Provider payout " + reference).build());
    }

    private void requirePendingReservation(Withdrawal withdrawal) {
        if (withdrawal.getReservationTraceId() == null || withdrawal.getReleaseTraceId() != null
                || withdrawal.getStatus() != Withdrawal.WithdrawalStatus.PROCESSING)
            throw new WalletOperationException("Payout has no pending reservation");
    }

    private void match(Withdrawal withdrawal, BigDecimal amount, String currency, String providerId) {
        if (amount == null || amount.compareTo(withdrawal.getAmount()) != 0 || currency == null
                || !currency.equalsIgnoreCase(withdrawal.getCurrency()) || providerId == null || providerId.isBlank()
                || (withdrawal.getExternalPaymentId() != null && !providerId.equals(withdrawal.getExternalPaymentId())))
            throw new WalletOperationException("Provider payout does not match the stored request");
    }

    private String reserveAccount(String currency) {
        return accounts.getOrCreateAccountEntity("PAYOUT:RESERVED:" + currency.toUpperCase(),
                "Pending provider payout liability", LedgerAccountType.LIABILITY, currency).getCode();
    }

    private void post(String trace, Withdrawal withdrawal, String debit, String credit) {
        var request = new LedgerPostingRequest();
        request.setTraceId(trace);
        request.setCurrency(withdrawal.getCurrency());
        request.setExternalReference(withdrawal.getReference());
        request.setDescription("Provider payout: " + trace);
        request.setPostings(List.of(entry(debit, LedgerPostingSide.DEBIT, withdrawal.getAmount()),
                entry(credit, LedgerPostingSide.CREDIT, withdrawal.getAmount())));
        ledger.recordFinancialTransaction(request);
    }

    private LedgerPostingRequest.Entry entry(String account, LedgerPostingSide side, BigDecimal amount) {
        var entry = new LedgerPostingRequest.Entry();
        entry.setAccountCode(account); entry.setSide(side); entry.setAmount(amount);
        return entry;
    }
}
