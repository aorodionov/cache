package ru.company;

import java.util.HashMap;

public class RAMCache<K, V> implements Cache<K, V> {
    private final HashMap<K,V> storage = new HashMap<>();

    public void put(K key, V value) {

    }

    public V remove(K key) {
        return null;
    }

    public V get(K key) {
        return null;
    }
}
