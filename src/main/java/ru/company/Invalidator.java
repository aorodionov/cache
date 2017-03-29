package ru.company;

/**
 * The class is responsible for the invalidation of keys
 *
 * @param <K> - the type of key
 */
public interface Invalidator<K> {
    void register(K key);

    K getExpiredKey();

    void update(K key);

    void clear();

    void unregister(K key);
}
