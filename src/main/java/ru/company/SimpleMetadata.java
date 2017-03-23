package ru.company;

public class SimpleMetadata implements Metadata<SimpleMetadata> {

    private static final int ONE_TIME = 1;
    private int timesUsed;
    private long lastTimeUsed;

    @Override
    public int compareTo(SimpleMetadata o) {
        if (timesUsed > o.timesUsed) return 1;
        if (timesUsed == o.timesUsed && lastTimeUsed > o.lastTimeUsed) return 1;
        if (lastTimeUsed == o.lastTimeUsed) return 0;
        return -1;
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
