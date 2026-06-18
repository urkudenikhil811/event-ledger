package com.eventledger.account_service.dto;

import com.eventledger.account_service.model.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public record ApplyTransactionRequest(
        @NotNull TransactionType type,
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @NotNull Instant eventTimestamp,
        @NotBlank String sourceEventId
) {}
