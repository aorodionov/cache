package ru.company;

/**
 * LFU implementation of {@link Metadata}.
 */
public class SimpleMetadata implements Metadata<SimpleMetadata> {

    private static final int ONE_TIME = 1;
    private int timesUsed;
    private long lastTimeUsed;

    @Override
    public int compareTo(SimpleMetadata o) {
        if (timesUsed > o.timesUsed ||
                timesUsed == o.timesUsed && lastTimeUsed > o.lastTimeUsed) {
            return 1;
        }
        if (timesUsed < o.timesUsed) {
            return -1;
        }
        return 0;
    }

    @Override
    public synchronized void update() {
        timesUsed++;
        lastTimeUsed = System.currentTimeMillis();
    }

    @Override
    public Metadata initialFill() {
        timesUsed = ONE_TIME;
        lastTimeUsed = System.currentTimeMillis();
        return this;
    }
}
