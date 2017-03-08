package ru.company;

public interface Cache<K, V> {
    void put(K key, V value);

    V remove(K key);

    V get(K key);
}
