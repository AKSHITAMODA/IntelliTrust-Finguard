package com.finguard.service;

import com.finguard.dto.alert.AlertResponse;
import com.finguard.entity.Alert;
import com.finguard.entity.User;
import com.finguard.exception.ResourceNotFoundException;
import com.finguard.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    public List<AlertResponse> list(User user) {
        return alertRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(AlertResponse::from)
                .toList();
    }

    public long countActive(User user) {
        return alertRepository.countByUserAndResolvedFalse(user);
    }

    @Transactional
    public AlertResponse resolve(User user, Long alertId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found: " + alertId));
        if (!alert.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Alert not found: " + alertId);
        }
        alert.setResolved(true);
        return AlertResponse.from(alertRepository.save(alert));
    }
}
