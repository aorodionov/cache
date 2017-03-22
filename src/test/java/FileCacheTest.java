import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.company.FileCache;
import ru.company.Invalidator;
import ru.company.SimpleInvalidator;
import ru.company.SimpleMetadataFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileCacheTest {
    private FileCache<String, String> cache;

    @BeforeEach
    public void init() throws URISyntaxException {
        Path path = Paths.get("src/test/resources").toAbsolutePath();
        Invalidator<String> invalidator = new SimpleInvalidator<>(new SimpleMetadataFactory());
        cache = new FileCache<>(path, invalidator, 10);
    }

    @AfterEach
    public void cleanUp() throws IOException, InterruptedException {
        Path path = Paths.get("src/test/resources").toAbsolutePath();
        //Waiting for asynchronous
        sleep(1000);
        Files.list(path).forEach((path1) -> {
            try {
                Files.delete(path1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void cachePut_SuccessTest() {
        for (int i = 0; i < 50; i++) {
            cache.put(String.valueOf(i), String.valueOf(i));
        }
        assertEquals(cache.size(), 10);
    }

    @Test
    public void cacheGet_SuccessTest() {
        cache.put("One", "Two");
        assertEquals("Two", cache.get("One"));
    }

    @Test
    public void cacheRemove_SuccessTest() {
        cache.clear();
        cache.put("One", "Two");
        cache.put("Three", "Four");
        assertEquals(2, cache.size());
        cache.remove("One");
        assertEquals(1, cache.size());
        assertEquals(null, cache.get("One"));
        assertEquals("Four", cache.get("Three"));
    }

    @Test
    public void cacheClear_SuccessTest() {
        for (int i = 0; i < 50; i++) {
            cache.put(String.valueOf(i), String.valueOf(i));
        }
        assertEquals(cache.size(), 10);
        cache.clear();
        assertEquals(cache.size(), 0);
    }

    @Test
    public void cacheInvalidate_SuccessTest() {
        for (int i = 0; i < 50; i++) {
            cache.put(String.valueOf(i), String.valueOf(i));
        }
        assertEquals(cache.size(), 10);
        cache.invalidate();
        assertEquals(cache.size(), 9);
    }
}
