import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.company.Metadata;
import ru.company.SimpleMetadata;

import java.lang.reflect.Field;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimpleMetadataTest {
    private static Metadata metadata;

    @BeforeAll
    public static void init() {
        metadata = new SimpleMetadata();
    }

    @Test
    public void metadataInit_SuccessTest() throws NoSuchFieldException, IllegalAccessException {
        Field lastTimeUsed = metadata.getClass().getDeclaredField("lastTimeUsed");
        Field timesUsed = metadata.getClass().getDeclaredField("timesUsed");
        lastTimeUsed.setAccessible(true);
        timesUsed.setAccessible(true);
        assertTrue((Long) lastTimeUsed.get(metadata) == 0L);
        assertTrue((int) timesUsed.get(metadata) == 0);
        metadata.initialFill();
        assertTrue((Long) lastTimeUsed.get(metadata) > 0);
        assertTrue((int) timesUsed.get(metadata) == 1);

    }

    @Test
    public void metadataUpdate_SuccessTest() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        Field lastTimeUsed = metadata.getClass().getDeclaredField("lastTimeUsed");
        Field timesUsed = metadata.getClass().getDeclaredField("timesUsed");
        lastTimeUsed.setAccessible(true);
        timesUsed.setAccessible(true);
        metadata.initialFill();
        Long lastTimeUsedValue = (Long) lastTimeUsed.get(metadata);
        int timesUsedValue = (int) timesUsed.get(metadata);
        sleep(1000);
        metadata.update();
        assertTrue(lastTimeUsedValue < ((Long) lastTimeUsed.get(metadata)));
        assertTrue(timesUsedValue < ((int) timesUsed.get(metadata)));
    }

    @Test
    public void metadataCompare_SuccessTest() {
        metadata.initialFill();
        Metadata otherMetadata = new SimpleMetadata();
        metadata.update();
        otherMetadata.initialFill();
        assertTrue(metadata.compareTo(otherMetadata) > 0);
    }

}
