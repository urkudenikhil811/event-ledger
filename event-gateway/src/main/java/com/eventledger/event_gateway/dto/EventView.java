package com.eventledger.event_gateway.dto;

import com.eventledger.event_gateway.model.Event;
import com.eventledger.event_gateway.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record EventView(
        String eventId,
        String accountId,
        TransactionType type,
        BigDecimal amount,
        String currency,
        Instant eventTimestamp,
        Map<String, Object> metadata
) {
    public static EventView from(Event event) {
        return new EventView(
                event.getEventId(),
                event.getAccountId(),
                event.getType(),
                event.getAmount(),
                event.getCurrency(),
                event.getEventTimestamp(),
                event.getMetadata()
        );
    }
}