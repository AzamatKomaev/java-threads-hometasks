package ru.netology.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class InvokeAllDemo {

    private record TaskResult(int taskId, long durationMillis, int value) {
    }

    public static void run() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        List<Callable<TaskResult>> tasks = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            int taskId = i;
            tasks.add(() -> {
                int durationMillis = ThreadLocalRandom.current().nextInt(1000, 3001);
                Thread.sleep(durationMillis);
                int value = ThreadLocalRandom.current().nextInt(100);
                return new TaskResult(taskId, durationMillis, value);
            });
        }

        long start = System.nanoTime();
        List<Future<TaskResult>> futures = executor.invokeAll(tasks);
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        for (Future<TaskResult> future : futures) {
            try {
                TaskResult result = future.get();
                System.out.println("  задача " + result.taskId() + ": длилась " + result.durationMillis()
                        + " мс, результат=" + result.value());
            } catch (Exception e) {
                System.out.println("  задача завершилась с ошибкой: " + e);
            }
        }
        System.out.println("  invokeAll() дождался всех 5 задач за " + elapsed + " мс");

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}
