package ru.company;

import java.util.List;

public interface Invalidator<K> {
    void register(K key);

    K getExpiredKey();
}
