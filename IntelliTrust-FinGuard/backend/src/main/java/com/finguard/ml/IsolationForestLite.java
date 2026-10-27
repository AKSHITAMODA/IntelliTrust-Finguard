package com.finguard.ml;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Self-contained implementation of the Isolation Forest algorithm
 * (Liu, Ting &amp; Zhou, 2008): anomalies are isolated in fewer random
 * splits than normal points, so they have a shorter average path length
 * across an ensemble of random trees.
 */
public class IsolationForestLite {

    private static final double EULER_MASCHERONI = 0.5772156649;

    private final int numTrees;
    private final int subSampleSize;
    private final long seed;

    private List<Node> trees = new ArrayList<>();
    private int builtSampleSize;

    public IsolationForestLite(int numTrees, int subSampleSize, long seed) {
        this.numTrees = numTrees;
        this.subSampleSize = subSampleSize;
        this.seed = seed;
    }

    public void fit(double[][] data) {
        trees.clear();
        if (data.length == 0) return;

        Random random = new Random(seed);
        this.builtSampleSize = Math.min(subSampleSize, data.length);
        int heightLimit = (int) Math.ceil(log2(Math.max(2, builtSampleSize)));

        for (int t = 0; t < numTrees; t++) {
            double[][] sample = sampleWithoutReplacement(data, builtSampleSize, random);
            trees.add(buildTree(sample, 0, heightLimit, random));
        }
    }

    /** Anomaly score in [0,1]; values above ~0.6 indicate anomalous points. */
    public double score(double[] point) {
        if (trees.isEmpty()) return 0.0;
        double totalPathLength = 0;
        for (Node tree : trees) {
            totalPathLength += pathLength(point, tree, 0);
        }
        double avgPathLength = totalPathLength / trees.size();
        double c = averagePathLengthNormalizer(builtSampleSize);
        if (c <= 0) return 0.0;
        return Math.pow(2, -avgPathLength / c);
    }

    private double pathLength(double[] point, Node node, int currentDepth) {
        if (node.isLeaf()) {
            return currentDepth + averagePathLengthNormalizer(node.size);
        }
        if (point[node.splitFeature] < node.splitValue) {
            return pathLength(point, node.left, currentDepth + 1);
        }
        return pathLength(point, node.right, currentDepth + 1);
    }

    private Node buildTree(double[][] data, int depth, int heightLimit, Random random) {
        if (depth >= heightLimit || data.length <= 1) {
            return Node.leaf(data.length);
        }

        int numFeatures = data[0].length;
        int feature = random.nextInt(numFeatures);

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (double[] row : data) {
            min = Math.min(min, row[feature]);
            max = Math.max(max, row[feature]);
        }
        if (min == max) {
            return Node.leaf(data.length);
        }

        double splitValue = min + random.nextDouble() * (max - min);

        List<double[]> leftRows = new ArrayList<>();
        List<double[]> rightRows = new ArrayList<>();
        for (double[] row : data) {
            if (row[feature] < splitValue) leftRows.add(row);
            else rightRows.add(row);
        }

        if (leftRows.isEmpty() || rightRows.isEmpty()) {
            return Node.leaf(data.length);
        }

        Node left = buildTree(leftRows.toArray(new double[0][]), depth + 1, heightLimit, random);
        Node right = buildTree(rightRows.toArray(new double[0][]), depth + 1, heightLimit, random);
        return Node.split(feature, splitValue, left, right);
    }

    private static double[][] sampleWithoutReplacement(double[][] data, int size, Random random) {
        double[][] copy = data.clone();
        int n = copy.length;
        int effectiveSize = Math.min(size, n);
        for (int i = 0; i < effectiveSize; i++) {
            int j = i + random.nextInt(n - i);
            double[] tmp = copy[i];
            copy[i] = copy[j];
            copy[j] = tmp;
        }
        double[][] result = new double[effectiveSize][];
        System.arraycopy(copy, 0, result, 0, effectiveSize);
        return result;
    }

    private static double averagePathLengthNormalizer(int n) {
        if (n <= 1) return 0.0;
        if (n == 2) return 1.0;
        return 2.0 * (Math.log(n - 1) + EULER_MASCHERONI) - (2.0 * (n - 1) / n);
    }

    private static double log2(double value) {
        return Math.log(value) / Math.log(2);
    }

    private static final class Node {
        int splitFeature;
        double splitValue;
        Node left;
        Node right;
        int size;

        static Node leaf(int size) {
            Node node = new Node();
            node.size = size;
            return node;
        }

        static Node split(int feature, double value, Node left, Node right) {
            Node node = new Node();
            node.splitFeature = feature;
            node.splitValue = value;
            node.left = left;
            node.right = right;
            return node;
        }

        boolean isLeaf() {
            return left == null && right == null;
        }
    }
}
