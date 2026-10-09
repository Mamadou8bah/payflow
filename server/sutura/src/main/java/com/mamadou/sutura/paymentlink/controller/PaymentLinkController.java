package com.mamadou.sutura.paymentlink.controller;

import com.mamadou.sutura.paymentlink.dto.CreatePaymentLinkRequest;
import com.mamadou.sutura.paymentlink.dto.PaymentLinkResponse;
import com.mamadou.sutura.paymentlink.service.PaymentLinkService;
import com.mamadou.sutura.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment-links")
@RequiredArgsConstructor
public class PaymentLinkController {

    private final PaymentLinkService paymentLinkService;

    @PostMapping
    public ResponseEntity<PaymentLinkResponse> create(@Valid @RequestBody CreatePaymentLinkRequest request,
                                                      @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(paymentLinkService.create(currentUser.getId(), request));
    }

    @GetMapping
    public ResponseEntity<java.util.List<PaymentLinkResponse>> list(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(paymentLinkService.listForMerchant(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentLinkResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(paymentLinkService.getById(currentUser.getId(), id));
    }
    @GetMapping("/ref/{reference}")
    public ResponseEntity<PaymentLinkResponse> lookup(@PathVariable String reference) {
        return ResponseEntity.ok(paymentLinkService.lookup(reference));
    }

    public record PayRequest(Long walletId) {}

    @PostMapping("/ref/{reference}/pay")
    public ResponseEntity<PaymentLinkResponse> pay(@PathVariable String reference, @RequestBody PayRequest request) {
        return ResponseEntity.ok(paymentLinkService.pay(reference, request.walletId()));
    }
}
