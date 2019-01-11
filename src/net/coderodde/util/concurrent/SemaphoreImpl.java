package net.coderodde.util.concurrent;

/**
 * This package-private interface defines the API for semaphore implementation 
 * types.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6
 */
interface SemaphoreImpl {

    /**
     * Locks the implementing semaphore.
     */
    void lock();

    /**
     * Unlocks the implementing semaphore.
     */
    void unlock();

    /**
     * Initialization routine.
     * 
     * @param counter the amount of threads that can pass without blocking.
     */
    void init(int counter);

    /**
     * Releases all the resources.
     */
    void clean();
}