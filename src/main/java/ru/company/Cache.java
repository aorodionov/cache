package ru.company;

import java.util.Map;
import java.util.Optional;

/**
 * Key-value {@link Cache}
 *
 * @param <K> - the type of keys maintained by this map
 * @param <V> - the type of mapped values
 * @see FileCache
 * @see TwoLevelCache
 */
public interface Cache<K, V> {

    /**
     * @param key   - the key which the value will be put
     * @param value - value
     * @return {@link Map} of excluded elements
     */
    Optional<Map<K, V>> put(K key, V value);

    /**
     *
     * @param key - key to delete
     * @return value of removed element
     */
    Optional<V> remove(K key);

    Optional<V> get(K key);

    /**
     *
     * @return {@link Map} of invalidate elements
     */
    Map<K, V> invalidate();

    void clear();

    int size();

    boolean containsKey(K key);
}
