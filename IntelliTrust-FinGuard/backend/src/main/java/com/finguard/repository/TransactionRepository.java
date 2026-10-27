package com.finguard.repository;

import com.finguard.entity.Transaction;
import com.finguard.entity.TransactionType;
import com.finguard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByUserOrderByTxnDateDesc(User user);

    List<Transaction> findTop10ByUserOrderByTxnDateDesc(User user);

    List<Transaction> findByUserAndTxnTypeOrderByTxnDateDesc(User user, TransactionType txnType);

    List<Transaction> findByUserAndTxnDateBetweenOrderByTxnDateAsc(User user, LocalDateTime from, LocalDateTime to);

    List<Transaction> findByUserAndTxnDateBetween(User user, LocalDateTime from, LocalDateTime to);

    List<Transaction> findTop120ByUserAndTxnTypeOrderByTxnDateDesc(User user, TransactionType txnType);

    Optional<Transaction> findByUserAndFingerprint(User user, String fingerprint);

    long countByUserAndFraudTrueAndTxnDateAfter(User user, LocalDateTime after);
}
