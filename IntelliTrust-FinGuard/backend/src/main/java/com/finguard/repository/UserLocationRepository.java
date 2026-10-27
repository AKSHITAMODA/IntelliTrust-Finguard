package com.finguard.repository;

import com.finguard.entity.User;
import com.finguard.entity.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {
    Optional<UserLocation> findFirstByUserOrderByCapturedAtDesc(User user);
}
