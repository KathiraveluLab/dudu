package org.dudu;

import com.hazelcast.core.HazelcastInstance;
import org.dudu.core.*;
import org.dudu.persistence.DuduPersistenceManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== ∂u∂u Distributed Framework TRUE MULTI-NODE Demo ===");
        
        try {
            // 1. Setup Persistence Manager (MongoDB & MySQL)
            DuduPersistenceManager persistenceManager = new DuduPersistenceManager(
                "127.0.0.1", 27017, "dudu_db", 
                "jdbc:mysql://localhost:3306/dudu_archive?useSSL=false", "root", "password"
            );

            // 2. Setup Configuration
            DuduPolicy policy = new DuduPolicy();
            policy.setBlockingKeySet(new HashSet<>(Arrays.asList("name")));
            policy.setDelta(0.75);
            policy.setStrategy(DuduPolicy.JoinStrategy.PPJOIN); // Default

            // 3. Seed Data into Persistence Layer (MongoDB)
            persistenceManager.saveRecord("name", new DataRecord("1", "name", "John Smith New York"));
            persistenceManager.saveRecord("name", new DataRecord("2", "name", "John Smith NY"));

            // 4. Initialize Framework (Isolated clusters)
            List<String> nodes = Arrays.asList("127.0.0.1:5701");
            DuduInitializer initializer = new DuduInitializer();
            initializer.initialize(nodes, policy);
            Map<String, HazelcastInstance> tenantInstances = initializer.getTenantInstances();

            // 5. Orchestrate Join (Executes DISTRIBUTEDly on Hazelcast Cluster)
            DuduCoordinator coordinator = new DuduCoordinator(tenantInstances, persistenceManager);
            coordinator.duduJoin(policy);

            // 6. Shutdown
            for (HazelcastInstance instance : tenantInstances.values()) {
                instance.shutdown();
            }
            System.out.println("=== Framework Execution Complete ===");
        } catch (Exception e) {
            System.err.println("[CRITICAL ERROR] Framework failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
