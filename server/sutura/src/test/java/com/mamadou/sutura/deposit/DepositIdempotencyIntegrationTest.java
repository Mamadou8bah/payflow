package com.mamadou.sutura.deposit;

import com.mamadou.sutura.deposit.dto.CreateDepositRequest;
import com.mamadou.sutura.deposit.entity.Deposit;
import com.mamadou.sutura.deposit.repository.DepositRepository;
import com.mamadou.sutura.deposit.service.DepositService;
import com.mamadou.sutura.user.entity.User;
import com.mamadou.sutura.user.repository.UserRepository;
import com.mamadou.sutura.wallet.entity.Wallet;
import com.mamadou.sutura.wallet.repository.WalletRepository;
import com.mamadou.sutura.webhook.client.CreateChargeResponse;
import com.mamadou.sutura.webhook.client.ModemPayClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import com.mamadou.sutura.fraud.service.FraudDetectionService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class DepositIdempotencyIntegrationTest {

    @Autowired
    DepositService depositService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    DepositRepository depositRepository;

    @MockitoBean
    ModemPayClient modemPayClient;

    @MockitoBean
    FraudDetectionService fraudDetectionService;

    @Test
    void idempotencyPreventsDuplicateCharge() {
        User user = userRepository.save(User.builder()
            .firstName("Test")
            .lastName("User")
            .phoneNumber(java.util.UUID.randomUUID().toString().substring(0, 18))
            .passwordHash("x")
            .email(java.util.UUID.randomUUID() + "@example.test")
            .enabled(true)
            .userStatus(com.mamadou.sutura.user.enums.UserStatus.ACTIVE)
            .role(com.mamadou.sutura.user.enums.Role.USER)
            .twoFactorEnabled(false)
            .build());

        Wallet w = walletRepository.save(Wallet.builder().user(user).currency("USD").name("Primary").build());

        CreateChargeResponse resp = new CreateChargeResponse();
        resp.setId("ch_123");
        resp.setPaymentUrl("https://pay.example/123");

        when(modemPayClient.createCharge(any())).thenReturn(resp);

        CreateDepositRequest req = CreateDepositRequest.builder()
                .walletId(w.getId())
                .amount(new BigDecimal("10.00"))
                .currency("USD")
                .paymentMethod("mobile_money")
                .phoneNumber("+100000000")
                .idempotencyKey("idem-abc-123")
                .build();

        Deposit d1 = depositService.createDeposit(req, user.getId(), false);
        Deposit d2 = depositService.createDeposit(req, user.getId(), false);

        // service should return the same deposit for same idempotency key
        assertThat(d1.getId()).isEqualTo(d2.getId());
        assertThat(d1.getExternalPaymentId()).isEqualTo("ch_123");
        verify(modemPayClient, times(1)).createCharge(any());
    }
}
