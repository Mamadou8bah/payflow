package com.mamadou.sutura.webhook.sentinel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mamadou.sutura.kyc.service.KycService;
import com.mamadou.sutura.transaction.enums.TransactionStatus;
import com.mamadou.sutura.transaction.repository.TransactionRepository;
import com.mamadou.sutura.transaction.service.TransactionService;
import com.mamadou.sutura.transfer.service.TransferExecutionService;
import com.mamadou.sutura.transfer.service.TransferValidationService;
import com.mamadou.sutura.wallet.exception.WalletOperationException;
import com.mamadou.sutura.ledger.exception.LedgerException;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service @RequiredArgsConstructor
public class SentinelWebhookService {
    private final SentinelWebhookVerifier verifier;
    private final SentinelWebhookProperties properties;
    private final SentinelWebhookReceiptRepository receipts;
    private final TransactionRepository transactions;
    private final TransactionService transactionService;
    private final TransferValidationService validation;
    private final TransferExecutionService execution;
    private final KycService kycService;
    private final SettlementReviewService settlementReviews;

    @Transactional
    public void process(String eventId,String timestamp,String signature,byte[] payload) {
        verifier.verify(timestamp,signature,payload);
        Map<?,?> body;
        String hash;
        try {
            body=new ObjectMapper().readValue(payload,Map.class);
            if (body == null) throw new IllegalArgumentException();
            hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(payload));
        } catch(Exception e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid callback payload"); }
        if(eventId==null || eventId.length()>64 || eventId.isBlank()
                || !eventId.equals(body.get("eventId")) || !properties.getTenantCode().equals(body.get("tenantCode"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Callback identity does not match");
        }
        var previous=receipts.findById(eventId);
        if(previous.isPresent()) {
            if(!hash.equals(previous.get().getPayloadHash())) throw new ResponseStatusException(HttpStatus.CONFLICT,"Event ID reused with different payload");
            return;
        }
        if("KYC_COMPLETE".equals(body.get("event"))) {
            try { kycService.refreshFromCallback(Long.valueOf(body.get("externalCustomerId").toString()),
                    Long.valueOf(body.get("sessionId").toString())); }
            catch(NumberFormatException | NullPointerException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid KYC reference"); }
        } else if("CASE_DECIDED".equals(body.get("event")) && body.get("externalTransactionId")!=null) {
            resolveTransfer(body);
        }
        // Receipt and any payment/identity update commit together; failures remain retryable.
        receipts.saveAndFlush(new SentinelWebhookReceipt(eventId,hash));
    }

    private void resolveTransfer(Map<?,?> body) {
        var tx=transactions.findReviewCandidate(String.valueOf(body.get("caseId"))).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.CONFLICT,"Review transaction not stored yet"));
        String externalReference = tx.getSentinelExternalReference() == null ? tx.getReference() : tx.getSentinelExternalReference();
        long customerId = tx.getSentinelCustomerId() == null ? tx.getInitiatedBy().getId() : tx.getSentinelCustomerId();
        if(!externalReference.equals(body.get("externalTransactionId"))
                || !Long.toString(customerId).equals(body.get("externalCustomerId"))
                || !java.util.Objects.equals(tx.getSentinelTransactionId(),body.get("transactionId"))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Case does not match the payment");
        }
        String decision=String.valueOf(body.get("decision"));
        if(!Set.of("APPROVE","REJECT","ESCALATE").contains(decision)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown case decision");
        }
        if (tx.getType()!=com.mamadou.sutura.transaction.enums.TransactionType.TRANSFER) {
            if (tx.getType() != com.mamadou.sutura.transaction.enums.TransactionType.WALLET_CREDIT
                    && tx.getType() != com.mamadou.sutura.transaction.enums.TransactionType.WALLET_DEBIT)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported review payment type");
            settlementReviews.resolve(tx, decision); return;
        }
        tx = transactions.findForUpdateById(tx.getId()).orElseThrow();
        if(tx.getStatus()!=TransactionStatus.PENDING || !"review".equalsIgnoreCase(tx.getSentinelRecommendation())) return;
        if("ESCALATE".equals(decision)) return;
        if("REJECT".equals(decision)) { transactionService.markFailed(tx,"Sentinel case rejected"); return; }
        try { validation.validatePending(tx); }
        catch(WalletOperationException | LedgerException e) { transactionService.markFailed(tx,e.getMessage()); return; }
        execution.executePending(tx);
    }
}
