package com.eventledger.account_service.dto;

import java.math.BigDecimal;

public record BalanceResponse(String accountId, BigDecimal balance) {
}
