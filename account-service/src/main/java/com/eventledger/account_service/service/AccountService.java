package com.eventledger.account_service.service;

import com.eventledger.account_service.model.Transaction;
import com.eventledger.account_service.model.TransactionType;
import com.eventledger.account_service.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final TransactionRepository transactionRepository;

    public AccountService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction applyTransaction(Transaction transaction) {
        log.info("Applying transaction {} to account {}",
                transaction.getSourceEventId(), transaction.getAccountId());
        return transactionRepository
                .findBySourceEventId(transaction.getSourceEventId())
                .orElseGet(() -> transactionRepository.save(transaction));
    }

    public List<Transaction> getTransactions(String accountId) {
        return transactionRepository.findByAccountIdOrderByEventTimestampAsc(accountId);
    }

    public BigDecimal computeBalance(List<Transaction> transactions) {
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.CREDIT) {
                balance = balance.add(t.getAmount());
            } else {
                balance = balance.subtract(t.getAmount());
            }
        }
        return balance;
    }
}