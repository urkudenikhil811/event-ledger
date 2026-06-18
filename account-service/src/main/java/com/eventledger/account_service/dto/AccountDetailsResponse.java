package com.eventledger.account_service.dto;

import java.math.BigDecimal;
import java.util.List;

public record AccountDetailsResponse(
        String accountId,
        BigDecimal balance,
        List<TransactionView> transactions
) {
}
