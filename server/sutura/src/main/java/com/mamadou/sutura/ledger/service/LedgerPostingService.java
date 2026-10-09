package com.mamadou.sutura.ledger.service;

import com.mamadou.sutura.ledger.dto.LedgerPostingRequest;
import com.mamadou.sutura.ledger.dto.LedgerPostingResponse;
import com.mamadou.sutura.ledger.dto.LedgerTraceResponse;
import com.mamadou.sutura.ledger.entity.LedgerAccount;
import com.mamadou.sutura.ledger.entity.LedgerPosting;
import com.mamadou.sutura.ledger.entity.LedgerTrace;
import com.mamadou.sutura.ledger.repository.LedgerTraceRepository;
import com.mamadou.sutura.ledger.enums.LedgerPostingSide;
import com.mamadou.sutura.ledger.exception.LedgerException;
import com.mamadou.sutura.ledger.mapper.LedgerMapper;
import com.mamadou.sutura.ledger.repository.LedgerPostingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerPostingService {

    private final LedgerPostingRepository ledgerPostingRepository;
    private final LedgerAccountService ledgerAccountService;
    private final LedgerMapper ledgerMapper;
    private final LedgerTraceRepository ledgerTraceRepository;

    @Transactional
    public LedgerTraceResponse recordPostings(LedgerPostingRequest request) {
        String traceId = resolveTraceId(request.getTraceId());
        String currency = normalizeCurrency(request.getCurrency());

        if (ledgerPostingRepository.existsByTraceId(traceId)) {
            throw new LedgerException("Ledger trace already exists: " + traceId);
        }

        validateZeroSum(request);
        // The primary key arbitrates concurrent retries; this reservation rolls back
        // with the postings if any part of the database transaction fails.
        ledgerTraceRepository.saveAndFlush(new LedgerTrace(traceId, currency));
        Map<String, LedgerAccount> accountCache = new HashMap<>();

        // Convert entries to postings with cached account lookups
        List<LedgerPosting> postings = request.getPostings()
                .stream()
                .map(entry -> toPosting(request, entry, traceId, currency, accountCache))
                .collect(Collectors.toList());

        List<LedgerPosting> savedPostings = ledgerPostingRepository.saveAll(postings);


        return ledgerMapper.toTraceResponse(savedPostings);
    }

    @Transactional(readOnly = true)
    public LedgerTraceResponse getTrace(String traceId) {
        List<LedgerPosting> postings = ledgerPostingRepository.findByTraceIdOrderByIdAsc(traceId);
        if (postings.isEmpty()) {
            throw new LedgerException("Ledger trace not found: " + traceId);
        }
        return ledgerMapper.toTraceResponse(postings);
    }

    @Transactional(readOnly = true)
    public List<LedgerPostingResponse> getAccountPostings(String accountCode) {
        return ledgerPostingRepository.findByAccount_CodeOrderByPostedAtDesc(accountCode.trim().toUpperCase())
                .stream()
                .map(ledgerMapper::toPostingResponse)
                .toList();
    }

    private LedgerPosting toPosting(
            LedgerPostingRequest request,
            LedgerPostingRequest.Entry entry,
            String traceId,
            String currency,
            Map<String, LedgerAccount> accountCache
    ) {
        String accountCode = entry.getAccountCode();

        // Use cache to avoid duplicate account lookups in same batch
        LedgerAccount account = accountCache.computeIfAbsent(accountCode, code ->
            ledgerAccountService.getAccountEntity(code)
        );

        if (!account.isActive()) {
            throw new LedgerException("Ledger account is inactive: " + account.getCode());
        }
        if (!account.getCurrency().equals(currency)) {
            throw new LedgerException("Posting currency does not match ledger account currency: " + account.getCode());
        }

        return LedgerPosting.builder()
                .traceId(traceId)
                .externalReference(trimToNull(request.getExternalReference()))
                .account(account)
                .side(entry.getSide())
                .amount(entry.getAmount())
                .currency(currency)
                .description(resolveDescription(request.getDescription(), entry.getDescription()))
                .build();
    }

    private void validateZeroSum(LedgerPostingRequest request) {
        if (request.getPostings() == null || request.getPostings().size() < 2
                || request.getPostings().stream().anyMatch(entry -> entry == null
                    || entry.getSide() == null || entry.getAmount() == null
                    || entry.getAmount().signum() <= 0)) {
            throw new LedgerException("Each ledger posting must have a side and positive amount");
        }
        if (request.getPostings().stream().anyMatch(entry -> entry.getAmount().stripTrailingZeros().scale() > 4
                || entry.getAmount().compareTo(new BigDecimal("1000000000000")) > 0))
            throw new LedgerException("Posting amount exceeds supported precision or maximum");
        BigDecimal totalDebits = totalForSide(request, LedgerPostingSide.DEBIT);
        BigDecimal totalCredits = totalForSide(request, LedgerPostingSide.CREDIT);

        if (totalDebits.compareTo(BigDecimal.ZERO) <= 0 || totalCredits.compareTo(BigDecimal.ZERO) <= 0) {
            throw new LedgerException("Ledger entry must include at least one debit and one credit");
        }
        if (totalDebits.compareTo(totalCredits) != 0) {
            throw new LedgerException("Ledger postings must be zero-sum: total debits must equal total credits");
        }
    }

    private BigDecimal totalForSide(LedgerPostingRequest request, LedgerPostingSide side) {
        return request.getPostings()
                .stream()
                .filter(entry -> entry.getSide() == side)
                .map(LedgerPostingRequest.Entry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String resolveTraceId(String traceId) {
        if (traceId == null || traceId.isBlank()) {
            return "ledger_" + UUID.randomUUID();
        }
        return traceId.trim();
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new LedgerException("Ledger posting currency is required");
        }
        return currency.trim().toUpperCase();
    }

    private String resolveDescription(String requestDescription, String entryDescription) {
        if (entryDescription != null && !entryDescription.isBlank()) {
            return entryDescription.trim();
        }
        return trimToNull(requestDescription);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
