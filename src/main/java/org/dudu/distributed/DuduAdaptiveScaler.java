package org.dudu.distributed;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.monitor.LocalMapStats;
import org.dudu.core.DuduInitializer;
import org.dudu.core.DuduPolicy;
import java.util.Set;

/**
 * Algorithm 3: Adaptive Scaling (Finalized)
 * Calculates required cluster size based on non-empty partitions and triggers reallocation.
 */
public class DuduAdaptiveScaler implements Runnable {
    private final DuduInitializer initializer;
    private final HazelcastInstance hazelcastInstance;
    private final Set<String> blockingKeySet;
    private final DuduPolicy policy;
    private final int nodeCapacity = 10; // Fixed capacity per node (records or units)

    public DuduAdaptiveScaler(DuduInitializer initializer, HazelcastInstance hazelcastInstance, 
                               Set<String> blockingKeySet, DuduPolicy policy) {
        this.initializer = initializer;
        this.hazelcastInstance = hazelcastInstance;
        this.blockingKeySet = blockingKeySet;
        this.policy = policy;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                for (String blockingKey : blockingKeySet) {
                    // 1. Monitor load (number of non-empty partitions or records)
                    int m = getNonEmptyPartitionCount(blockingKey);
                    
                    // 2. Calculate required cluster size u = ceil(m / c)
                    int u = (int) Math.ceil((double) m / nodeCapacity);
                    if (u == 0) u = 1;

                    // 3. Trigger reallocation if current size != u
                    int currentSize = initializer.getCurrentClusterSize(blockingKey);
                    if (currentSize != u) {
                        initializer.reallocateResources(blockingKey, u);
                    }
                }
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private int getNonEmptyPartitionCount(String blockingKey) {
        IMap<Object, Object> map = hazelcastInstance.getMap("dudu-data-" + blockingKey);
        LocalMapStats stats = map.getLocalMapStats();
        // Simulating m as the number of records for demo purposes
        return (int) stats.getOwnedEntryCount();
    }
}
