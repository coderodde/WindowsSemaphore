import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import net.coderodde.util.concurrent.Semaphore;

/**
 * This class implements a demonstration for the semaphore.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6
 */
public class Demo {

    /**
     * This character denotes so called "poison pill" for communicating to the 
     * consumers that they should exit.
     */
    private static final Character TERMINATION_SENTINEL = '\u2622';

    /**
     * Implements a consumer thread.
     */
    static class Consumer extends Thread {

        /**
         * The buffer to consume from.
         */
        private final ConcurrentBuffer<Character> buffer;

        /**
         * Constructs this consumer thread.
         * 
         * @param buffer the concurrent buffer to consume from.
         * @param id     the ID of this consumer thread.
         */
        Consumer(ConcurrentBuffer<Character> buffer, int id) {
            this.buffer = buffer;
            this.setName("Consumer " + id);
        }

        /**
         * The actual code of this consumer thread.
         */
        @Override
        public void run() {
            for (;;) {
                final Character c = buffer.remove();
                
                if (c.equals(TERMINATION_SENTINEL)) {
                    // We have a poison pill. Put it back in the buffer and 
                    // terminate this thread.
                    buffer.add(c);
                    return;
                }
            }
        }
    }

    /**
     * This class implements producer threads.
     */
    static class Producer extends Thread {

        /**
         * The concurrent set holding all active producers.
         */
        private Set<Producer> activeProducers;

        /**
         * The actual concurrent buffer to produce to.
         */
        private final ConcurrentBuffer<Character> buffer;

        /**
         * Constructs this producer thread.
         * 
         * @param buffer the buffer for producing items.
         * @param id     the ID of this producer thread.
         */
        Producer(ConcurrentBuffer<Character> buffer, int id) {
            this.buffer = buffer;
            this.setName("Producer " + id);
        }

        /**
         * Sets the set of active producer threads.
         * 
         * @param set a set of threads.
         */
        void setProducerSet(Set<Producer> set) {
            activeProducers = set;
        }

        /**
         * The actual code for this producer thread.
         */
        @Override
        public void run() {
            final Random rnd = new Random();

            for (int i = 0; i < 30; ++i) {
                final Character c = (char)('A' + rnd.nextInt(26));
                buffer.add(c);
            }

            activeProducers.remove(this);

            if (activeProducers.isEmpty()) {
                // The last thread terminates the consumers.
                buffer.add(TERMINATION_SENTINEL);
            }
        }
    }

    /**
     * Implements a concurrent buffer queue. 
     * 
     * @param <E> the actual type of elements.
     */
    static class ConcurrentBuffer<E> {

        /**
         * The default capacity of this buffer.
         */
        private static final int DEFAULT_CAPACITY = 10;

        /**
         * A binary semaphore (mutex) for synchronizing the access to internals
         * of this buffer.
         */
        private final Semaphore mutex;

        /**
         * Guards against the empty buffer.
         */
        private final Semaphore fillCount;

        /**
         * Guards against the full buffer.
         */
        private final Semaphore emptyCount;

        /**
         * The actual storage array.
         */
        private final Object[] storage;

        /**
         * The index of the head element.
         */
        private int index;

        /**
         * The size of this buffer.
         */
        private int size;

        /**
         * Constructs this buffer.
         */
        ConcurrentBuffer() {
            this.mutex      = new Semaphore(1);
            this.fillCount  = new Semaphore(DEFAULT_CAPACITY);
            this.emptyCount = new Semaphore(DEFAULT_CAPACITY);
            this.storage    = new Object[DEFAULT_CAPACITY];
            
            for (int i = 0; i < DEFAULT_CAPACITY; i++) {
                this.fillCount.lock(); // Bring the counter to zero.
            }
        }

        /**
         * Appends <code>element</code> to the tail of this buffer. If this 
         * buffer is full, blocks the calling thread until some space becomes
         * available.
         * 
         * @param element the element to append.
         */
        void add(E element) {
            emptyCount.lock();
            mutex.lock();

            storage[(index + size) % storage.length] = element;
            ++size;

            System.out.println(Thread.currentThread().getName() + " produced " +
                               element + ": " + this);

            mutex.unlock();
            fillCount.unlock();
        }

        /**
         * Removes the element at the head of this buffer. If this buffer is
         * empty, blocks the calling thread until some content appears in this
         * buffer.
         * 
         * @return the element at the head of this buffer.
         */
        E remove() {
            fillCount.lock();
            mutex.lock();

            final E ret = (E) storage[index % storage.length];
            index = (index + 1) % storage.length;
            --size;

            System.out.println(Thread.currentThread().getName() + " consumed " +
                               ret + ": " + this);

            mutex.unlock();
            emptyCount.unlock();

            return ret;
        }

        /**
         * Returns the string representation of the contents of this buffer. 
         * This method is not synchronized.
         * 
         * @return a string.
         */
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("[");

            for (int i = index, j = 0; 
                    j < size; 
                    ++j, i = (i + 1) % storage.length) {
                sb.append(storage[i]);

                if (j < size - 1) {
                    sb.append(", ");
                }
            }

            return sb.append("]").toString();
        }
    }

    /**
     * Implements a demonstration.
     * 
     * @param consumerAmount the amount of consumers to use.
     * @param producerAmount the amount of producers to use.
     */
    public static void run(int consumerAmount, int producerAmount) {
        final Consumer[] consumers = new Consumer[consumerAmount];
        final Producer[] producers = new Producer[producerAmount];
        final ConcurrentBuffer<Character> buffer = new ConcurrentBuffer<>();

        for (int i = 0; i < consumerAmount; ++i) {
            consumers[i] = new Consumer(buffer, i);
        }

        for (int i = 0; i < producerAmount; ++i) {
            producers[i] = new Producer(buffer, i);
        }

        final Set<Producer> producerSet = new HashSet<>(producerAmount);
        producerSet.addAll(Arrays.asList(producers));

        final Set<Producer> synchronizedSet = 
                Collections.synchronizedSet(producerSet);

        for (final Producer p : producers) {
            p.setProducerSet(synchronizedSet);
        }

        for (final Producer p : producers) {
            p.start();
        }

        for (final Consumer c : consumers) {
            c.start();
        }
    }

    public static void main(String... args) throws InterruptedException {
        
//        Semaphore sem = new Semaphore(1);
//        sem.lock();
//        sem.lock();
//        sem.lock();
//        java.util.concurrent.Semaphore semmm = new java.util.concurrent.Semaphore(1);
//        semmm.acquire();
//        semmm.acquire();
//        System.out.println("shit");
//        System.out.println("never");
//
//        Semaphore sem = new Semaphore(1);
//        sem.lock();
//        System.out.println("half");
//        sem.unlock();
//        sem.lock();
//        System.out.println("done!");
        run(2, 3);
    }
}
