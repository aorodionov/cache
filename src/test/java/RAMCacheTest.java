import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.company.*;

public class RAMCacheTest {
    private static RAMCache<String,String> cache;

    @BeforeAll
    public static void init(){
        Invalidator<String> invalidator = new SimpleInvalidator<>(SimpleMetadata.class);
        cache = new RAMCache<>(invalidator,10);
    }

    @Test
    public void cachePut_SuccessTest(){
        for (int i = 0; i < 50; i++) {
            cache.put(String.valueOf(i),String.valueOf(i));
        }
        Assertions.assertEquals(cache.size(),10);
    }

    @Test
    public void cacheClear_SuccessTest(){
        for (int i = 0; i < 50; i++) {
            cache.put(String.valueOf(i),String.valueOf(i));
        }
        cache.clear();
        Assertions.assertEquals(0,cache.size());
        cache.put("OneMore","OneMore");
        Assertions.assertEquals(1,cache.size());
    }
}
