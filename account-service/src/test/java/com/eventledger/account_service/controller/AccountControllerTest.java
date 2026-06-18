package com.eventledger.account_service.controller;

import com.eventledger.account_service.model.Transaction;
import com.eventledger.account_service.model.TransactionType;
import com.eventledger.account_service.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    void validBalanceRequestReturns200() throws Exception {
        given(accountService.getTransactions("acct-123")).willReturn(List.of(credit()));
        given(accountService.computeBalance(anyList())).willReturn(new BigDecimal("150.00"));

        mockMvc.perform(get("/accounts/acct-123/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("acct-123"))
                .andExpect(jsonPath("$.balance").exists());
    }

    @Test
    void unknownAccountReturns404() throws Exception {
        given(accountService.getTransactions("acct-999")).willReturn(List.of());

        mockMvc.perform(get("/accounts/acct-999/balance"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidTransactionReturns400() throws Exception {
        String badBody = """
                {"type":"CREDIT","amount":-5,"currency":"USD","eventTimestamp":"2026-05-15T14:02:11Z","sourceEventId":"evt-001"}
                """;

        mockMvc.perform(post("/accounts/acct-123/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest());
    }

    private Transaction credit() {
        return new Transaction("acct-123", TransactionType.CREDIT, new BigDecimal("150.00"),
                "USD", Instant.parse("2026-05-15T14:02:11Z"), "evt-001");
    }
}