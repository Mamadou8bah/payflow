package com.mamadou.sutura.kyc;

import com.mamadou.sutura.kyc.entity.KycVerification;
import com.mamadou.sutura.kyc.repository.KycVerificationRepository;
import com.mamadou.sutura.kyc.service.KycPrivilegeService;
import com.mamadou.sutura.user.entity.User;
import com.mamadou.sutura.user.repository.UserRepository;
import com.mamadou.sutura.wallet.entity.*;
import com.mamadou.sutura.wallet.enums.KycLevel;
import com.mamadou.sutura.wallet.repository.*;
import com.mamadou.sutura.wallet.service.WalletLimitService;
import com.mamadou.sutura.wallet.exception.WalletOperationException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletPrivilegeTest {
    private final KycVerificationRepository kyc = mock(KycVerificationRepository.class);
    private final KycPrivilegeService privileges = new KycPrivilegeService(kyc);
    private final WalletLimitService limits = new WalletLimitService(mock(WalletLimitRepository.class),
            mock(WalletRepository.class), mock(UserRepository.class), privileges);
    private Wallet wallet;

    @BeforeEach void setup() {
        wallet = Wallet.builder().id(9L).user(User.builder().id(7).build()).build();
        wallet.setWalletLimit(WalletLimit.builder().wallet(wallet).kycLevel(KycLevel.LEVEL_0)
                .minTransactionAmount(BigDecimal.ONE).maxTransactionAmount(new BigDecimal("100000"))
                .dailyTransactionLimit(new BigDecimal("200000")).weeklyTransactionLimit(new BigDecimal("1000000"))
                .monthlyTransactionLimit(new BigDecimal("3000000")).build());
        when(kyc.findById(7L)).thenReturn(Optional.empty());
    }

    @Test void baselineRemainsAvailableWithoutVerifiedIdentity() {
        assertThatCode(() -> limits.validateDebit(wallet, BigDecimal.TEN)).doesNotThrowAnyException();
    }

    @ParameterizedTest @ValueSource(strings={"PENDING","FLAGGED","REJECTED"})
    void unverifiedHigherTierCannotSpend(String status) {
        var record = new KycVerification(); record.setStatus(status);
        when(kyc.findById(7L)).thenReturn(Optional.of(record));
        wallet.getWalletLimit().setKycLevel(KycLevel.LEVEL_1);
        assertThatThrownBy(() -> limits.validateDebit(wallet, BigDecimal.TEN)).isInstanceOf(WalletOperationException.class);
    }

    @Test void verifiedHigherTierCanSpendWithinConfiguredLimits() {
        var record = new KycVerification(); record.setStatus("VERIFIED");
        when(kyc.findById(7L)).thenReturn(Optional.of(record));
        wallet.getWalletLimit().setKycLevel(KycLevel.LEVEL_1);
        assertThatCode(() -> limits.validateDebit(wallet, BigDecimal.TEN)).doesNotThrowAnyException();
    }

    @Test void raisingNumericCapsCannotBypassVerificationWithLevelZero() {
        wallet.getWalletLimit().setMaxTransactionAmount(new BigDecimal("200000"));
        assertThatThrownBy(() -> limits.validateDebit(wallet, BigDecimal.TEN)).isInstanceOf(WalletOperationException.class);
    }

    @Test void missingOrDisabledLimitsCannotBypassSpendingControls() {
        wallet.getWalletLimit().setActive(false);
        assertThatThrownBy(() -> limits.validateDebit(wallet, BigDecimal.TEN)).isInstanceOf(WalletOperationException.class);
        wallet.setWalletLimit(null);
        assertThatThrownBy(() -> limits.validateDebit(wallet, BigDecimal.TEN)).isInstanceOf(WalletOperationException.class);
    }
}
