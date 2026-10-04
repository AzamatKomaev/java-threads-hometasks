package ru.netology.hazards;

import java.util.concurrent.TimeUnit;

public class DeadlockPreventionDemo {

    private static final NamedLock resourceA = new NamedLock(1, "resourceA");
    private static final NamedLock resourceB = new NamedLock(2, "resourceB");

    public static boolean run() throws InterruptedException {
        Thread thread1 = new Thread(() -> acquireInOrder(resourceA, resourceB), "Thread-1");
        Thread thread2 = new Thread(() -> acquireInOrder(resourceB, resourceA), "Thread-2");

        thread1.start();
        thread2.start();

        thread1.join(TimeUnit.SECONDS.toMillis(5));
        thread2.join(TimeUnit.SECONDS.toMillis(5));

        return !thread1.isAlive() && !thread2.isAlive();
    }

    private static void acquireInOrder(NamedLock first, NamedLock second) {
        NamedLock ordered1 = first.id() < second.id() ? first : second;
        NamedLock ordered2 = first.id() < second.id() ? second : first;

        synchronized (ordered1) {
            sleepQuietly(100);
            synchronized (ordered2) {
                System.out.println(Thread.currentThread().getName()
                        + " захватил " + ordered1.name() + " и " + ordered2.name() + " без deadlock");
            }
        }
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record NamedLock(int id, String name) {
    }
}
