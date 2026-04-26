package org.dudu.core;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a data record for duplicate detection.
 */
public class DataRecord implements Serializable {
    private String id;
    private String blockingKey;
    private Set<String> tokens;

    /**
     * Default constructor for Jackson/Jongo deserialization.
     */
    public DataRecord() {
    }

    public DataRecord(String id, String blockingKey, String content) {
        this.id = id;
        this.blockingKey = blockingKey;
        this.tokens = tokenize(content);
    }

    private Set<String> tokenize(String content) {
        Set<String> set = new HashSet<>();
        if (content != null) {
            for (String s : content.toLowerCase().split("\\s+")) {
                if (!s.isEmpty()) set.add(s);
            }
        }
        return set;
    }

    public String getId() {
        return id;
    }

    public String getBlockingKey() {
        return blockingKey;
    }

    public Set<String> getTokens() {
        return tokens;
    }

    public String getContent() {
        return String.join(" ", tokens);
    }

    public int size() {
        return tokens.size();
    }

    @Override
    public String toString() {
        return "DataRecord{" + "id='" + id + '\'' + ", tokens=" + tokens + '}';
    }
}
