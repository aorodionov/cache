package ru.company;

public interface Invalidator<K> {
    void register(K key);

    K getExpiredKey();

    void unregister(K key);
}
