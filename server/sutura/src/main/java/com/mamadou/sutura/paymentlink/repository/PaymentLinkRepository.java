package com.mamadou.sutura.paymentlink.repository;

import com.mamadou.sutura.paymentlink.entity.PaymentLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentLinkRepository extends JpaRepository<PaymentLink, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from PaymentLink p where lower(p.reference) = lower(:reference)")
    Optional<PaymentLink> findForPayment(@org.springframework.data.repository.query.Param("reference") String reference);
    Optional<PaymentLink> findByReferenceIgnoreCase(String reference);
    List<PaymentLink> findByMerchantIdOrderByIdDesc(Long merchantId);
    Optional<PaymentLink> findByIdAndMerchantId(Long id, Long merchantId);
}
