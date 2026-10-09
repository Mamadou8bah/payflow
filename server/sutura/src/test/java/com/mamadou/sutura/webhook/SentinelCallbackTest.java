package com.mamadou.sutura.webhook;

import com.mamadou.sutura.webhook.sentinel.*;
import com.mamadou.sutura.kyc.service.KycService;
import com.mamadou.sutura.transaction.entity.Transaction;
import com.mamadou.sutura.transaction.enums.*;
import com.mamadou.sutura.transaction.repository.TransactionRepository;
import com.mamadou.sutura.transaction.service.TransactionService;
import com.mamadou.sutura.transfer.service.*;
import com.mamadou.sutura.user.entity.User;
import com.mamadou.sutura.wallet.exception.WalletOperationException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.*;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SentinelCallbackTest {
    private static final String SECRET="01234567890123456789012345678901";
    private final SentinelWebhookProperties properties=new SentinelWebhookProperties();
    private final SentinelWebhookReceiptRepository receipts=mock(SentinelWebhookReceiptRepository.class);
    private final TransactionRepository repository=mock(TransactionRepository.class);
    private final TransactionService transactions=mock(TransactionService.class);
    private final TransferValidationService validation=mock(TransferValidationService.class);
    private final TransferExecutionService execution=mock(TransferExecutionService.class);
    private final KycService kyc=mock(KycService.class);
    private final SentinelWebhookService service=new SentinelWebhookService(new SentinelWebhookVerifier(properties),
            properties,receipts,repository,transactions,validation,execution,kyc,mock(SettlementReviewService.class));
    private Transaction tx;

    @BeforeEach void setup() {
        properties.setSigningSecret(SECRET); properties.setTenantCode("SUTURA");
        tx=Transaction.builder().id(42L).type(TransactionType.TRANSFER).status(TransactionStatus.PENDING)
                .reference("xfer-42").sentinelCaseId("case-9").sentinelTransactionId("sentinel-42")
                .sentinelRecommendation("review").initiatedBy(User.builder().id(7).build()).build();
        when(repository.findReviewCandidate("case-9")).thenReturn(Optional.of(tx));
        when(repository.findForUpdateById(42L)).thenReturn(Optional.of(tx));
        when(receipts.findById(anyString())).thenReturn(Optional.empty());
    }

    @Test void authenticatedApprovalRechecksThenExecutesAndStoresReceipt() throws Exception {
        process(body("APPROVE"));
        var order=inOrder(validation,execution,receipts);
        order.verify(validation).validatePending(tx);
        order.verify(execution).executePending(tx);
        order.verify(receipts).saveAndFlush(any());
    }

    @Test void rejectionNeverPosts() throws Exception {
        process(body("REJECT"));
        verify(transactions).markFailed(tx,"Sentinel case rejected"); verifyNoInteractions(execution);
    }

    @Test void escalationStaysPending() throws Exception {
        process(body("ESCALATE"));
        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.PENDING); verifyNoInteractions(execution,transactions);
    }

    @Test void frozenOrInsufficientWalletFailsWithoutPosting() throws Exception {
        doThrow(new WalletOperationException("Insufficient balance")).when(validation).validatePending(tx);
        process(body("APPROVE"));
        verify(transactions).markFailed(tx,"Insufficient balance"); verifyNoInteractions(execution);
    }

    @Test void replayDoesNotExecuteAgain() throws Exception {
        String body=body("APPROVE");
        process(body);
        String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(body.getBytes(StandardCharsets.UTF_8)));
        when(receipts.findById("event-1")).thenReturn(Optional.of(new SentinelWebhookReceipt("event-1",hash)));
        process(body);
        verify(execution,times(1)).executePending(tx);
    }

    @Test void reusedEventIdWithDifferentPayloadIsRejected() {
        when(receipts.findById("event-1")).thenReturn(Optional.of(new SentinelWebhookReceipt("event-1","other-hash")));
        assertThatThrownBy(() -> process(body("APPROVE"))).isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(execution);
    }

    @Test void completedPaymentCannotBePostedAgainWithANewEvent() throws Exception {
        tx.setStatus(TransactionStatus.COMPLETED);
        process(body("APPROVE")); verifyNoInteractions(execution);
    }

    @Test void wrongPaymentReferenceIsRejected() {
        assertThatThrownBy(() -> process(body("APPROVE").replace("xfer-42","another-transfer")))
                .isInstanceOf(ResponseStatusException.class); verifyNoInteractions(execution);
    }

    @Test void wrongTenantIsRejectedBeforeLookup() {
        assertThatThrownBy(() -> process(body("APPROVE").replace("SUTURA","OTHER")))
                .isInstanceOf(ResponseStatusException.class); verifyNoInteractions(repository,execution);
    }

    @Test void forgedOrExpiredSignaturesAreRejectedBeforeLookup() throws Exception {
        byte[] payload=body("APPROVE").getBytes(StandardCharsets.UTF_8);
        String now=Long.toString(Instant.now().getEpochSecond());
        assertThatThrownBy(() -> service.process("event-1",now,"00".repeat(32),payload)).isInstanceOf(ResponseStatusException.class);
        String expired=Long.toString(Instant.now().minusSeconds(600).getEpochSecond());
        assertThatThrownBy(() -> service.process("event-1",expired,sign(expired,payload),payload)).isInstanceOf(ResponseStatusException.class);
        verifyNoInteractions(repository,execution);
    }

    @Test void kycCallbackRefreshesAuthoritativeSessionRatherThanAcceptingClaimedStatus() throws Exception {
        process("{\"event\":\"KYC_COMPLETE\",\"eventId\":\"event-1\",\"tenantCode\":\"SUTURA\",\"externalCustomerId\":\"7\",\"sessionId\":\"42\",\"kycStatus\":\"VERIFIED\"}");
        verify(kyc).refreshFromCallback(7L,42L); verifyNoInteractions(execution);
    }

    private void process(String body) throws Exception {
        byte[] bytes=body.getBytes(StandardCharsets.UTF_8);
        String time=Long.toString(Instant.now().getEpochSecond());
        service.process("event-1",time,sign(time,bytes),bytes);
    }

    private String body(String decision) {
        return "{\"event\":\"CASE_DECIDED\",\"eventId\":\"event-1\",\"tenantCode\":\"SUTURA\",\"caseId\":\"case-9\",\"transactionId\":\"sentinel-42\",\"externalTransactionId\":\"xfer-42\",\"externalCustomerId\":\"7\",\"decision\":\""+decision+"\"}";
    }

    private String sign(String time,byte[] body) throws Exception {
        Mac mac=Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));
        mac.update((time+".").getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(mac.doFinal(body));
    }
}
