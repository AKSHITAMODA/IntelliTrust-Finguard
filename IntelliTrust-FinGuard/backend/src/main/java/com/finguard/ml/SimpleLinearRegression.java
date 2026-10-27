package com.finguard.ml;

/**
 * Ordinary least-squares linear regression over a single feature (month ordinal -> total spend).
 * Self-contained so the forecast's math is fully inspectable for the model-transparency panel.
 */
public class SimpleLinearRegression {

    private double slope;
    private double intercept;
    private double rSquared;
    private int trainingSize;
    private boolean fitted = false;

    public void fit(double[] x, double[] y) {
        int n = x.length;
        this.trainingSize = n;
        if (n == 0) {
            this.fitted = false;
            return;
        }
        if (n == 1) {
            this.slope = 0;
            this.intercept = y[0];
            this.rSquared = 0;
            this.fitted = true;
            return;
        }

        double meanX = mean(x);
        double meanY = mean(y);

        double num = 0;
        double den = 0;
        for (int i = 0; i < n; i++) {
            num += (x[i] - meanX) * (y[i] - meanY);
            den += (x[i] - meanX) * (x[i] - meanX);
        }

        this.slope = den == 0 ? 0 : num / den;
        this.intercept = meanY - slope * meanX;

        double ssTot = 0;
        double ssRes = 0;
        for (int i = 0; i < n; i++) {
            double predicted = predict(x[i]);
            ssTot += (y[i] - meanY) * (y[i] - meanY);
            ssRes += (y[i] - predicted) * (y[i] - predicted);
        }
        this.rSquared = ssTot == 0 ? 1.0 : Math.max(0.0, 1.0 - (ssRes / ssTot));
        this.fitted = true;
    }

    public double predict(double x) {
        return intercept + slope * x;
    }

    private static double mean(double[] values) {
        double sum = 0;
        for (double v : values) sum += v;
        return sum / values.length;
    }

    public double getSlope() { return slope; }
    public double getIntercept() { return intercept; }
    public double getRSquared() { return rSquared; }
    public int getTrainingSize() { return trainingSize; }
    public boolean isFitted() { return fitted; }
}
