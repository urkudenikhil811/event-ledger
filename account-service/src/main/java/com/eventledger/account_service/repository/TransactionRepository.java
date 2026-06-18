package com.eventledger.account_service.repository;

import com.eventledger.account_service.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findBySourceEventId(String sourceEventId);

    List<Transaction> findByAccountIdOrderByEventTimestampAsc(String accountId);
}
