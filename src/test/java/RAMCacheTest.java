import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.company.Invalidator;
import ru.company.RAMCache;
import ru.company.SimpleInvalidator;
import ru.company.SimpleMetadataFactory;

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
            cache.put(String.valueOf(i), String.valueOf(i));
        }
        Assertions.assertEquals(cache.size(), 10);
    }

    @Test
    public void cacheClear_SuccessTest() {
        for (int i = 0; i < 50; i++) {
            cache.put(String.valueOf(i), String.valueOf(i));
        }
        cache.clear();
        Assertions.assertEquals(0, cache.size());
        cache.put("OneMore", "OneMore");
        Assertions.assertEquals(1, cache.size());
    }
}
