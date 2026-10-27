package com.finguard.service;

import com.finguard.dto.location.LocationRequest;
import com.finguard.dto.location.LocationResponse;
import com.finguard.entity.User;
import com.finguard.entity.UserLocation;
import com.finguard.repository.UserLocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final UserLocationRepository userLocationRepository;

    @Transactional
    public LocationResponse recordLocation(User user, LocationRequest request) {
        UserLocation location = new UserLocation();
        location.setUser(user);
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setAccuracy(request.accuracy());
        location = userLocationRepository.save(location);
        return toResponse(location);
    }

    public Optional<UserLocation> latestLocation(User user) {
        return userLocationRepository.findFirstByUserOrderByCapturedAtDesc(user);
    }

    public LocationResponse latestLocationResponse(User user) {
        return latestLocation(user).map(this::toResponse).orElse(null);
    }

    private LocationResponse toResponse(UserLocation l) {
        return new LocationResponse(l.getLatitude(), l.getLongitude(), l.getAccuracy(), l.getCapturedAt());
    }
}
