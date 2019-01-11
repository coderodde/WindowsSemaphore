package net.coderodde.util.concurrent;

/**
 * This class implements the semaphore type interfacing with MacOSX.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6
 */
final class WindowsSemaphoreImpl implements SemaphoreImpl {

    static {
        try {
            System.load("C:\\Users\\rodde\\source\\repos\\" +
                        "JavaWindowsSemaphore\\x64\\Release\\" + 
                        "JavaWindowsSemaphore.dll");
        } catch (UnsatisfiedLinkError error) {
            error.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Holds the handle to a semaphore.
     */
    private volatile long semaphoreHandle;

    /**
     * Constructs a semaphore with <code>counter</code> permits.
     * 
     * @param counter the amount of permits.
     */
    WindowsSemaphoreImpl(int counter) {
        init(counter);
    }

    /**
     * Acquires this semaphore. If the current counter of this semaphore is
     * zero, the calling thread is blocked.
     */
    @Override
    public native void lock();

    /**
     * Releases this semaphore. Effectively increments the counter of this 
     * semaphore so that other threads may acquire this semaphore.
     */
 
    @Override
    public native void unlock();

    /**
     * Creates the semaphore and loads its handle into <code>semaphoreId</code>.
     */
    @Override
    public native void init(int counter);

    /**
     * Releases resources.
     */
    @Override
    public native void clean();

    /**
     * Release the resources associated with this semaphore.
     */
    @Override
    protected void finalize() {
        try {
            super.finalize();
        } catch (Throwable t) {}

        clean();
    }
}
