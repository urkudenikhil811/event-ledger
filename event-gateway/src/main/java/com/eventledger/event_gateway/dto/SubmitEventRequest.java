package com.eventledger.event_gateway.dto;

import com.eventledger.event_gateway.model.TransactionType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record SubmitEventRequest(
        @NotBlank String eventId,
        @NotBlank String accountId,
        @NotNull TransactionType type,
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @NotNull Instant eventTimestamp,
        Map<String, Object> metadata
) {}