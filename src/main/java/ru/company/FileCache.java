package ru.company;

import java.io.File;

public class FileCache<K, V> implements Cache<K, V> {

    private final File source;

    public FileCache(File source) {
        this.source = source;
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
