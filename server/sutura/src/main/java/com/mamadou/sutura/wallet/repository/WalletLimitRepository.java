package com.mamadou.sutura.wallet.repository;

import com.mamadou.sutura.wallet.entity.WalletLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletLimitRepository extends JpaRepository<WalletLimit, Long> {
    Optional<WalletLimit> findByWalletId(Long walletId);
}
