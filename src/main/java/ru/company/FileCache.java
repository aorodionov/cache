package ru.company;

import java.net.URL;

public class FileCache<K, V> implements Cache<K, V> {
    private final URL source;

    public FileCache(URL source, Invalidator<String> invalidator) {
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
