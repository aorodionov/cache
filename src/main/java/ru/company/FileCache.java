package ru.company;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileCache<K, V> implements Cache<K, V> {
    private static final int TWO_KB = 2048;
    private HashMap<K, Path> keyPathStorage;
    private Invalidator<K> invalidator;
    private int maxSize;
    private ExecutorService executor;
    private Path directory;

    public FileCache(Path directory, Invalidator<K> invalidator, int maxSize) {
        this.invalidator = invalidator;
        this.maxSize = maxSize;
        this.executor = Executors.newSingleThreadExecutor();
        this.directory = directory;
        this.keyPathStorage = new HashMap<>();
    }

    @Override
    public Map<K, V> put(K key, V value) {
        String newFileName = getNewFileName();
        Path newPath = putToFile(newFileName, value);
        invalidator.register(key);
        keyPathStorage.put(key, newPath);
        if (keyPathStorage.size() >= maxSize) return invalidate();
        return null;
    }

    @Override
    public V remove(K key) {
        V value = removeFromFile(key);
        invalidator.unregister(key);
        keyPathStorage.remove(key);
        return value;
    }

    @Override
    public V get(K key) {
        Path path = keyPathStorage.get(key);
        if (path == null || !Files.exists(path)) return null;
        Future<V> future = executor.submit(() -> readFile(path));
        invalidator.register(key);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error occurred while reading the file ", e);
        }
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
    public void clear() {
        try {
            Files.list(directory).forEach((path) -> {
                try {
                    Files.delete(path);
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

    private Path putToFile(String filename, V value) {
        Path resolvedPath = directory.resolve(filename);
        if (!Files.exists(resolvedPath)) try {
            Files.createDirectories(resolvedPath.getParent());
            Files.createFile(resolvedPath);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while creating the file: " + resolvedPath, e);
        }
        executor.submit(() -> writeToFile(resolvedPath, value));
        return resolvedPath;
    }

    private V removeFromFile(K key) {
        Path path = keyPathStorage.get(key);
        if (path == null) return null;
        Future<V> value = executor.submit(() -> readFile(path));
        executor.submit(() -> Files.deleteIfExists(path));
        try {
            return value.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error occurred while reading the file: " + path, e);
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
