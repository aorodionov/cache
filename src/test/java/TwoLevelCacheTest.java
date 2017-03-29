import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.company.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TwoLevelCacheTest {
    private TwoLevelCache<String, String> cache;

    @BeforeEach
    public void init() throws URISyntaxException {
        Path path = Paths.get("src/test/resources").toAbsolutePath();
        Invalidator<String> ramInvalidator = new SimpleInvalidator<>(new SimpleMetadataFactory());
        Invalidator<String> fileInvalidator = new SimpleInvalidator<>(new SimpleMetadataFactory());
        RAMCache<String, String> ram = new RAMCache<>(ramInvalidator, 10);
        FileCache<String, String> file = new FileCache<>(path, fileInvalidator, 10);
        cache = new TwoLevelCache<>(ram, file);
    }

    @AfterEach
    public void cleanUp() throws IOException, InterruptedException {
        Path path = Paths.get("src/test/resources").toAbsolutePath();
        Files.list(path).forEach((path1) -> {
            try {
                Files.delete(path1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void cachePut_SuccessTest() throws ExecutionException, InterruptedException {
        for (int i = 0; i < 76; i++) {
            final int count = i;
            new Thread(() -> cache.put(String.valueOf(count), String.valueOf(count))).run();
            cache.get(String.valueOf(count / 2));
        }
        assertEquals(20, cache.size());
    }

    @Test
    public void cacheGet_SuccessTest() {
        cache.put("One", "Two");
        assertEquals("Two", cache.get("One").orElse(null));
    }

    @Test
    @SuppressWarnings("all")
    public void cacheRemove_SuccessTest() {
        cache.put("One", "Two");
        cache.put("Three", "Four");
        assertEquals(2, cache.size());
        cache.remove("One");
        assertEquals(1, cache.size());
        assertEquals(Optional.empty(), cache.get("One"));
        assertEquals("Four", cache.get("Three").orElse(null));
    }

    @Test
    public void cacheClear_SuccessTest() {
        for (int i = 0; i < 50; i++) {
            cache.put(String.valueOf(i), String.valueOf(i));
        }
        assertEquals(cache.size(), 20);
        cache.clear();
        assertEquals(cache.size(), 0);
    }

    @Test
    public void cacheInvalidate_SuccessTest() {
        for (int i = 0; i < 50; i++) {
            cache.put(String.valueOf(i), String.valueOf(i));
        }
        assertEquals(cache.size(), 20);
        cache.invalidate();
        assertEquals(cache.size(), 19);
    }
}
