package com.finguard.repository;

import com.finguard.entity.Alert;
import com.finguard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByUserOrderByCreatedAtDesc(User user);
    long countByUserAndResolvedFalse(User user);
}
