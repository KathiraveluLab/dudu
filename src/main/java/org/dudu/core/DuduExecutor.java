package org.dudu.core;

import org.dudu.algorithms.PPJoin;
import java.util.ArrayList;
import java.util.List;

/**
 * Algorithm 4: Executor Execution
 * Executes the near-duplicate detection logic on a data partition.
 */
public class DuduExecutor {
    private final PPJoin ppJoin;

    public DuduExecutor(double delta) {
        this.ppJoin = new PPJoin(delta);
    }

    /**
     * Executes the duplicate detection for a list of DataRecords.
     */
    public List<String[]> executeForRecords(List<DataRecord> dataPartition) {
        System.out.println("Executor running PPJoin on partition of size: " + dataPartition.size());
        List<String[]> results = ppJoin.findDuplicates(dataPartition);
        System.out.println("Executor found " + results.size() + " duplicate pairs.");
        return results;
    }

    public List<Object> execute(List<Object> dataPartition) {
        return new ArrayList<>(); // Legacy support for skeleton
    }
}
