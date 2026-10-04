package ru.netology.httppool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpPoolDemo {

    private static final List<String> URLS = List.of(
            "https://httpbin.org/get",
            "https://httpbin.org/delay/1",
            "https://httpbin.org/status/200",
            "https://httpbin.org/status/404",
            "https://httpbin.org/uuid",
            "https://httpbin.org/ip",
            "https://httpbin.org/headers",
            "https://api.github.com",
            "https://api.github.com/users/octocat",
            "https://jsonplaceholder.typicode.com/posts/1",
            "https://jsonplaceholder.typicode.com/users",
            "https://jsonplaceholder.typicode.com/comments",
            "https://jsonplaceholder.typicode.com/todos/1",
            "https://jsonplaceholder.typicode.com/albums/1",
            "https://jsonplaceholder.typicode.com/photos/1",
            "https://catfact.ninja/fact",
            "https://api.adviceslip.com/advice",
            "https://dog.ceo/api/breeds/image/random",
            "https://official-joke-api.appspot.com/random_joke",
            "https://api.chucknorris.io/jokes/random"
    );

    public static void main(String[] args) throws InterruptedException {
        runCustomThreadPoolExecutor();
        System.out.println();
        comparePoolTypes();
    }

    private static void runCustomThreadPoolExecutor() throws InterruptedException {
        System.out.println("Этап 1. Запросы через собственный ThreadPoolExecutor");

        AtomicInteger threadCounter = new AtomicInteger();
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable, "http-pool-" + threadCounter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        };

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5, 10, 30, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(50),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy());

        System.out.println("  настройки пула: core=5, max=10, keepAlive=30s, queue=LinkedBlockingQueue(50)");

        List<Future<FetchResult>> futures = new ArrayList<>();
        long start = System.nanoTime();
        for (String url : URLS) {
            futures.add(executor.submit(new UrlFetcher(url)));
        }

        System.out.println("  во время выполнения: activeCount=" + executor.getActiveCount()
                + ", poolSize=" + executor.getPoolSize()
                + ", queueSize=" + executor.getQueue().size());

        List<FetchResult> results = new ArrayList<>();
        for (Future<FetchResult> future : futures) {
            try {
                results.add(future.get());
            } catch (ExecutionException e) {
                results.add(new FetchResult("unknown", -1, 0, e.getCause().getClass().getSimpleName()));
            }
        }
        long totalElapsed = (System.nanoTime() - start) / 1_000_000;

        System.out.println("  после завершения: completedTaskCount=" + executor.getCompletedTaskCount()
                + ", largestPoolSize=" + executor.getLargestPoolSize());

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        printSummary(results, totalElapsed);
    }

    private static void printSummary(List<FetchResult> results, long totalElapsedMillis) {
        System.out.println();
        System.out.println("  сводка по запросам:");
        for (FetchResult result : results) {
            String status = result.error() != null ? "ошибка: " + result.error() : String.valueOf(result.statusCode());
            System.out.printf("    %-55s status=%-20s время=%d мс%n", result.url(), status, result.elapsedMillis());
        }

        long successCount = results.stream().filter(FetchResult::isSuccess).count();
        long failureCount = results.size() - successCount;
        double averageMillis = results.stream().mapToLong(FetchResult::elapsedMillis).average().orElse(0);
        long maxMillis = results.stream().mapToLong(FetchResult::elapsedMillis).max().orElse(0);
        long minMillis = results.stream().mapToLong(FetchResult::elapsedMillis).min().orElse(0);

        System.out.println();
        System.out.println("  всего запросов: " + results.size());
        System.out.println("  успешных (2xx): " + successCount + ", неуспешных: " + failureCount);
        System.out.printf("  время ответа: мин=%d мс, среднее=%.1f мс, макс=%d мс%n", minMillis, averageMillis, maxMillis);
        System.out.println("  общее время выполнения всех запросов параллельно: " + totalElapsedMillis + " мс");
    }

    private static void comparePoolTypes() throws InterruptedException {
        System.out.println("Этап 2. Сравнение типов пулов на той же нагрузке");

        measurePool("SingleThreadExecutor", Executors.newSingleThreadExecutor());
        measurePool("FixedThreadPool(5)", Executors.newFixedThreadPool(5));
        measurePool("CachedThreadPool", Executors.newCachedThreadPool());
    }

    private static void measurePool(String name, ExecutorService executor) throws InterruptedException {
        List<Future<FetchResult>> futures = new ArrayList<>();
        long start = System.nanoTime();
        for (String url : URLS) {
            futures.add(executor.submit(new UrlFetcher(url)));
        }
        for (Future<FetchResult> future : futures) {
            try {
                future.get();
            } catch (ExecutionException ignored) {
                // результат не важен для замера времени, ошибки уже обработаны внутри UrlFetcher
            }
        }
        long elapsed = (System.nanoTime() - start) / 1_000_000;

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("  " + name + ": " + elapsed + " мс на " + URLS.size() + " запросов");
    }
}
