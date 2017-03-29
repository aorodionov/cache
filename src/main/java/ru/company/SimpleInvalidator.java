package ru.company;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link Invalidator}.
 * Process of invalidation based on comparing metadata.
 *
 * @param <K> - the type of key
 */
public class SimpleInvalidator<K> implements Invalidator<K> {
    private ConcurrentHashMap<K, Metadata> storage = new ConcurrentHashMap<>();
    private AbstractMetadataFactory factory;

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
        Comparator<Map.Entry<K, Metadata>> comparing =
                Comparator.comparing(entry -> (entry.getValue()));
        return storage.entrySet()
                .stream()
                .sorted(comparing)
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new RuntimeException("Empty storage of invalidator"));
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
