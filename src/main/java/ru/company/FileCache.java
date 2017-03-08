package ru.company;

import java.io.File;

public class FileCache<K, V> implements Cache<K, V> {
    private final File source;

    public FileCache(File source) {
        this.source = source;
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
