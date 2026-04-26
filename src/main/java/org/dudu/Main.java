package org.dudu;

import com.hazelcast.core.HazelcastInstance;
import org.dudu.core.*;
import org.dudu.persistence.DuduDataLoader;
import org.dudu.persistence.DuduPersistenceManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== ∂u∂u Distributed Framework TRUE MULTI-NODE Demo ===");
        
        try {
            // 0. Parse Arguments
            String filePath = "data/records.csv";
            String targetKey = "name";
            if (args.length >= 1) filePath = args[0];
            if (args.length >= 2) targetKey = args[1];

            // 1. Setup Persistence Manager (MongoDB & MySQL)
            String mongoHost = System.getenv("DUDU_MONGO_HOST") != null ? System.getenv("DUDU_MONGO_HOST") : "127.0.0.1";
            int mongoPort = System.getenv("DUDU_MONGO_PORT") != null ? Integer.parseInt(System.getenv("DUDU_MONGO_PORT")) : 27017;
            
            int mysqlPort = System.getenv("DUDU_MYSQL_PORT") != null ? Integer.parseInt(System.getenv("DUDU_MYSQL_PORT")) : 3306;
            String mysqlUrl = System.getenv("DUDU_MYSQL_URL") != null ? System.getenv("DUDU_MYSQL_URL") : "jdbc:mysql://localhost:" + mysqlPort + "/dudu_archive?useSSL=false&allowPublicKeyRetrieval=true";
            String mysqlUser = System.getenv("DUDU_MYSQL_USER") != null ? System.getenv("DUDU_MYSQL_USER") : "root";
            String mysqlPass = System.getenv("DUDU_MYSQL_PASSWORD") != null ? System.getenv("DUDU_MYSQL_PASSWORD") : "password";

            DuduPersistenceManager persistenceManager = new DuduPersistenceManager(
                mongoHost, mongoPort, "dudu_db", 
                mysqlUrl, mysqlUser, mysqlPass
            );

            // 2. Setup Configuration
            DuduPolicy policy = new DuduPolicy();
            policy.setBlockingKeySet(new HashSet<>(Arrays.asList(targetKey)));
            policy.setDelta(0.75);
            policy.setStrategy(DuduPolicy.JoinStrategy.PPJOIN); // Default

            // 3. Ingest Data from File
            System.out.println("[LOADER] Loading data from: " + filePath + " (Selective Key: " + targetKey + ")");
            List<DataRecord> records = DuduDataLoader.loadFromCsv(filePath);
            int count = 0;
            for (DataRecord record : records) {
                if (record.getBlockingKey().equals(targetKey)) {
                    persistenceManager.saveRecord(targetKey, record);
                    count++;
                }
            }
            System.out.println("[LOADER] Successfully ingested " + count + " records into MongoDB.");

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
