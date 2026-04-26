package org.dudu.persistence;

import com.mongodb.MongoClient;
import org.dudu.core.DataRecord;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Persistence Manager for ∂u∂u Framework.
 * MongoDB (via Jongo) for record storage.
 * MySQL (via JDBC) for duplicate result archiving.
 */
public class DuduPersistenceManager {
    private final Jongo jongo;
    private final Connection mysqlConn;

    public DuduPersistenceManager(String mongoHost, int mongoPort, String dbName, 
                                 String mysqlUrl, String user, String password) throws Exception {
        // 1. Initialize MongoDB (Jongo)
        System.out.println("[PERSISTENCE] Connecting to MongoDB: " + mongoHost + ":" + mongoPort);
        MongoClient client = new MongoClient(mongoHost, mongoPort);
        this.jongo = new Jongo(client.getDB(dbName));

        // 2. Initialize MySQL (JDBC)
        System.out.println("[PERSISTENCE] Connecting to MySQL: " + mysqlUrl);
        this.mysqlConn = DriverManager.getConnection(mysqlUrl, user, password);
        
        // 3. Initialize Schema
        initSchema();
    }

    private void initSchema() throws SQLException {
        try (Statement stmt = mysqlConn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS dudu_results (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "blocking_key VARCHAR(255), " +
                    "record_1_id VARCHAR(255), " +
                    "record_2_id VARCHAR(255), " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            System.out.println("[PERSISTENCE] MySQL schema initialized successfully.");
        }
    }

    public void saveRecord(String blockingKey, DataRecord record) {
        MongoCollection collection = jongo.getCollection("dudu_records_" + blockingKey);
        collection.save(record);
    }

    public void clearRecords(String blockingKey) {
        MongoCollection collection = jongo.getCollection("dudu_records_" + blockingKey);
        collection.remove();
    }

    public List<DataRecord> loadRecords(String blockingKey) {
        MongoCollection collection = jongo.getCollection("dudu_records_" + blockingKey);
        Iterable<DataRecord> iterable = collection.find().as(DataRecord.class);
        List<DataRecord> records = new ArrayList<>();
        iterable.forEach(records::add);
        return records;
    }

    public void archiveDuplicates(String blockingKey, List<String[]> duplicates) {
        if (duplicates.isEmpty()) return;
        
        String sql = "INSERT INTO dudu_results (blocking_key, record_1_id, record_2_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = mysqlConn.prepareStatement(sql)) {
            for (String[] pair : duplicates) {
                ps.setString(1, blockingKey);
                ps.setString(2, pair[0]);
                ps.setString(3, pair[1]);
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("[PERSISTENCE] Archived " + duplicates.size() + " batch results to MySQL.");
        } catch (SQLException e) {
            System.err.println("[ERROR] Failed to archive duplicates: " + e.getMessage());
        }
    }
}
