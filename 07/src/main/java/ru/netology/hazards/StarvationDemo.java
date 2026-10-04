package ru.netology.hazards;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class StarvationDemo {

    private static final int THREADS_COUNT = 5;

    public static long[] run(long durationMillis, boolean fair) throws InterruptedException {
        ReentrantLock lock = new ReentrantLock(fair);
        long deadline = System.currentTimeMillis() + durationMillis;
        AtomicLong[] counts = new AtomicLong[THREADS_COUNT];
        for (int i = 0; i < THREADS_COUNT; i++) {
            counts[i] = new AtomicLong();
        }

        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < THREADS_COUNT; i++) {
            boolean aggressive = i == 0;
            AtomicLong counter = counts[i];
            threads.add(new Thread(() -> {
                while (System.currentTimeMillis() < deadline) {
                    lock.lock();
                    try {
                        counter.incrementAndGet();
                    } finally {
                        lock.unlock();
                    }
                    if (!aggressive) {
                        Thread.yield();
                    }
                }
            }, "Worker-" + i));
        }

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        long[] result = new long[THREADS_COUNT];
        for (int i = 0; i < THREADS_COUNT; i++) {
            result[i] = counts[i].get();
        }
        return result;
    }
}
