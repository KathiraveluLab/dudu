package org.dudu.core;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class DuduPolicy implements Serializable {
    private Set<String> blockingKeySet = new HashSet<>();
    private double delta = 0.8;
    private int minThreshold = 10;
    private int maxThreshold = 100;
    private JoinStrategy strategy = JoinStrategy.PPJOIN;
    private int snmWindowSize = 3;

    public enum JoinStrategy {
        PPJOIN, SNM
    }

    // Getters and Setters
    public Set<String> getBlockingKeySet() { return blockingKeySet; }
    public void setBlockingKeySet(Set<String> blockingKeySet) { this.blockingKeySet = blockingKeySet; }
    public double getDelta() { return delta; }
    public void setDelta(double delta) { this.delta = delta; }
    public int getMinThreshold() { return minThreshold; }
    public void setMinThreshold(int minThreshold) { this.minThreshold = minThreshold; }
    public int getMaxThreshold() { return maxThreshold; }
    public void setMaxThreshold(int maxThreshold) { this.maxThreshold = maxThreshold; }
    public JoinStrategy getStrategy() { return strategy; }
    public void setStrategy(JoinStrategy strategy) { this.strategy = strategy; }
    public int getSnmWindowSize() { return snmWindowSize; }
    public void setSnmWindowSize(int snmWindowSize) { this.snmWindowSize = snmWindowSize; }
}
