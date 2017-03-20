import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.company.Invalidator;
import ru.company.SimpleInvalidator;
import ru.company.SimpleMetadataFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.stream.IntStream;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleInvalidatorTest {

    private Invalidator<String> invalidator;
    private HashMap storage;

    @BeforeEach
    public void init() throws IllegalAccessException, NoSuchFieldException {
        invalidator = new SimpleInvalidator<>(new SimpleMetadataFactory());
        Field storageField = invalidator.getClass().getDeclaredField("storage");
        storageField.setAccessible(true);
        storage = (HashMap) storageField.get(invalidator);
    }

    @Test
    public void invalidatorRegister_SuccessTest() throws NoSuchFieldException, IllegalAccessException {
        assertTrue(storage.size() == 0);
        invalidator.register("one");
        assertTrue(storage.size() == 1);
    }

    @Test
    public void invalidatorUnregister_SuccessTest() throws NoSuchFieldException, IllegalAccessException {
        assertTrue(storage.size() == 0);
        invalidator.register("one");
        assertTrue(storage.size() == 1);
        invalidator.unregister("two");
        assertTrue(storage.size() == 1);
        invalidator.unregister("one");
        assertTrue(storage.size() == 0);
    }

    @Test
    public void invalidatorClear_SuccessTest() {
        IntStream.range(0, 10)
                .mapToObj(String::valueOf)
                .forEach(invalidator::register);
        assertTrue(storage.size() == 10);
        invalidator.clear();
        assertTrue(storage.size() == 0);
        invalidator.register("something");
        assertTrue(storage.size() == 1);
    }

    @Test
    public void invalidatorGetExpiredKey_SuccessTest() throws InterruptedException {
        invalidator.register("one");
        sleep(1000);
        invalidator.register("two");
        assertFalse(invalidator.getExpiredKey().equals("two"));
        assertTrue(invalidator.getExpiredKey().equals("one"));
    }
}
