package ru.company;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileCache<K, V> implements Cache<K, V> {
    private static final int TWO_KB = 2048;
    private List<K> keyStorage;
    private Invalidator<K> invalidator;
    private int maxSize;
    private ExecutorService executor;
    private Path path;

    public FileCache(Path path, Invalidator<K> invalidator, int maxSize) {
        this.invalidator = invalidator;
        this.maxSize = maxSize;
        this.executor = Executors.newSingleThreadExecutor();
        this.path = path;
        keyStorage = new ArrayList<>();
        if (!Files.exists(path)) {
            writeToFile(new HashMap<>());
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException("Cannot create cache file by path: " + path, e);
            }
        } else {
            HashMap<K, V> map;
            try {
                map = executor.submit(this::readFile).get();
                map.keySet().forEach((key) -> {
                    this.invalidator.register(key);
                    keyStorage.add(key);
                });
            } catch (InterruptedException | ExecutionException e) {
                writeToFile(new HashMap<>());
            }

        }
    }

    @Override
    public void put(K key, V value) {
        if (keyStorage.size() >= maxSize) {
            invalidate();
        }
        putToFile(key, value);
        invalidator.register(key);
        keyStorage.add(key);
    }

    @Override
    public V remove(K key) {
        Future<V> future = executor.submit(() -> removeFromFile(key));
        invalidator.unregister(key);
        keyStorage.remove(key);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error occurred while removing " + key + " from file ", e);
        }
    }

    @Override
    public V get(K key) {
        Future<V> future = executor.submit(() -> readFile().get(key));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error occurred while reading the file ", e);
        }
    }

    @Override
    public void invalidate() {
        K expiredKey = invalidator.getExpiredKey();
        invalidator.unregister(expiredKey);
        keyStorage.remove(expiredKey);
        remove(expiredKey);
    }

    @Override
    public void clear() {
        try {
            Files.delete(path);
            Files.createFile(path);
            writeToFile(new HashMap<>());
            keyStorage.clear();
            invalidator.clear();
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while clearing the file", e);
        }
    }

    @Override
    public int size() {
        return keyStorage.size();
    }

    private void putToFile(K key, V value) {
        HashMap<K, V> map;
        try {
            map = executor.submit(this::readFile).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error occurred while reading the file ", e);
        }
        map.put(key, value);
        executor.submit(() -> writeToFile(map));
    }

    private V removeFromFile(K key) {
        HashMap<K, V> map = readFile();
        executor.submit(() -> writeToFile(map));
        return map.remove(key);
    }

    @SuppressWarnings("unchecked")
    private HashMap<K, V> readFile() {
        ByteBuffer buffer = ByteBuffer.allocate(TWO_KB);
        HashMap<K, V> map;
        try (FileChannel inputChannel = FileChannel.open(path)) {
            inputChannel.read(buffer);
            buffer.flip();
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer.array()));
            map = (HashMap<K, V>) ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Cannot read from file ", e);
        }
        return map;
    }

    private void writeToFile(HashMap<K, V> map) {
        ByteBuffer buffer = ByteBuffer.allocate(TWO_KB);
        try (FileChannel outputChannel = new FileOutputStream(path.toString()).getChannel()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(map);
            oos.flush();
            buffer.put(baos.toByteArray());
            buffer.flip();
            outputChannel.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write to file", e);
        }
    }

}
