package com.mamadou.sutura.transfer.validator;

import com.mamadou.sutura.transfer.dto.TransferRequest;
import com.mamadou.sutura.wallet.exception.WalletOperationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransferRequestValidator {

    public void validate(TransferRequest request) {
        if (request.getDestinationWalletId() == null) {
            throw new WalletOperationException("Destination wallet is required");
        }
        if (request.getSourceWalletId() != null
                && request.getSourceWalletId().equals(request.getDestinationWalletId())) {
            throw new WalletOperationException("Source and destination wallets must be different");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletOperationException("Amount must be greater than zero");
        }
    }
}
