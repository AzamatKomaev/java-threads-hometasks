package ru.netology.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SynchronizationDemo {

    public static void main(String[] args) throws InterruptedException {
        runDataCollectorDemo();
        System.out.println();
        runBankTransferDemo();
        System.out.println();
        runCounterBenchmark();
    }

    private static void runDataCollectorDemo() throws InterruptedException {
        System.out.println("Этап 1. DataCollector: synchronized + wait()/notifyAll()");

        int producers = 5;
        int itemsPerProducer = 1000;
        int expectedTotal = producers * itemsPerProducer;
        DataCollector collector = new DataCollector(expectedTotal);

        List<Thread> workers = new ArrayList<>();
        for (int p = 0; p < producers; p++) {
            int producerId = p;
            Thread worker = new Thread(() -> {
                for (int i = 0; i < itemsPerProducer; i++) {
                    String key = "producer-" + producerId + "-item-" + i;
                    if (!collector.isAlreadyProcessed(key)) {
                        collector.collectItem(new Item(key, i));
                        collector.incrementProcessed();
                    }
                }
            }, "Producer-" + p);
            workers.add(worker);
        }

        Thread reporter = new Thread(() -> {
            try {
                collector.awaitCompletion();
                System.out.println("  reporter: получено уведомление, обработано "
                        + collector.getProcessedCount() + " элементов");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Reporter");

        reporter.start();
        workers.forEach(Thread::start);
        for (Thread worker : workers) {
            worker.join();
        }
        reporter.join();

        System.out.println("  итого собрано элементов: " + collector.getItemsCount()
                + ", ожидалось: " + expectedTotal);
        System.out.println("  сумма значений: " + collector.getSum());
    }

    private static void runBankTransferDemo() throws InterruptedException {
        System.out.println("Этап 2. Переводы между счетами: порядок захвата блокировок против deadlock");

        int accountsCount = 6;
        long initialBalance = 1000;
        List<BankAccount> accounts = new ArrayList<>();
        for (int i = 0; i < accountsCount; i++) {
            accounts.add(new BankAccount(i, initialBalance));
        }
        long totalBefore = accounts.stream().mapToLong(BankAccount::getBalance).sum();

        TransferService transferService = new TransferService();
        int threadsCount = 8;
        int transfersPerThread = 2000;
        AtomicInteger rejected = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(threadsCount);
        for (int t = 0; t < threadsCount; t++) {
            pool.submit(() -> {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                for (int i = 0; i < transfersPerThread; i++) {
                    int fromIdx = random.nextInt(accountsCount);
                    int toIdx = random.nextInt(accountsCount);
                    if (fromIdx == toIdx) {
                        continue;
                    }
                    long amount = random.nextInt(50) + 1;
                    try {
                        transferService.transfer(accounts.get(fromIdx), accounts.get(toIdx), amount);
                    } catch (TransferService.InsufficientFundsException e) {
                        rejected.incrementAndGet();
                    }
                }
            });
        }

        pool.shutdown();
        boolean finished = pool.awaitTermination(30, TimeUnit.SECONDS);

        long totalAfter = accounts.stream().mapToLong(BankAccount::getBalance).sum();

        System.out.println("  завершилось без зависания за 30 секунд: " + finished);
        System.out.println("  сумма балансов до переводов: " + totalBefore);
        System.out.println("  сумма балансов после переводов: " + totalAfter);
        System.out.println("  отклонено переводов из-за нехватки средств: " + rejected.get());
        System.out.println("  корректность сохранена (суммы равны): " + (totalBefore == totalAfter));
    }

    private static void runCounterBenchmark() throws InterruptedException {
        System.out.println("Этап 3. Производительность: synchronized vs без синхронизации");

        int threadsCount = 8;
        int incrementsPerThread = 200_000;
        long expected = (long) threadsCount * incrementsPerThread;

        UnsafeCounter unsafeCounter = new UnsafeCounter();
        long unsafeTime = runIncrements(threadsCount, incrementsPerThread, unsafeCounter::increment);
        System.out.println("  без synchronized: ожидалось " + expected
                + ", получено " + unsafeCounter.getValue()
                + ", потеряно инкрементов " + (expected - unsafeCounter.getValue())
                + ", время " + unsafeTime + " мс");

        SafeCounter safeCounter = new SafeCounter();
        long safeTime = runIncrements(threadsCount, incrementsPerThread, safeCounter::increment);
        System.out.println("  с synchronized: ожидалось " + expected
                + ", получено " + safeCounter.getValue()
                + ", время " + safeTime + " мс");
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
}
