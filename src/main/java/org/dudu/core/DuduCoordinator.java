package org.dudu.core;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import org.dudu.persistence.DuduPersistenceManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Algorithm 2: Coordinator Execution (Finalized for True Multi-Node Distribution)
 */
public class DuduCoordinator {
    private final Map<String, HazelcastInstance> tenantInstances;
    private final ExecutorService localExecutor;
    private final DuduPersistenceManager persistenceManager;

    public DuduCoordinator(Map<String, HazelcastInstance> tenantInstances, DuduPersistenceManager persistenceManager) {
        this.tenantInstances = tenantInstances;
        this.persistenceManager = persistenceManager;
        this.localExecutor = Executors.newFixedThreadPool(Math.max(1, tenantInstances.size()));
    }

    public void duduJoin(String targetKey, DuduPolicy policy) {
        System.out.println("Starting orchestrated TRUE DISTRIBUTED join with Strategy: " + policy.getStrategy());

        List<CompletableFuture<List<String[]>>> futures = tenantInstances.entrySet().stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> {
                    String blockingKey = entry.getKey();
                    HazelcastInstance instance = entry.getValue();

                    // 1. Pull data from Persistence Layer (MongoDB)
                    List<DataRecord> partition = persistenceManager.loadRecords(blockingKey);
                    if (partition == null || partition.isEmpty()) return new ArrayList<String[]>();

                    // 2. Submit Task to Hazelcast Cluster (True Distributed Execution)
                    IExecutorService executorService = instance.getExecutorService("dudu-executor-" + blockingKey);
                    DuduJoinTask task = new DuduJoinTask(blockingKey, policy, partition);
                    
                    try {
                        Future<List<String[]>> future = executorService.submit(task);
                        List<String[]> pairs = future.get();
                        
                        // 3. Persist results back to MySQL (via Persistence Manager)
                        persistenceManager.archiveDuplicates(blockingKey, pairs);
                        return pairs;
                    } catch (Exception e) {
                        System.err.println("[ERROR] Distributed task execution failed for " + blockingKey + ": " + e.getMessage());
                        return new ArrayList<String[]>();
                    }
                }, localExecutor))
                .collect(Collectors.toList());

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        List<String[]> allDuplicatePairs = allOf.thenApply(v -> futures.stream()
                .flatMap(f -> f.join().stream())
                .collect(Collectors.toList())).join();

        // 6. Report with Content
        reportWithContent(targetKey, allDuplicatePairs);
        localExecutor.shutdown();
    }

    private void reportWithContent(String blockingKey, List<String[]> pairs) {
        System.out.println("--------------------------------------------------");
        System.out.println("Final duplicate count across all clusters: " + pairs.size());
        
        if (!pairs.isEmpty()) {
            List<DataRecord> records = persistenceManager.loadRecords(blockingKey);
            Map<String, String> idToContent = records.stream()
                    .collect(Collectors.toMap(DataRecord::getId, DataRecord::getContent, (a, b) -> a));

            for (String[] pair : pairs) {
                if (pair[0].equals(pair[1])) continue; // Filter out identical ID matches (from accidental repeated ingestions)
                
                System.out.println("[RESULT] Possible Match:");
                System.out.println("   -> ID:" + pair[0] + " | " + idToContent.getOrDefault(pair[0], "N/A"));
                System.out.println("   -> ID:" + pair[1] + " | " + idToContent.getOrDefault(pair[1], "N/A"));
                System.out.println();
            }
        }
        System.out.println("--------------------------------------------------");
        System.out.println("[ARCHIVE] Results stored in MySQL table: dudu_results");
    }
}
