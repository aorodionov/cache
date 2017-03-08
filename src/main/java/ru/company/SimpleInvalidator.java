package ru.company;

import java.util.*;

public class SimpleInvalidator<K> implements Invalidator<K> {
    private final HashMap<K, Metadata> storage = new HashMap<>();
    private final Class<? extends Metadata> metadataClass;

    public SimpleInvalidator(Class<? extends Metadata> metadataClass) {
        this.metadataClass = metadataClass;
    }

    @Override
    public void register(K key) {
        if (storage.containsKey(key)) {
            Metadata info = storage.get(key);
            info.update();
        } else {
            Metadata metadata;
            try {
                metadata = metadataClass.newInstance();
                metadata.initialFill();
                storage.put(key, metadata);
            } catch (InstantiationException e) {
                throw new IllegalArgumentException("Incorrect metadata class | Can not instantiate metadata from received class");
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Incorrect metadata class | Can not get access to constructor of received metadata class");
            }
        }
    }

    @Override
    public K getExpiredKey() {
        List<Map.Entry<K, Metadata>> list = new ArrayList<>(storage.entrySet());
        Comparator<Map.Entry<K, Metadata>> comparing =
                Comparator.comparing(entry -> (entry.getValue()));
        list.sort(comparing);
        return list.get(0).getKey();
    }

    @Override
    public void unregister(K key) {
        storage.remove(key);
    }

}
