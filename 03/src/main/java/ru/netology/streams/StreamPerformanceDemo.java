package ru.netology.streams;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StreamPerformanceDemo {

    private static final int SIZE = 1_000_000;
    private static final int WARMUP_ITERATIONS = 3;
    private static final int MEASURED_ITERATIONS = 5;

    public static void main(String[] args) {
        List<Integer> numbers = generateNumbers(SIZE);

        System.out.println("Размер списка: " + SIZE);
        System.out.println("Доступно ядер процессора: " + Runtime.getRuntime().availableProcessors());
        System.out.println();

        long sequentialAvg = measure("Последовательный stream()", numbers, false);
        long parallelAvg = measure("Параллельный parallelStream()", numbers, true);

        System.out.println();
        System.out.println("Итог:");
        System.out.println("Среднее время stream(): " + toMillis(sequentialAvg) + " мс");
        System.out.println("Среднее время parallelStream(): " + toMillis(parallelAvg) + " мс");
        System.out.println("Ускорение: " + String.format("%.2f", (double) sequentialAvg / parallelAvg) + "x");
    }

    private static List<Integer> generateNumbers(int size) {
        return ThreadLocalRandom.current()
                .ints(size, 1, 1_000_001)
                .boxed()
                .collect(Collectors.toList());
    }

    private static long measure(String label, List<Integer> numbers, boolean parallel) {
        // прогревочные прогоны нужны, чтобы JIT-компиляция не искажала замер основного времени
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            process(numbers, parallel);
        }

        long totalNanos = 0;
        long lastSum = 0;
        System.out.println(label + ":");
        for (int i = 1; i <= MEASURED_ITERATIONS; i++) {
            long start = System.nanoTime();
            lastSum = process(numbers, parallel);
            long elapsed = System.nanoTime() - start;
            totalNanos += elapsed;
            System.out.println("  прогон " + i + ": " + toMillis(elapsed) + " мс");
        }

        long average = totalNanos / MEASURED_ITERATIONS;
        System.out.println("  среднее: " + toMillis(average) + " мс, сумма: " + lastSum);
        return average;
    }

    private static long process(List<Integer> numbers, boolean parallel) {
        Stream<Integer> source = parallel ? numbers.parallelStream() : numbers.stream();
        return source
                .filter(n -> n % 2 == 0)
                .mapToLong(n -> (long) n * 2)
                .sum();
    }

    private static long toMillis(long nanos) {
        return nanos / 1_000_000;
    }
}
