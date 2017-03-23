package ru.company;

import java.util.Map;
import java.util.Optional;

public interface Cache<K, V> {
    Optional<Map<K, V>> put(K key, V value);

    Optional<V> remove(K key);

    Optional<V> get(K key);

    Map<K, V> invalidate();

    void clear();

    int size();
}
