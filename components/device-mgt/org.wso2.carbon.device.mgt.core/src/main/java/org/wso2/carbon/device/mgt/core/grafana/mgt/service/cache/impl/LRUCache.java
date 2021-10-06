package org.wso2.carbon.device.mgt.core.grafana.mgt.service.cache.impl;

import org.wso2.carbon.device.mgt.core.grafana.mgt.service.cache.Cache;

import java.util.LinkedHashMap;

public class LRUCache<K, V> implements Cache<K, V> {
    private final LinkedHashMap<K, V> cache;
    private final int capacity;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<>(capacity);
    }

    @Override
    public void add(K key, V value) {
        if (cache.size() == capacity) {
            cache.remove(cache.entrySet().iterator().next().getKey());
        }
        cache.put(key, value);

    }

    @Override
    public V get(K key) {
        V query = cache.get(key);
        if (query != null) {
            if (cache.size() == capacity) {
                cache.remove(key);
                cache.put(key, query);
            }
        }
        return query;
    }
}
