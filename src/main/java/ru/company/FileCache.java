package ru.company;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * {@link Cache} implementaion based on hdd storage
 *
 * @param <K> - the type of keys maintained by this map
 * @param <V> - the type of mapped values
 * @see RAMCache
 * @see TwoLevelCache
 */
public class FileCache<K, V> implements Cache<K, V> {
    private static final int TWO_KB = 2048;
    private HashMap<K, Path> keyPathStorage;
    private Invalidator<K> invalidator;
    private int maxSize;
    private Path directory;

    public FileCache(Path directory, Invalidator<K> invalidator, int maxSize) {
        this.invalidator = invalidator;
        this.maxSize = maxSize;
        this.directory = directory;
        this.keyPathStorage = new HashMap<>();
    }

    @Override
    @SuppressWarnings("all")
    public Optional<Map<K, V>> put(K key, V value) {
        Optional<Map<K, V>> invalidated = Optional.empty();
        if (keyPathStorage.size() == maxSize) {
            invalidated = Optional.ofNullable(invalidate());
        }

        Path resolvedPath;
        if (keyPathStorage.get(key) == null) {
            String newFileName = getNewFileName();
            resolvedPath = directory.resolve(newFileName);
        } else resolvedPath = keyPathStorage.get(key);

        synchronized (resolvedPath) {
            putToFile(resolvedPath, value);
        }
        invalidator.register(key);
        keyPathStorage.put(key, resolvedPath);
        return invalidated;
    }

    @Override
    @SuppressWarnings("all")
    public Optional<V> remove(K key) {
        synchronized (key) {
            V value = removeFromFile(key);
            invalidator.unregister(key);
            keyPathStorage.remove(key);
            return Optional.ofNullable(value);
        }
    }

    @Override
    public Optional<V> get(K key) {
        Path path = keyPathStorage.get(key);
        if (path == null || !Files.exists(path)) return Optional.empty();
        invalidator.update(key);
        return Optional.ofNullable(readFile(path));
    }

    @Override
    public HashMap<K, V> invalidate() {
        HashMap<K, V> expired = new HashMap<>();
        K expiredKey = invalidator.getExpiredKey();
        remove(expiredKey).ifPresent(v -> expired.put(expiredKey, v));
        keyPathStorage.remove(expiredKey);
        return expired;
    }

    @Override
    @SuppressWarnings("all")
    public void clear() {
        try {
            Files.list(directory).forEach((path) -> {
                try {
                    synchronized (path) {
                        Files.deleteIfExists(path);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error occurred while clearing files", e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while clearing files in directory: " + directory, e);
        }
        keyPathStorage.clear();
        invalidator.clear();
    }

    @Override
    public int size() {
        return keyPathStorage.size();
    }

    @Override
    public boolean containsKey(K key) {
        return keyPathStorage.containsKey(key);
    }

    @SuppressWarnings("all")
    private void putToFile(Path path, V value) {
        if (!Files.exists(path)) try {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while creating the file: " + path, e);
        }
        synchronized (path) {
            writeToFile(path, value);
        }
    }

    @SuppressWarnings("all")
    private V removeFromFile(K key) {
        Path path = keyPathStorage.get(key);
        if (path == null) return null;
        synchronized (path) {
            V value = readFile(path);
            try {
                Files.deleteIfExists(path);
            } catch (IOException e) {
                throw new RuntimeException("Error occured while deleting the file: " + path, e);
            }
            return value;
        }
    }

    @SuppressWarnings("unchecked")
    private V readFile(Path path) {
        ByteBuffer buffer = ByteBuffer.allocate(TWO_KB);
        V value;
        try (FileChannel inputChannel = FileChannel.open(path)) {
            inputChannel.read(buffer);
            buffer.flip();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer.array()));
            value = (V) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Cannot read from file ", e);
        }
        return value;
    }

    private void writeToFile(Path path, V value) {
        ByteBuffer buffer = ByteBuffer.allocate(TWO_KB);
        try (FileChannel outputChannel = new FileOutputStream(path.toString()).getChannel()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.flush();
            buffer.put(baos.toByteArray());
            buffer.flip();
            outputChannel.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write to file", e);
        }
    }

    private String getNewFileName() {
        return UUID.randomUUID().toString();
    }

}
