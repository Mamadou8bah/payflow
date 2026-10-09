package com.mamadou.sutura.paymentlink.dto;

import com.mamadou.sutura.paymentlink.entity.PaymentLink;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentLinkResponse {
    private Long id;
    private String status;
    private String merchantName;
    private String transactionReference;
    private Long merchantId;
    private Long walletId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String paymentUrl;
    private String reference;
    private LocalDateTime expiresAt;

    public static PaymentLinkResponse from(PaymentLink p) {
        PaymentLinkResponse r = new PaymentLinkResponse();
        r.setId(p.getId());
        r.setStatus(p.getStatus());
        r.setMerchantName(p.getMerchant().getFirstName() + " " + p.getMerchant().getLastName());
        r.setTransactionReference(p.getTransactionReference());
        r.setMerchantId(p.getMerchant().getId());
        r.setWalletId(p.getWallet().getId());
        r.setAmount(p.getAmount());
        r.setCurrency(p.getCurrency());
        r.setDescription(p.getDescription());
        r.setPaymentUrl(p.getPaymentUrl());
        r.setReference(p.getReference());
        r.setExpiresAt(p.getExpiresAt());
        return r;
    }
}
