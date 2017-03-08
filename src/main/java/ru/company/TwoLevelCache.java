package ru.company;

public class TwoLevelCache<K, V> implements Cache<K, V> {

    private final Cache firstLevelCache;
    private final Cache secondLevelCache;

    public TwoLevelCache(Cache firstLevelCache, Cache secondLevelCache) {
        this.firstLevelCache = firstLevelCache;
        this.secondLevelCache = secondLevelCache;
    }

    public void put(K key, V value) {

    }

    public V remove(K key) {
        return null;
    }

    public V get(K key) {
        return null;
    }
}
