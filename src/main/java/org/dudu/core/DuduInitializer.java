package org.dudu.core;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.dudu.distributed.DuduAdaptiveScaler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Algorithm 1: ∂u∂u Initialization (Finalized for Adaptive Scaling)
 */
public class DuduInitializer {
    private final Map<String, HazelcastInstance> tenantInstances = new HashMap<>();
    private final Map<String, Integer> currentClusterSizes = new HashMap<>();

    public void initialize(List<String> nodes, DuduPolicy policy) {
        Set<String> blockingKeySet = policy.getBlockingKeySet();

        int portOffset = 1;
        for (String blockingKey : blockingKeySet) {
            Config config = new Config();
            config.setInstanceName("dudu-cluster-" + blockingKey);
            
            NetworkConfig network = config.getNetworkConfig();
            network.setPort(5710 + portOffset++); 
            network.getJoin().getMulticastConfig().setEnabled(false);
            network.getJoin().getTcpIpConfig().setEnabled(true).setMembers(nodes);

            HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
            tenantInstances.put(blockingKey, instance);
            currentClusterSizes.put(blockingKey, nodes.size());
            System.out.println("Dedicated cluster initialized for tenant: " + blockingKey);
            
            startAdaptiveScaling(instance, blockingKey, policy);
        }
    }

    public synchronized void reallocateResources(String blockingKey, int targetSize) {
        int currentSize = currentClusterSizes.getOrDefault(blockingKey, 0);
        if (targetSize > currentSize) {
            System.out.println("[ALGORITHM-3] Scaling UP for " + blockingKey + ": " + currentSize + " -> " + targetSize + " nodes.");
            // Simulate adding node contexts
        } else if (targetSize < currentSize) {
            System.out.println("[ALGORITHM-3] Scaling DOWN for " + blockingKey + ": " + currentSize + " -> " + targetSize + " nodes.");
            // Simulate removing node contexts
        }
        currentClusterSizes.put(blockingKey, targetSize);
    }

    private void startAdaptiveScaling(HazelcastInstance instance, String blockingKey, DuduPolicy policy) {
        DuduAdaptiveScaler scaler = new DuduAdaptiveScaler(this, instance, Set.of(blockingKey), policy);
        Thread scalerThread = new Thread(scaler, "DuduAdaptiveScalerThread-" + blockingKey);
        scalerThread.setDaemon(true);
        scalerThread.start();
    }

    public Map<String, HazelcastInstance> getTenantInstances() {
        return tenantInstances;
    }

    public int getCurrentClusterSize(String blockingKey) {
        return currentClusterSizes.getOrDefault(blockingKey, 0);
    }
}
