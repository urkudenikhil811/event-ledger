package com.eventledger.account_service.dto;

import com.eventledger.account_service.model.Transaction;
import com.eventledger.account_service.model.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;

public record TransactionView(
        Long id,
        String accountId,
        TransactionType type,
        BigDecimal amount,
        String currency,
        Instant eventTimestamp,
        String sourceEventId
) {
    public static TransactionView from(Transaction t) {
        return new TransactionView(
                t.getId(), t.getAccountId(), t.getType(), t.getAmount(),
                t.getCurrency(), t.getEventTimestamp(), t.getSourceEventId());
    }
}
