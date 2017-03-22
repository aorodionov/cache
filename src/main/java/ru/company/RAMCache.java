package ru.company;

import java.util.HashMap;
import java.util.Map;

/**
 * Key-value {@link Cache} implementaion based on {@link HashMap}
 *
 * @param <K> - the type of keys maintained by this map
 * @param <V> - the type of mapped values
 * @see FileCache
 * @see TwoLevelCache
 */

public class RAMCache<K, V> implements Cache<K, V> {
    private final HashMap<K, V> storage = new HashMap<>();
    private final Invalidator<K> invalidator;
    private final int maxSize;

    public RAMCache(Invalidator<K> invalidator, int maxSize) {
        this.invalidator = invalidator;
        this.maxSize = maxSize;
    }

    @Override
    public Map<K, V> put(K key, V value) {
        storage.put(key, value);
        invalidator.register(key);
        if (storage.size() >= maxSize) return invalidate();
        return null;
    }

    @Override
    public V remove(K key) {
        invalidator.unregister(key);
        return storage.remove(key);
    }

    @Override
    public V get(K key) {
        invalidator.register(key);
        return storage.get(key);
    }

    @Override
    public Map<K, V> invalidate() {
        HashMap<K, V> expired = new HashMap<>();
        K expiredKey = invalidator.getExpiredKey();
        V value = remove(expiredKey);
        expired.put(expiredKey, value);
        return expired;
    }

    @Override
    public void clear(){
        invalidator.clear();
        storage.clear();
    }

    @Override
    public int size() {
        return storage.size();
    }
}
