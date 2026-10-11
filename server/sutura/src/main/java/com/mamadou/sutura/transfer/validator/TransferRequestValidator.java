package com.mamadou.sutura.transfer.validator;

import com.mamadou.sutura.transfer.dto.TransferRequest;
import com.mamadou.sutura.wallet.exception.WalletOperationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransferRequestValidator {

    public void validate(TransferRequest request) {
        boolean hasPhone = request.getRecipientPhone() != null;
        if (hasPhone == (request.getDestinationWalletId() != null)) {
            throw new WalletOperationException("Provide a recipient phone number or destination wallet, not both");
        }
        if (hasPhone) request.setRecipientPhone(RecipientPhone.normalize(request.getRecipientPhone()));
        if (request.getSourceWalletId() != null
                && request.getSourceWalletId().equals(request.getDestinationWalletId())) {
            throw new WalletOperationException("Source and destination wallets must be different");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new WalletOperationException("Amount must be greater than zero");
        }
    }
}
