package ru.company;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TwoLevelCache<K, V> implements Cache<K, V> {

    private Cache<K, V> firstLevelCache;
    private Cache<K, V> secondLevelCache;

    public TwoLevelCache(Cache<K, V> firstLevelCache, Cache<K, V> secondLevelCache) {
        this.firstLevelCache = firstLevelCache;
        this.secondLevelCache = secondLevelCache;
    }

    @Override
    public Optional<Map<K, V>> put(K key, V value) {
        Map<K, V> displased = firstLevelCache.put(key, value).orElse(new HashMap<>());
        HashMap<K, V> invalidated = new HashMap<>();
        displased.forEach((k, v) -> invalidated.putAll(secondLevelCache.put(k, v)
                .orElse(new HashMap<>())));
        return Optional.of(invalidated);
    }

    @Override
    public Optional<V> remove(K key) {
        if (firstLevelCache.get(key) != null) return firstLevelCache.remove(key);
        return secondLevelCache.remove(key);
    }

    @Override
    public Optional<V> get(K key) {
        if (firstLevelCache.get(key) != null) return firstLevelCache.get(key);
        Optional<V> value = secondLevelCache.get(key);
        value.map(v -> firstLevelCache.put(key, v)
                .orElseGet(HashMap::new))
                .orElseGet(HashMap::new)
                .forEach(secondLevelCache::put);
        return value;
    }

    @Override
    public Map<K, V> invalidate() {
        Map<K, V> invalidated = secondLevelCache.invalidate();
        if (invalidated == null) return firstLevelCache.invalidate();
        return invalidated;
    }

    @Override
    public void clear() {
        firstLevelCache.clear();
        secondLevelCache.clear();
    }

    @Override
    public int size() {
        return firstLevelCache.size() + secondLevelCache.size();
    }

}
