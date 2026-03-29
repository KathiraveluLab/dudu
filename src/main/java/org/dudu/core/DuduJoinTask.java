package org.dudu.core;

import org.dudu.algorithms.PPJoin;
import org.dudu.algorithms.SNMJoiner;
import org.dudu.persistence.DuduPersistenceManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Distributed Task for duplicate detection.
 * Executes on Hazelcast nodes directly.
 */
public class DuduJoinTask implements Callable<List<String[]>>, Serializable {
    private final String blockingKey;
    private final DuduPolicy policy;
    private final List<DataRecord> data;

    public DuduJoinTask(String blockingKey, DuduPolicy policy, List<DataRecord> data) {
        this.blockingKey = blockingKey;
        this.policy = policy;
        this.data = data;
    }

    @Override
    public List<String[]> call() {
        if (data == null || data.isEmpty()) return new ArrayList<>();

        List<String[]> pairs;
        if (policy.getStrategy() == DuduPolicy.JoinStrategy.SNM) {
            SNMJoiner joiner = new SNMJoiner(policy.getSnmWindowSize(), policy.getDelta());
            pairs = joiner.findDuplicates(data);
        } else {
            PPJoin joiner = new PPJoin(policy.getDelta());
            pairs = joiner.findDuplicates(data);
        }
        
        return pairs;
    }
}
