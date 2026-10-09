package com.mamadou.sutura.transfer.service;

import com.mamadou.sutura.ledger.dto.LedgerPostingRequest;
import com.mamadou.sutura.ledger.dto.LedgerTraceResponse;
import com.mamadou.sutura.ledger.enums.LedgerPostingSide;
import com.mamadou.sutura.ledger.service.LedgerService;
import com.mamadou.sutura.transaction.entity.Transaction;
import com.mamadou.sutura.transaction.service.TransactionService;
import com.mamadou.sutura.transfer.dto.TransferRequest;
import com.mamadou.sutura.transfer.dto.TransferValidationResult;
import com.mamadou.sutura.wallet.entity.Wallet;
import com.mamadou.sutura.wallet.service.WalletLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferExecutionService {

    private final LedgerService ledgerService;
    private final TransactionService transactionService;
    private final WalletLimitService walletLimitService;
    private final com.mamadou.sutura.wallet.repository.WalletTransactionRepository walletTransactions;



    @org.springframework.transaction.annotation.Transactional
    public Transaction execute(TransferRequest request, TransferValidationResult validationResult) {
        Wallet sourceWallet = validationResult.getSourceWallet();
        Wallet destinationWallet = validationResult.getDestinationWallet();

        Transaction transaction = transactionService.createPendingTransfer(
                sourceWallet,
                destinationWallet,
                validationResult.getInitiatedBy(),
                request.getAmount(),
                sourceWallet.getCurrency(),
                request.getDescription(),
                request.getReference()
        );

        return executePending(transaction);
    }

    @org.springframework.transaction.annotation.Transactional
    public Transaction executePending(Transaction transaction) {
        // Posting, usage, and product status share the caller's database transaction.
        Wallet source = transaction.getSourceWallet();
        Wallet destination = transaction.getDestinationWallet();
        LedgerTraceResponse trace = ledgerService.recordFinancialTransaction(ledgerPostingRequest(
                transaction.getReference(), transaction.getReference(), transaction.getCurrency(),
                transaction.getDescription(),
                entry(source.getLedgerAccount().getCode(), LedgerPostingSide.DEBIT, transaction.getAmount(), "Transfer debit"),
                entry(destination.getLedgerAccount().getCode(), LedgerPostingSide.CREDIT, transaction.getAmount(), "Transfer credit")));
        walletTransactions.save(com.mamadou.sutura.wallet.entity.WalletTransaction.builder().wallet(source)
                .amount(transaction.getAmount()).transactionType("DEBIT").reference(transaction.getReference()).description(transaction.getDescription()).build());
        walletTransactions.save(com.mamadou.sutura.wallet.entity.WalletTransaction.builder().wallet(destination)
                .amount(transaction.getAmount()).transactionType("CREDIT").reference(transaction.getReference()).description(transaction.getDescription()).build());
        walletLimitService.trackDebitUsage(source.getWalletLimit(), transaction.getAmount());
        return transactionService.markCompleted(transaction, trace.getTraceId());
    }

    private LedgerPostingRequest ledgerPostingRequest(
            String traceId,
            String externalReference,
            String currency,
            String description,
            LedgerPostingRequest.Entry... entries
    ) {
        LedgerPostingRequest request = new LedgerPostingRequest();
        request.setTraceId(traceId);
        request.setExternalReference(externalReference);
        request.setCurrency(currency);
        request.setDescription(description);
        request.setPostings(List.of(entries));
        return request;
    }

    private LedgerPostingRequest.Entry entry(
            String accountCode,
            LedgerPostingSide side,
            BigDecimal amount,
            String description
    ) {
        LedgerPostingRequest.Entry entry = new LedgerPostingRequest.Entry();
        entry.setAccountCode(accountCode);
        entry.setSide(side);
        entry.setAmount(amount);
        entry.setDescription(description);
        return entry;
    }
}
