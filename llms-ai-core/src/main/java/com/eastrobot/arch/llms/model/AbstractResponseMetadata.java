package com.eastrobot.arch.llms.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AbstractResponseMetadata {

//    protected static final String AI_METADATA_STRING = "{ id: %1$s, usage: %2$s, rateLimit: %3$s }";
    protected static final String AI_METADATA_STRING = "{ @type: %1$s, id: %2$s, model: %3$s, usage: %4$s, rateLimit: %5$s }";
    protected final Map<String, Object> map = new ConcurrentHashMap<>();

    /**
     * Gets an entry from the context. Returns {@code null} when entry is not present.
     *
     * @param key key
     * @param <T> value type
     * @return entry or {@code null} if not present
     */
    public <T> T get(String key) {
        return (T) this.map.get(key);
    }

    /**
     * Gets an entry from the context. Throws exception when entry is not present.
     *
     * @param key key
     * @param <T> value type
     * @return entry
     * @throws IllegalArgumentException if not present
     */
    public <T> T getRequired(Object key) {
        T object = (T) this.map.get(key);
        if (object == null) {
            throw new IllegalArgumentException("Context does not have an entry for key [" + key + "]");
        }
        return object;
    }

    /**
     * Checks if context contains a key.
     *
     * @param key key
     * @return {@code true} when the context contains the entry with the given key
     */
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    /**
     * Returns an element or default if not present.
     *
     * @param key           key
     * @param defaultObject default object to return
     * @param <T>           value type
     * @return object or default if not present
     */
    public <T> T getOrDefault(Object key, T defaultObject) {
        return (T) this.map.getOrDefault(key, defaultObject);
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return Collections.unmodifiableMap(this.map).entrySet();
    }

    public Set<String> keySet() {
        return Collections.unmodifiableSet(this.map.keySet());
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

}
