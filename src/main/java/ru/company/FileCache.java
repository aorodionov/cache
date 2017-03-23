package ru.company;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    public Map<K, V> put(K key, V value) {
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
        if (keyPathStorage.size() > maxSize) return invalidate();
        return null;
    }

    @Override
    @SuppressWarnings("all")
    public V remove(K key) {
        synchronized (key) {
            V value = removeFromFile(key);
            invalidator.unregister(key);
            keyPathStorage.remove(key);
            return value;
        }
    }

    @Override
    public V get(K key) {
        Path path = keyPathStorage.get(key);
        if (path == null || !Files.exists(path)) return null;
        invalidator.register(key);
        return readFile(path);
    }

    @Override
    public HashMap<K, V> invalidate() {
        HashMap<K, V> expired = new HashMap<>();
        K expiredKey = invalidator.getExpiredKey();
        V value = remove(expiredKey);
        expired.put(expiredKey, value);
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
