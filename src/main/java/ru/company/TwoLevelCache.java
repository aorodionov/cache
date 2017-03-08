package ru.company;

public class TwoLevelCache<K, V> implements Cache<K, V> {

    private final Cache firstLevelCache;
    private final Cache secondLevelCache;

    public TwoLevelCache(Cache firstLevelCache, Cache secondLevelCache) {
        this.firstLevelCache = firstLevelCache;
        this.secondLevelCache = secondLevelCache;
    }

    @Override
    public void put(K key, V value) {

    }

    @Override
    public V remove(K key) {
        return null;
    }

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public void invalidate() {

    }

    @Override
    public void clear() {

    }

    @Override
    public int size() {
        return 0;
    }
}
