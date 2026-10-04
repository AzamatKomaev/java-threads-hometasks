package ru.netology.atomics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AtomicsDemo {

    public static void main(String[] args) throws InterruptedException {
        runVolatileStopDemo();
        System.out.println();
        runCounterComparison();
        System.out.println();
        runSingletonCacheDemo();
    }

    private static void runVolatileStopDemo() throws InterruptedException {
        System.out.println("Этап 1. Безопасная остановка потока через volatile");

        VolatileWorker worker = new VolatileWorker();
        Thread thread = new Thread(worker, "VolatileWorker");

        thread.start();
        Thread.sleep(200);
        worker.stop();
        thread.join(1000);

        System.out.println("  поток завершился: " + !thread.isAlive());
        System.out.println("  выполнено итераций за 200 мс: " + worker.getIterations());
    }

    private static void runCounterComparison() throws InterruptedException {
        System.out.println("Этап 2. AtomicInteger против synchronized-счётчика");

        int threadsCount = 8;
        int incrementsPerThread = 500_000;
        int expected = threadsCount * incrementsPerThread;

        AtomicCounter atomicCounter = new AtomicCounter();
        long atomicTime = runIncrements(threadsCount, incrementsPerThread, atomicCounter::increment);
        System.out.println("  AtomicCounter: ожидалось " + expected
                + ", получено " + atomicCounter.get() + ", время " + atomicTime + " мс");

        SynchronizedCounter synchronizedCounter = new SynchronizedCounter();
        long syncTime = runIncrements(threadsCount, incrementsPerThread, synchronizedCounter::increment);
        System.out.println("  SynchronizedCounter: ожидалось " + expected
                + ", получено " + synchronizedCounter.get() + ", время " + syncTime + " мс");
    }

    private static long runIncrements(int threadsCount, int incrementsPerThread, Runnable incrementAction)
            throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < threadsCount; t++) {
            threads.add(new Thread(() -> {
                for (int i = 0; i < incrementsPerThread; i++) {
                    incrementAction.run();
                }
            }));
        }

        long start = System.nanoTime();
        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }
        return (System.nanoTime() - start) / 1_000_000;
    }

    private static void runSingletonCacheDemo() throws InterruptedException {
        System.out.println("Этап 3. Lock-free singleton-кэш через AtomicReference");

        SingletonCache cache = new SingletonCache();
        int threadsCount = 50;
        Set<String> seenValues = ConcurrentHashMap.newKeySet();

        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < threadsCount; t++) {
            threads.add(new Thread(() -> {
                String value = cache.getOrCreate(() -> "cached-value-" + System.nanoTime());
                seenValues.add(value);
            }));
        }

        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }

        System.out.println("  потоков обратилось к кэшу: " + threadsCount);
        System.out.println("  объект создан раз: " + cache.getCreationCount());
        System.out.println("  различных значений увидели потоки: " + seenValues.size());
    }
}
