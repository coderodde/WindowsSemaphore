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
    public Semaphore(int counter) {
        checkCounter(counter);
        this.impl = new WindowsSemaphoreImpl(counter);
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
    private static void checkCounter(int counter) {
        if (counter <= 0) {
            throw new IllegalArgumentException(
                    "The semaphore counter too small: " + counter + ", " +
                    "should be at least 0.");
        }
    }
}