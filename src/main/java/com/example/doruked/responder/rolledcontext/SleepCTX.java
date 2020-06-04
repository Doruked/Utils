package com.example.doruked.responder.rolledcontext;

/**
 * A basic immutable instance of {@link SleepContext}
 */
public class SleepCTX implements SleepContext {

    private final boolean shouldSleep;
    private final long interval;


    private SleepCTX(boolean shouldSleep, long interval) {
        this.interval = interval;
        this.shouldSleep = shouldSleep;
    }

    public long getSleepInterval() {
        return interval;
    }

    public boolean getShouldSleep() {
        return shouldSleep;
    }

    /**
     * creates an instance of {@link SleepCTX}
     *
     * @param sleep whether the context requests sleep or not
     * @param durationMillis the duration in millis the sleep request
     * @return an instance of {@link SleepCTX}
     */
    public static SleepCTX create(boolean sleep, long durationMillis) {
        return new SleepCTX(sleep, durationMillis);
    }

    /**
     * @return an instance of {@link SleepCTX} that indicates no sleeping is requested
     * @implSpec initializes with both {@code false} request and a negative duration.
     * Both may not be required, but since this class must be interpreted by another class,
     * and how it will be interpreted is unknown, it takes a safe-ish approach.
     */
    public static SleepCTX notSleeping() {
        return create(false, -1);
    }

    /**
     * @return an instance of {@link SleepCTX} that indicates it has finished sleeping
     * @implSpec the context still indicates that it is sleeping, but that it has a {@code 0} duration.
     * This is to allow some granularity (if desired) between stopping sleep, and saying it has finished.
     */
    public static SleepCTX finishedSleeping() {
        return create(true, 0);
    }




}
