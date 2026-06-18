package com.eventledger.account_service.service;

import com.eventledger.account_service.model.Transaction;
import com.eventledger.account_service.model.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccountServiceBalanceTest {

    private final AccountService accountService = new AccountService(null);

    @Test
    void emptyLedgerHasZeroBalance() {
        BigDecimal balance = accountService.computeBalance(List.of());
        assertThat(balance).isEqualByComparingTo("0");
    }

    @Test
    void balanceIsCreditsMinusDebits() {
        BigDecimal balance = accountService.computeBalance(List.of(credit("150.00"), debit("50.00")));
        assertThat(balance).isEqualByComparingTo("100.00");
    }

    @Test
    void arrivalOrderDoesNotChangeBalance() {
        BigDecimal balanceA = accountService.computeBalance(List.of(credit("150.00"), debit("50.00")));
        BigDecimal balanceB = accountService.computeBalance(List.of(debit("50.00"), credit("150.00")));
        assertThat(balanceA).isEqualByComparingTo(balanceB);
    }

    private Transaction credit(String amount) {
        return new Transaction("acct-123", TransactionType.CREDIT, new BigDecimal(amount),
                "USD", Instant.parse("2026-05-15T14:02:11Z"), "evt-credit");
    }

    private Transaction debit(String amount) {
        return new Transaction("acct-123", TransactionType.DEBIT, new BigDecimal(amount),
                "USD", Instant.parse("2026-05-15T10:00:00Z"), "evt-debit");
    }
}