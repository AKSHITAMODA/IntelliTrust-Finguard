package com.finguard.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeoUtilsTest {

    @Test
    void zeroDistanceForSamePoint() {
        assertEquals(0.0, GeoUtils.haversineKm(12.9716, 77.5946, 12.9716, 77.5946), 1e-6);
    }

    @Test
    void bengaluruToDelhiIsApproximatelyCorrect() {
        // Bengaluru -> New Delhi is roughly 1740 km great-circle distance.
        double distance = GeoUtils.haversineKm(12.9716, 77.5946, 28.6139, 77.2090);
        assertEquals(1740, distance, 60);
    }
}
