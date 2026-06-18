package com.eventledger.account_service.service;

import com.eventledger.account_service.model.Transaction;
import com.eventledger.account_service.model.TransactionType;
import com.eventledger.account_service.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccountServiceIdempotencyTest {

    @Autowired
    private TransactionRepository transactionRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(transactionRepository);
    }

    @Test
    void applyingSameEventTwiceCreatesOnlyOneRow() {
        Transaction first = accountService.applyTransaction(event("evt-001"));
        Transaction second = accountService.applyTransaction(event("evt-001"));

        assertThat(second.getId()).isEqualTo(first.getId());   // same row returned
        assertThat(transactionRepository.count()).isEqualTo(1L); // only one row exists
    }

    @Test
    void differentEventsCreateSeparateRows() {
        accountService.applyTransaction(event("evt-001"));
        accountService.applyTransaction(event("evt-002"));

        assertThat(transactionRepository.count()).isEqualTo(2L);
    }

    private Transaction event(String sourceEventId) {
        return new Transaction("acct-123", TransactionType.CREDIT, new BigDecimal("150.00"),
                "USD", Instant.parse("2026-05-15T14:02:11Z"), sourceEventId);
    }
}