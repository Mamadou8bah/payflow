package com.mamadou.sutura.transfer.dto;

import com.mamadou.sutura.user.entity.User;
import com.mamadou.sutura.wallet.entity.Wallet;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransferValidationResult {
    private Wallet sourceWallet;
    private Wallet destinationWallet;
    private User initiatedBy;
}
