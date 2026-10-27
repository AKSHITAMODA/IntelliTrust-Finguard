package com.finguard.service;

import com.finguard.dto.transaction.TransactionRequest;
import com.finguard.dto.transaction.TransactionResponse;
import com.finguard.entity.*;
import com.finguard.exception.ResourceNotFoundException;
import com.finguard.repository.TransactionRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FraudService fraudService;

    @Transactional
    public TransactionResponse create(User user, TransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setTxnDate(request.txnDate());
        transaction.setDescription(request.description());
        transaction.setMerchant(request.merchant());
        transaction.setCategory(request.category() == null ? TransactionCategory.OTHER : request.category());
        transaction.setAmount(request.amount());
        transaction.setTxnType(request.txnType());
        transaction.setSource(TransactionSource.MANUAL);
        transaction.setLatitude(request.latitude());
        transaction.setLongitude(request.longitude());
        transaction.setCity(request.city());
        transaction.setCountry(request.country());
        transaction = transactionRepository.save(transaction);

        fraudService.evaluate(user, transaction);
        return TransactionResponse.from(transaction);
    }

    public List<TransactionResponse> list(User user, TransactionCategory category, TransactionType type,
                                           Boolean fraudOnly, LocalDateTime from, LocalDateTime to, String search) {
        Specification<Transaction> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user"), user));
            if (category != null) predicates.add(cb.equal(root.get("category"), category));
            if (type != null) predicates.add(cb.equal(root.get("txnType"), type));
            if (Boolean.TRUE.equals(fraudOnly)) predicates.add(cb.isTrue(root.get("fraud")));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("txnDate"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("txnDate"), to));
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(root.get("merchant")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return transactionRepository.findAll(spec, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "txnDate"))
                .stream().map(TransactionResponse::from).toList();
    }

    public TransactionResponse getById(User user, Long id) {
        Transaction transaction = findOwned(user, id);
        return TransactionResponse.from(transaction);
    }

    public Transaction getOwnedEntity(User user, Long id) {
        return findOwned(user, id);
    }

    @Transactional
    public void delete(User user, Long id) {
        Transaction transaction = findOwned(user, id);
        transactionRepository.delete(transaction);
    }

    private Transaction findOwned(User user, Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Transaction not found: " + id);
        }
        return transaction;
    }
}
