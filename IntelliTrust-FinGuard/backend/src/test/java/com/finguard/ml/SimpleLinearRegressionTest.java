package com.finguard.ml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleLinearRegressionTest {

    @Test
    void fitsPerfectLineWithRSquaredOne() {
        double[] x = {0, 1, 2, 3, 4};
        double[] y = {10, 15, 20, 25, 30};

        SimpleLinearRegression regression = new SimpleLinearRegression();
        regression.fit(x, y);

        assertEquals(5.0, regression.getSlope(), 1e-6);
        assertEquals(10.0, regression.getIntercept(), 1e-6);
        assertEquals(1.0, regression.getRSquared(), 1e-6);
        assertEquals(35.0, regression.predict(5), 1e-6);
    }

    @Test
    void lowerRSquaredForNoisyData() {
        double[] x = {0, 1, 2, 3, 4};
        double[] y = {10, 40, 12, 38, 15};

        SimpleLinearRegression regression = new SimpleLinearRegression();
        regression.fit(x, y);

        assertTrue(regression.getRSquared() < 0.5);
    }
}
