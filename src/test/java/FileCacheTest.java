import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.company.FileCache;
import ru.company.Invalidator;
import ru.company.SimpleInvalidator;
import ru.company.SimpleMetadataFactory;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileCacheTest {
    private FileCache<String, String> cache;

    @BeforeEach
    public void init() throws URISyntaxException {
        URL testFileLocation = Thread.currentThread()
                .getContextClassLoader()
                .getResource("testfile.cache");
        Path path = Paths.get(testFileLocation.toURI());
        Invalidator<String> invalidator = new SimpleInvalidator<>(new SimpleMetadataFactory());
        cache = new FileCache<>(path, invalidator, 10);
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
