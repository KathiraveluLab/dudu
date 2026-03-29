package org.dudu.algorithms;

import org.dudu.core.DataRecord;
import java.io.Serializable;
import java.util.*;

/**
 * Implementation of PPJoin algorithm for near-duplicate detection.
 * Optimized with Prefix Indexing, Length Filtering, and Suffix Filtering.
 */
public class PPJoin implements Serializable {
    private final double delta;

    public PPJoin(double delta) {
        this.delta = delta;
    }

    public List<String[]> findDuplicates(List<DataRecord> data) {
        List<String[]> duplicatePairs = new ArrayList<>();
        Collections.sort(data, Comparator.comparingInt(DataRecord::size));

        Map<String, List<Integer>> invertedIndex = new HashMap<>();

        for (int i = 0; i < data.size(); i++) {
            DataRecord r1 = data.get(i);
            int len1 = r1.size();
            
            // Calculate prefix size for r1
            int p1 = (int) Math.floor((1 - delta) * len1) + 1;
            List<String> tokens1 = new ArrayList<>(r1.getTokens());
            Collections.sort(tokens1);

            Map<Integer, Integer> candidateOverlap = new HashMap<>();
            
            for (int k = 0; k < p1; k++) {
                String token = tokens1.get(k);
                if (invertedIndex.containsKey(token)) {
                    for (int j : invertedIndex.get(token)) {
                        candidateOverlap.put(j, candidateOverlap.getOrDefault(j, 0) + 1);
                    }
                }
                // Index current record's prefix
                invertedIndex.computeIfAbsent(token, x -> new ArrayList<>()).add(i);
            }

            // Verify candidates
            for (Map.Entry<Integer, Integer> entry : candidateOverlap.entrySet()) {
                int j = entry.getKey();
                int currentOverlap = entry.getValue();
                DataRecord r2 = data.get(j);
                
                // 1. Length Filter
                if (r1.size() < delta * r2.size()) continue;

                // 2. Suffix Filtering Optimization
                if (isSimilarityAboveThreshold(r1, r2, currentOverlap, p1)) {
                    duplicatePairs.add(new String[]{r1.getId(), r2.getId()});
                }
            }
        }
        return removeInternalDuplicates(duplicatePairs);
    }

    private boolean isSimilarityAboveThreshold(DataRecord r1, DataRecord r2, int overlap, int p1) {
        int s1 = r1.size();
        int s2 = r2.size();
        
        // Required overlap for Jaccard similarity delta
        // Jaccard = overlap / (s1 + s2 - overlap) >= delta
        // overlap >= delta * (s1 + s2 - overlap)
        // overlap * (1 + delta) >= delta * (s1 + s2)
        // overlap >= ceil(delta * (s1 + s2) / (1 + delta))
        int requiredOverlap = (int) Math.ceil((delta * (s1 + s2)) / (1 + delta));

        // Suffix Filtering: max possible overlap = current overlap + remaining tokens in r1 and r2
        // Remaining tokens in r1 after its prefix: s1 - p1
        // Remaining tokens in r2 (approximation): s2 - p2 (p2 not calculated yet, can use s2)
        int maxPossible = overlap + Math.min(s1 - p1, s2); // Very conservative pruning

        if (maxPossible < requiredOverlap) return false;

        // Perform real intersection
        Set<String> intersection = new HashSet<>(r1.getTokens());
        intersection.retainAll(r2.getTokens());
        int intersectSize = intersection.size();
        
        double similarity = (double) intersectSize / (s1 + s2 - intersectSize);
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
