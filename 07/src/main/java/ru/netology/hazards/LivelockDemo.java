package ru.netology.hazards;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class LivelockDemo {

    public record Result(long retries1, long retries2, boolean completed1, boolean completed2) {
    }

    public static Result run(long durationMillis, boolean useBackoff) throws InterruptedException {
        ReentrantLock resourceA = new ReentrantLock();
        ReentrantLock resourceB = new ReentrantLock();
        AtomicLong retries1 = new AtomicLong();
        AtomicLong retries2 = new AtomicLong();
        AtomicBoolean completed1 = new AtomicBoolean();
        AtomicBoolean completed2 = new AtomicBoolean();
        CountDownLatch startSignal = new CountDownLatch(1);
        long deadline = System.currentTimeMillis() + durationMillis;

        Thread thread1 = new Thread(() -> {
            awaitStart(startSignal);
            worker(resourceA, resourceB, deadline, retries1, completed1, useBackoff);
        }, "Thread-1");
        Thread thread2 = new Thread(() -> {
            awaitStart(startSignal);
            worker(resourceB, resourceA, deadline, retries2, completed2, useBackoff);
        }, "Thread-2");

        thread1.start();
        thread2.start();
        startSignal.countDown();
        thread1.join();
        thread2.join();

        return new Result(retries1.get(), retries2.get(), completed1.get(), completed2.get());
    }

    private static void worker(ReentrantLock ownLock, ReentrantLock otherLock, long deadline,
                                AtomicLong retries, AtomicBoolean completed, boolean useBackoff) {
        while (System.currentTimeMillis() < deadline) {
            if (ownLock.tryLock()) {
                try {
                    sleepShort();
                    if (otherLock.tryLock()) {
                        try {
                            completed.set(true);
                            return;
                        } finally {
                            otherLock.unlock();
                        }
                    }
                } finally {
                    ownLock.unlock();
                }
            }
            retries.incrementAndGet();
            if (useBackoff) {
                sleepRandom();
            } else {
                Thread.yield();
            }
        }
    }

    private static void sleepRandom() {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(1, 5));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleepShort() {
        try {
            Thread.sleep(15);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void awaitStart(CountDownLatch startSignal) {
        try {
            startSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
