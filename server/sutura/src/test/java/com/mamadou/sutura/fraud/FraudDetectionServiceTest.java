package com.mamadou.sutura.fraud;

import com.mamadou.sutura.fraud.client.FraudDetectionClient;
import com.mamadou.sutura.fraud.config.FraudDetectionProperties;
import com.mamadou.sutura.fraud.dto.FraudDecisionResponse;
import com.mamadou.sutura.fraud.exception.FraudBlockedException;
import com.mamadou.sutura.fraud.service.FraudDetectionService;
import com.mamadou.sutura.fraud.service.FraudEvaluationLogService;
import com.mamadou.sutura.risk.service.RiskFlagService;
import com.mamadou.sutura.user.entity.User;
import com.mamadou.sutura.wallet.entity.Wallet;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.client.ResourceAccessException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FraudDetectionServiceTest {
    private final FraudDetectionClient client = mock(FraudDetectionClient.class);
    private final FraudDetectionProperties properties = new FraudDetectionProperties();
    private final FraudDetectionService service = new FraudDetectionService(
            client, properties, mock(RiskFlagService.class), mock(FraudEvaluationLogService.class));

    @Test
    void onlyAllowCanProceed() {
        assertThatCode(() -> service.enforceDecision(decision("allow"))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"review", "block", "unknown", ""})
    void preventsNonAllowDecisionsFromReachingExecution(String recommendation) {
        assertThatThrownBy(() -> service.enforceDecision(decision(recommendation)))
                .isInstanceOf(FraudBlockedException.class);
    }

    @Test
    void missingDecisionCannotProceed() {
        assertThatThrownBy(() -> service.enforceDecision(null)).isInstanceOf(FraudBlockedException.class);
    }

    @Test
    void timeoutFailsClosed() {
        when(client.score(any())).thenThrow(new ResourceAccessException("timed out"));
        User user = User.builder().id(7L).build();
        Wallet wallet = Wallet.builder().id(9L).currency("GMD").build();
        assertThatThrownBy(() -> service.checkBeforeExecution("transfer-42", user, wallet,
                new BigDecimal("100.00"), "transfer"))
                .isInstanceOf(FraudBlockedException.class);
    }

    private FraudDecisionResponse decision(String value) {
        return new FraudDecisionResponse("transfer-42", value, 0.7,
                List.of("risk explanation"), 1, null, Map.of());
    }
}
