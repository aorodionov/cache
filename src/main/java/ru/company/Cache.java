package ru.company;

import java.util.Map;

public interface Cache<K, V> {
    Map<K, V> put(K key, V value);

    V remove(K key);

    V get(K key);

    Map<K, V> invalidate();

    void clear();

    int size();
}
