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
     * @param maxCounter the maximum number of threads that can hold the lock to 
     * this semaphore at the same time.
     * 
     * @param counter the initial number of threads that can enter the 
     * semaphore.
     */
    void init(int maxCounter, int counter);

    /**
     * Releases all the resources.
     */
    void clean();
}