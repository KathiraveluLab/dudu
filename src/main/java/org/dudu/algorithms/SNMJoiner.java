package org.dudu.algorithms;

import org.dudu.core.DataRecord;
import java.io.Serializable;
import java.util.*;

/**
 * Implementation of Sorted Neighborhood Method (SNM) for near-duplicate detection.
 * Sorts records by a sorting key and matches within a sliding window.
 */
public class SNMJoiner implements Serializable {
    private final int windowSize;
    private final double delta;

    public SNMJoiner(int windowSize, double delta) {
        this.windowSize = windowSize;
        this.delta = delta;
    }

    public List<String[]> findDuplicates(List<DataRecord> data) {
        List<String[]> duplicatePairs = new ArrayList<>();
        
        // 1. Sort data by content (simulated sorting key)
        data.sort(Comparator.comparing(DataRecord::getContent));

        // 2. Sliding Window matching
        for (int i = 0; i < data.size(); i++) {
            DataRecord r1 = data.get(i);
            for (int j = i + 1; j < Math.min(i + windowSize, data.size()); j++) {
                DataRecord r2 = data.get(j);
                
                if (isSimilarityAboveThreshold(r1, r2)) {
                    duplicatePairs.add(new String[]{r1.getId(), r2.getId()});
                }
            }
        }
        return removeInternalDuplicates(duplicatePairs);
    }

    private boolean isSimilarityAboveThreshold(DataRecord r1, DataRecord r2) {
        Set<String> s1 = r1.getTokens();
        Set<String> s2 = r2.getTokens();
        
        Set<String> intersection = new HashSet<>(s1);
        intersection.retainAll(s2);
        int intersectSize = intersection.size();
        
        double similarity = (double) intersectSize / (s1.size() + s2.size() - intersectSize);
        return similarity >= delta;
    }

    private List<String[]> removeInternalDuplicates(List<String[]> pairs) {
        Set<String> seen = new HashSet<>();
        List<String[]> unique = new ArrayList<>();
        for (String[] pair : pairs) {
            String key = pair[0].compareTo(pair[1]) < 0 ? pair[0] + "-" + pair[1] : pair[1] + "-" + pair[0];
            if (seen.add(key)) {
                unique.add(pair);
            }
        }
        return unique;
    }
}
