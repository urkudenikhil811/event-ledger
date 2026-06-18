package com.eventledger.account_service.controller;

import com.eventledger.account_service.dto.AccountDetailsResponse;
import com.eventledger.account_service.dto.ApplyTransactionRequest;
import com.eventledger.account_service.dto.BalanceResponse;
import com.eventledger.account_service.dto.TransactionView;
import com.eventledger.account_service.model.Transaction;
import com.eventledger.account_service.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/{accountId}/transactions")
    public ResponseEntity<TransactionView> applyTransaction(
            @PathVariable String accountId,
            @Valid @RequestBody ApplyTransactionRequest request) {

        Transaction toApply = new Transaction(
                accountId,
                request.type(),
                request.amount(),
                request.currency(),
                request.eventTimestamp(),
                request.sourceEventId());

        Transaction applied = accountService.applyTransaction(toApply);
        return ResponseEntity.ok(TransactionView.from(applied));
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable String accountId) {
        List<Transaction> transactions = accountService.getTransactions(accountId);
        if (transactions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        BigDecimal balance = accountService.computeBalance(transactions);
        return ResponseEntity.ok(new BalanceResponse(accountId, balance));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDetailsResponse> getAccount(@PathVariable String accountId) {
        List<Transaction> transactions = accountService.getTransactions(accountId);
        if (transactions.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        BigDecimal balance = accountService.computeBalance(transactions);
        List<TransactionView> views = transactions.stream()
                .map(TransactionView::from)
                .toList();
        return ResponseEntity.ok(new AccountDetailsResponse(accountId, balance, views));
    }
}