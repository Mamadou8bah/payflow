package com.mamadou.sutura.ledger.repository;

import com.mamadou.sutura.ledger.entity.LedgerTrace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerTraceRepository extends JpaRepository<LedgerTrace, String> {}
