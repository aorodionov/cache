package ru.company;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleInvalidator<K> implements Invalidator<K> {
    private ConcurrentHashMap<K, Metadata> storage = new ConcurrentHashMap<>();
    private AbstractMetadataFactory factory;
    private ReentrantLock lock = new ReentrantLock();
    public SimpleInvalidator(AbstractMetadataFactory metadataFactory) {
        this.factory = metadataFactory;
    }

    @Override
    public void register(K key) {
        if (storage.containsKey(key)) {
            update(key);
        } else {
            Metadata metadata = factory.getInstance();
            storage.put(key, metadata);
        }
    }

    public void update(K key) {
        Optional.ofNullable(storage.get(key))
                .ifPresent(Metadata::update);
    }

    @Override
    @SuppressWarnings("unchecked")
    public K getExpiredKey() {
        List<Map.Entry<K, Metadata>> list = new ArrayList<>(storage.entrySet());
        Comparator<Map.Entry<K, Metadata>> comparing =
                Comparator.comparing(entry -> (entry.getValue()));
        lock.lock();
        list.sort(comparing);
        lock.unlock();
        return list.get(0).getKey();
    }

    @Override
    public void clear() {
        storage.clear();
    }

    @Override
    public void unregister(K key) {
        storage.remove(key);
    }

}
