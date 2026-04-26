package org.dudu.persistence;

import org.dudu.core.DataRecord;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to load data from files into the ∂u∂u framework.
 */
public class DuduDataLoader {

    public static List<DataRecord> loadFromCsv(String filePath) throws IOException {
        List<DataRecord> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                String[] parts = line.split(",", 3);
                if (parts.length >= 3) {
                    String id = parts[0].trim();
                    String blockingKey = parts[1].trim();
                    String content = parts[2].trim();
                    records.add(new DataRecord(id, blockingKey, content));
                }
            }
        }
        return records;
    }
}
