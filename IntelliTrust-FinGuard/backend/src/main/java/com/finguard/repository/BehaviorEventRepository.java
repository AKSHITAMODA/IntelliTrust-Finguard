package com.finguard.repository;

import com.finguard.entity.BehaviorEvent;
import com.finguard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BehaviorEventRepository extends JpaRepository<BehaviorEvent, Long> {
    List<BehaviorEvent> findByUserOrderByCapturedAtDesc(User user);
    List<BehaviorEvent> findTop30ByUserOrderByCapturedAtDesc(User user);
}
