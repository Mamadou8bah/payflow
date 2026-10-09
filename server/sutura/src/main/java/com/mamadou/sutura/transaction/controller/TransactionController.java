package com.mamadou.sutura.transaction.controller;

import com.mamadou.sutura.transaction.dto.ReversalRequest;
import com.mamadou.sutura.transaction.dto.TransactionDetailResponse;
import com.mamadou.sutura.transaction.dto.TransactionFilterRequest;
import com.mamadou.sutura.transaction.dto.TransactionResponse;
import com.mamadou.sutura.transaction.dto.TransactionSearchRequest;
import com.mamadou.sutura.transaction.service.TransactionQueryService;
import com.mamadou.sutura.transfer.service.TransferReversalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionQueryService transactionQueryService;
    private final TransferReversalService transferReversalService;

    @PostMapping("/filter")
    public ResponseEntity<List<TransactionResponse>> filter(@RequestBody TransactionFilterRequest request) {
        return ResponseEntity.ok(transactionQueryService.history(request));
    }

    @PostMapping("/search")
    public ResponseEntity<List<TransactionResponse>> search(@RequestBody TransactionSearchRequest request) {
        return ResponseEntity.ok(transactionQueryService.search(request));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> history(TransactionFilterRequest request) {
        return ResponseEntity.ok(transactionQueryService.history(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDetailResponse> getTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(transactionQueryService.getById(id));
    }

    @GetMapping("/reference")
    public ResponseEntity<TransactionDetailResponse> getByReference(@RequestParam String reference) {
        return ResponseEntity.ok(transactionQueryService.getByReference(reference));
    }

    @PostMapping("/{id}/reverse")
    public ResponseEntity<TransactionResponse> reverse(
            @PathVariable Long id,
            @Valid @RequestBody ReversalRequest request
    ) {
        return ResponseEntity.ok(transferReversalService.reverse(id, request));
    }
}
