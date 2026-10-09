package com.mamadou.sutura.webhook.service;

import com.mamadou.sutura.withdrawal.service.WithdrawalReservationService;
import com.mamadou.sutura.webhook.dto.ModemPayWebhookPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WithdrawalProcessingService {
    private final WithdrawalReservationService reservations;

    @Transactional
    public void process(ModemPayWebhookPayload payload) {
        var data = payload.getData();
        reservations.confirm(data.getReference(), data.getAmount(), data.getCurrency(), data.getChargeId());
    }
}
