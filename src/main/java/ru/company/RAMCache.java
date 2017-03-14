package ru.company;

import java.util.HashMap;

public class RAMCache<K, V> implements Cache<K, V> {
    private final HashMap<K, V> storage = new HashMap<>();
    private final Invalidator<K> invalidator;
    private final int maxSize;

    public RAMCache(Invalidator<K> invalidator, int maxSize) {
        this.invalidator = invalidator;
        this.maxSize = maxSize;
    }

    @Override
    public void put(K key, V value) {
        if (storage.size() >= maxSize) invalidate();
        storage.put(key, value);
        invalidator.register(key);
    }

    @Override
    public V remove(K key) {
        invalidator.unregister(key);
        return storage.remove(key);
    }

    @Override
    public V get(K key) {
        return storage.get(key);
    }

    @Override
    public void invalidate() {
        remove(invalidator.getExpiredKey());
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
