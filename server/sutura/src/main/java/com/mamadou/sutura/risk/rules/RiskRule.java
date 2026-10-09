package com.mamadou.sutura.risk.rules;

import com.mamadou.sutura.risk.dto.RiskRuleResult;
import com.mamadou.sutura.wallet.entity.Wallet;

import java.math.BigDecimal;

/**
 * Interface for risk evaluation rules.
 * Implementations evaluate specific risk conditions for transactions.
 */
public interface RiskRule {

    /**
     * Evaluate the rule for the given wallet and transaction details
     *
     * @param walletId the wallet ID
     * @param wallet the wallet entity
     * @param transactionAmount the transaction amount
     * @param transactionId the transaction ID
     * @return RiskRuleResult indicating whether rule was triggered and risk score
     */
    RiskRuleResult evaluate(Long walletId, Wallet wallet, BigDecimal transactionAmount, Long transactionId);

    /**
     * Get the rule name/identifier
     */
    String getName();
}
