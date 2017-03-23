package ru.company;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleInvalidator<K> implements Invalidator<K> {
    private final ConcurrentHashMap<K, Metadata> storage = new ConcurrentHashMap<>();
    private final AbstractMetadataFactory factory;

    public SimpleInvalidator(AbstractMetadataFactory metadataFactory) {
        this.factory = metadataFactory;
    }

    @Override
    public void register(K key) {
        if (storage.containsKey(key)) {
            Metadata info = storage.get(key);
            info.update();
        } else {
            Metadata metadata = factory.getInstance();
            storage.put(key, metadata);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public K getExpiredKey() {
        List<Map.Entry<K, Metadata>> list = new ArrayList<>(storage.entrySet());
        Comparator<Map.Entry<K, Metadata>> comparing =
                Comparator.comparing(entry -> (entry.getValue()));
        list.sort(comparing);
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
