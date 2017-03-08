package ru.company;

public class SimpleMetadataImpl implements Metadata<SimpleMetadataImpl> {

    private static final int ONE_TIME = 1;
    private int timesUsed;
    private long lastTimeUsed;

    SimpleMetadataImpl(int timesUsed, long lastTimeUsed) {
        this.timesUsed = timesUsed;
        this.lastTimeUsed = lastTimeUsed;
    }

    private int getTimesUsed() {
        return timesUsed;
    }

    private long getLastTimeUsed() {
        return lastTimeUsed;
    }

    @Override
    public int compareTo(SimpleMetadataImpl o) {
        if (timesUsed > o.getTimesUsed()) return 1;
        if (timesUsed == o.getTimesUsed() && lastTimeUsed > o.getLastTimeUsed()) return 1;
        if (lastTimeUsed == o.getLastTimeUsed()) return 0;
        return -1;
    }

    @Override
    public void update() {
        timesUsed++;
        lastTimeUsed = System.currentTimeMillis();
    }

    @Override
    public Metadata newInstance() {
        return new SimpleMetadataImpl(ONE_TIME,System.currentTimeMillis());
    }
}
