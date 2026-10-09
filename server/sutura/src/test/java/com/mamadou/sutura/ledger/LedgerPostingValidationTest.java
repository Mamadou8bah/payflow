package com.mamadou.sutura.ledger;

import com.mamadou.sutura.ledger.dto.LedgerPostingRequest;
import com.mamadou.sutura.ledger.entity.LedgerAccount;
import com.mamadou.sutura.ledger.enums.LedgerPostingSide;
import com.mamadou.sutura.ledger.exception.LedgerException;
import com.mamadou.sutura.ledger.mapper.LedgerMapper;
import com.mamadou.sutura.ledger.repository.LedgerPostingRepository;
import com.mamadou.sutura.ledger.repository.LedgerTraceRepository;
import com.mamadou.sutura.ledger.service.LedgerAccountService;
import com.mamadou.sutura.ledger.service.LedgerPostingService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LedgerPostingValidationTest {
    private LedgerPostingRepository postings;
    private LedgerTraceRepository traces;
    private LedgerAccountService accounts;
    private LedgerPostingService service;

    @BeforeEach void start() {
        postings = mock(LedgerPostingRepository.class);
        traces = mock(LedgerTraceRepository.class);
        accounts = mock(LedgerAccountService.class);
        service = new LedgerPostingService(postings, accounts, mock(LedgerMapper.class), traces);
        when(postings.saveAll(anyList())).thenAnswer(call -> call.getArgument(0));
    }

    @Test void negativeLinesAreRejectedEvenWhenTotalsBalance() {
        var request = request("bad", entry(LedgerPostingSide.DEBIT, "20"), entry(LedgerPostingSide.DEBIT, "-10"),
                entry(LedgerPostingSide.CREDIT, "10"));
        assertThatThrownBy(() -> service.recordPostings(request)).isInstanceOf(LedgerException.class);
        verifyNoInteractions(traces, accounts);
        verify(postings, never()).saveAll(anyList());
    }

    @Test void unbalancedAmountsNeverReserveOrPost() {
        assertThatThrownBy(() -> service.recordPostings(request("bad", entry(LedgerPostingSide.DEBIT, "10"),
                entry(LedgerPostingSide.CREDIT, "11")))).isInstanceOf(LedgerException.class);
        verifyNoInteractions(traces, accounts);
        verify(postings, never()).saveAll(anyList());
    }

    @Test void nextCallReloadsAccountAndRejectsNewlyInactiveState() {
        var active = LedgerAccount.builder().code("WALLET").currency("GMD").active(true).build();
        var inactive = LedgerAccount.builder().code("WALLET").currency("GMD").active(false).build();
        when(accounts.getAccountEntity("WALLET")).thenReturn(active, inactive);
        service.recordPostings(request("first", entry(LedgerPostingSide.DEBIT, "10"), entry(LedgerPostingSide.CREDIT, "10")));
        assertThatThrownBy(() -> service.recordPostings(request("second", entry(LedgerPostingSide.DEBIT, "10"),
                entry(LedgerPostingSide.CREDIT, "10")))).isInstanceOf(LedgerException.class).hasMessageContaining("inactive");
        verify(accounts, times(2)).getAccountEntity("WALLET");
        verify(postings, times(1)).saveAll(anyList());
    }

    @Test void precisionAndOverflowAreRejectedBeforeDatabaseRounding() {
        for (String amount : List.of("0.00001", "1000000000001"))
            assertThatThrownBy(() -> service.recordPostings(request("bad", entry(LedgerPostingSide.DEBIT, amount),
                    entry(LedgerPostingSide.CREDIT, amount)))).hasMessageContaining("precision or maximum");
        verifyNoInteractions(traces, accounts);
        verify(postings, never()).saveAll(anyList());
    }

    private static LedgerPostingRequest request(String trace, LedgerPostingRequest.Entry... entries) {
        var request = new LedgerPostingRequest();
        request.setTraceId(trace);
        request.setCurrency("GMD");
        request.setPostings(List.of(entries));
        return request;
    }

    private static LedgerPostingRequest.Entry entry(LedgerPostingSide side, String amount) {
        var entry = new LedgerPostingRequest.Entry();
        entry.setAccountCode("WALLET");
        entry.setSide(side);
        entry.setAmount(new BigDecimal(amount));
        return entry;
    }
}
