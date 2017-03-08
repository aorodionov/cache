package ru.company;

import java.util.*;

public class SimpleInvalidatorImpl<K> implements Invalidator<K> {
    private final HashMap<K, Metadata> storage = new HashMap<>();
    private final Metadata metadata;

    public SimpleInvalidatorImpl(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void register(K key) {
        if (storage.containsKey(key)) {
            Metadata info = storage.get(key);
            info.update();
        } else storage.put(key, metadata.newInstance());
    }

    @Override
    public K getExpiredKey() {
        List<Map.Entry<K, Metadata>> list = new ArrayList<>(storage.entrySet());
        Comparator<Map.Entry<K, Metadata>> comparing =
                Comparator.comparing(entry -> (entry.getValue()));
        list.sort(comparing.reversed());
        return list.get(0).getKey();
    }

}
