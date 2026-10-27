package com.finguard.ml;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

class IsolationForestLiteTest {

    @Test
    void scoresObviousOutlierHigherThanClusteredPoint() {
        Random random = new Random(7);
        double[][] cluster = new double[100][2];
        for (int i = 0; i < cluster.length; i++) {
            cluster[i][0] = 100 + random.nextGaussian() * 5;
            cluster[i][1] = 50 + random.nextGaussian() * 5;
        }

        IsolationForestLite forest = new IsolationForestLite(100, 64, 1L);
        forest.fit(cluster);

        double normalScore = forest.score(new double[]{100, 50});
        double outlierScore = forest.score(new double[]{5000, 5000});

        assertTrue(outlierScore > normalScore,
                "Outlier score (" + outlierScore + ") should exceed normal score (" + normalScore + ")");
    }
}
