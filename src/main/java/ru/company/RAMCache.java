package ru.company;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Key-value {@link Cache} implementaion based on {@link HashMap}
 *
 * @param <K> - the type of keys maintained by this map
 * @param <V> - the type of mapped values
 * @see FileCache
 * @see TwoLevelCache
 */

public class RAMCache<K, V> implements Cache<K, V> {
    private final ConcurrentHashMap<K, V> storage = new ConcurrentHashMap<>();
    private final Invalidator<K> invalidator;
    private final int maxSize;

    public RAMCache(Invalidator<K> invalidator, int maxSize) {
        this.invalidator = invalidator;
        this.maxSize = maxSize;
    }

    @Override
    public Optional<Map<K, V>> put(K key, V value) {
        Optional<Map<K, V>> invalidated = Optional.empty();
        if (storage.size() == maxSize) {
            invalidated = Optional.ofNullable(invalidate());
        }
        storage.put(key, value);
        invalidator.register(key);
        return invalidated;
    }

    @Override
    public Optional<V> remove(K key) {
        invalidator.unregister(key);
        return Optional.ofNullable(storage.remove(key));
    }

    @Override
    public Optional<V> get(K key) {
        invalidator.update(key);
        return Optional.ofNullable(storage.get(key));
    }

    @Override
    public Map<K, V> invalidate() {
        HashMap<K, V> expired = new HashMap<>();
        K expiredKey = invalidator.getExpiredKey();
        remove(expiredKey).ifPresent(v -> expired.put(expiredKey, v));
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

    @Override
    public boolean containsKey(K key) {
        return storage.containsKey(key);
    }
}
