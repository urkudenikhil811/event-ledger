package com.eventledger.account_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AccountServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullFlow_idempotentApply_outOfOrder_correctBalanceAndOrdering() throws Exception {
        String credit = """
                {"type":"CREDIT","amount":150.00,"currency":"USD","eventTimestamp":"2026-05-15T14:02:11Z","sourceEventId":"evt-001"}
                """;
        String debit = """
                {"type":"DEBIT","amount":50.00,"currency":"USD","eventTimestamp":"2026-05-15T10:00:00Z","sourceEventId":"evt-004"}
                """;

        // apply the credit
        mockMvc.perform(post("/accounts/acct-int/transactions")
                        .contentType(MediaType.APPLICATION_JSON).content(credit))
                .andExpect(status().isOk());

        // apply the SAME credit again -> must be idempotent (no double count)
        mockMvc.perform(post("/accounts/acct-int/transactions")
                        .contentType(MediaType.APPLICATION_JSON).content(credit))
                .andExpect(status().isOk());

        // apply the debit with an EARLIER timestamp (out of order)
        mockMvc.perform(post("/accounts/acct-int/transactions")
                        .contentType(MediaType.APPLICATION_JSON).content(debit))
                .andExpect(status().isOk());

        // balance = 150 - 50 = 100 (the duplicate credit did NOT add another 150)
        mockMvc.perform(get("/accounts/acct-int/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100.0));

        // details: exactly 2 rows, ordered by timestamp -> debit (10:00) first, credit (14:02) second
        mockMvc.perform(get("/accounts/acct-int"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(2))
                .andExpect(jsonPath("$.transactions[0].sourceEventId").value("evt-004"))
                .andExpect(jsonPath("$.transactions[1].sourceEventId").value("evt-001"));
    }
}