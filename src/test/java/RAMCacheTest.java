import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.company.Invalidator;
import ru.company.RAMCache;
import ru.company.SimpleInvalidator;
import ru.company.SimpleMetadataFactory;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RAMCacheTest {
    private RAMCache<String, String> cache;

    @BeforeEach
    public void init() {
        Invalidator<String> invalidator = new SimpleInvalidator<>(new SimpleMetadataFactory());
        cache = new RAMCache<>(invalidator, 10);
    }

    @Test
    public void cachePut_SuccessTest() {
        for (int i = 0; i < 50; i++) {
            final int count = i;
            new Thread(() -> cache.put(String.valueOf(count % 5), String.valueOf(count))).run();
        }
        Assertions.assertEquals(5, cache.size());
    }

    @Test
    public void cacheGet_SuccessTest() {
        cache.put("One", "Two");
        assertEquals("Two", cache.get("One").orElse(null));
    }

    @Test
    @SuppressWarnings("all")
    public void cacheRemove_SuccessTest() {
        cache.clear();
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
