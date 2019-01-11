package net.coderodde.util.concurrent;

/**
 * This class implements a semaphore type ported to Windows.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Jan 4, 2019)
 */
public class Semaphore {

    /**
     * Holds the actual native implementation of this semaphore.
     */
    private final SemaphoreImpl impl;

    /**
     * Constructs a new semaphore with <code>counter</code> permits.
     * 
     * @param counter the amount of threads that can lock this semaphore without
     *                blocking.
     */
    public Semaphore(int maxCounter, int counter) {
        checkCounters(maxCounter, counter);
        this.impl = new WindowsSemaphoreImpl(maxCounter, counter);
    }

    /**
     * Acquires this semaphore.
     */
    public void lock() {
        impl.lock();
    }

    /**
     * Releases this semaphore.
     */
    public void unlock() {
        impl.unlock();
    }

    /**
     * Checks the sanity of <code>counter</code>.
     * 
     * @param counter the initial amount of permits.
     */
    private static void checkCounters(int maxCounter, int counter) {
        if (maxCounter < 1) {
            throw new IllegalArgumentException(
                    "Max counter is too small: " + maxCounter + ", " + 
                    "should be at least 1.");
        }
        
        if (counter < 0) {
            throw new IllegalArgumentException(
                    "The semaphore counter too small: " + counter + ", " +
                    "should be at least 0.");
        }
        
        if (counter > maxCounter) {
            throw new IllegalArgumentException(
                    "The counter (" + counter + ") is larger than the max " +
                    "counter (" + maxCounter + ").");
        }
    }
}