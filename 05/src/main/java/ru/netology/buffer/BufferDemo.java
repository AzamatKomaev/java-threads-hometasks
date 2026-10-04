package ru.netology.buffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class BufferDemo {

    private static final int CAPACITY = 16;
    private static final int PRODUCERS = 4;
    private static final int CONSUMERS = 4;
    private static final int ITEMS_PER_PRODUCER = 50_000;

    public static void main(String[] args) throws InterruptedException {
        runCorrectnessDemo();
        System.out.println();
        runPerformanceComparison();
    }

    private static void runCorrectnessDemo() throws InterruptedException {
        System.out.println("Этап 1. Корректность BoundedBuffer (ReentrantLock + Condition)");
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(CAPACITY);
        Result result = runWorkload(buffer, PRODUCERS, CONSUMERS, ITEMS_PER_PRODUCER);

        long expectedCount = (long) PRODUCERS * ITEMS_PER_PRODUCER;
        System.out.println("  произведено элементов: " + result.producedCount
                + ", ожидалось: " + expectedCount);
        System.out.println("  потреблено элементов: " + result.consumedCount
                + ", ожидалось: " + expectedCount);
        System.out.println("  сумма произведённых значений: " + result.producedSum.get());
        System.out.println("  сумма потреблённых значений: " + result.consumedSum.get());
        System.out.println("  корректность сохранена (суммы равны): "
                + (result.producedSum.get() == result.consumedSum.get()));
        System.out.println("  время выполнения: " + result.elapsedMillis + " мс");
    }

    private static void runPerformanceComparison() throws InterruptedException {
        System.out.println("Этап 2. Сравнение производительности: ReentrantLock vs synchronized");

        BoundedBuffer<Integer> lockBuffer = new BoundedBuffer<>(CAPACITY);
        Result lockResult = runWorkload(lockBuffer, PRODUCERS, CONSUMERS, ITEMS_PER_PRODUCER);
        System.out.println("  BoundedBuffer (ReentrantLock): " + lockResult.elapsedMillis + " мс, "
                + "обработано " + lockResult.consumedCount + " элементов");

        SynchronizedBoundedBuffer<Integer> syncBuffer = new SynchronizedBoundedBuffer<>(CAPACITY);
        Result syncResult = runWorkload(syncBuffer, PRODUCERS, CONSUMERS, ITEMS_PER_PRODUCER);
        System.out.println("  SynchronizedBoundedBuffer (synchronized/wait/notifyAll): "
                + syncResult.elapsedMillis + " мс, обработано " + syncResult.consumedCount + " элементов");
    }

    private static Result runWorkload(Buffer<Integer> buffer, int producers, int consumers,
                                       int itemsPerProducer) throws InterruptedException {
        AtomicLong producedSum = new AtomicLong();
        AtomicLong consumedSum = new AtomicLong();
        AtomicLong producedCount = new AtomicLong();
        AtomicLong consumedCount = new AtomicLong();
        long expectedCount = (long) producers * itemsPerProducer;

        List<Thread> producerThreads = new ArrayList<>();
        for (int p = 0; p < producers; p++) {
            producerThreads.add(new Thread(() -> {
                for (int i = 0; i < itemsPerProducer; i++) {
                    try {
                        buffer.put(i);
                        producedSum.addAndGet(i);
                        producedCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }));
        }

        int baseQuota = (int) (expectedCount / consumers);
        int remainder = (int) (expectedCount % consumers);

        List<Thread> consumerThreads = new ArrayList<>();
        for (int c = 0; c < consumers; c++) {
            int quota = baseQuota + (c < remainder ? 1 : 0);
            consumerThreads.add(new Thread(() -> {
                for (int i = 0; i < quota; i++) {
                    try {
                        Integer item = buffer.take();
                        consumedSum.addAndGet(item);
                        consumedCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }));
        }

        long start = System.nanoTime();
        consumerThreads.forEach(Thread::start);
        producerThreads.forEach(Thread::start);

        for (Thread producer : producerThreads) {
            producer.join();
        }
        for (Thread consumer : consumerThreads) {
            consumer.join();
        }
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        return new Result(producedCount.get(), consumedCount.get(), producedSum, consumedSum, elapsed);
    }

    private record Result(long producedCount, long consumedCount, AtomicLong producedSum,
                           AtomicLong consumedSum, long elapsedMillis) {
    }
}
