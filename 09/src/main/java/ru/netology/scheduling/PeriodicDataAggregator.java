package ru.netology.scheduling;

import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PeriodicDataAggregator {

    private record EntityOutcome(String entityId, Double rate, String error) {
    }

    private final List<String> entityIds;
    private final DataService dataService;
    private final ExecutorService workerPool;
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger cycleCount = new AtomicInteger();

    public PeriodicDataAggregator(List<String> entityIds, DataService dataService) {
        this.entityIds = entityIds;
        this.dataService = dataService;
        this.workerPool = Executors.newFixedThreadPool(entityIds.size());
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start(long intervalSeconds) {
        scheduler.scheduleAtFixedRate(this::runCycle, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    public void stop() throws InterruptedException {
        scheduler.shutdown();
        workerPool.shutdown();
        scheduler.awaitTermination(5, TimeUnit.SECONDS);
        workerPool.awaitTermination(5, TimeUnit.SECONDS);
    }

    private void runCycle() {
        int cycle = cycleCount.incrementAndGet();
        System.out.println("  [" + LocalTime.now().withNano(0) + "] цикл #" + cycle
                + ": запрашиваем данные по " + entityIds.size() + " сущностям");

        List<Callable<EntityOutcome>> tasks = entityIds.stream()
                .<Callable<EntityOutcome>>map(id -> () -> fetchOne(id))
                .toList();

        try {
            List<Future<EntityOutcome>> futures = workerPool.invokeAll(tasks, 2, TimeUnit.SECONDS);
            int success = 0;
            int failed = 0;
            for (Future<EntityOutcome> future : futures) {
                EntityOutcome outcome = future.get();
                if (outcome.error() == null) {
                    success++;
                    System.out.println("    " + outcome.entityId() + " -> " + outcome.rate());
                } else {
                    failed++;
                    System.out.println("    " + outcome.entityId() + " -> ошибка: " + outcome.error());
                }
            }
            System.out.println("  цикл #" + cycle + " завершён: успешно=" + success + ", с ошибкой=" + failed);
        } catch (Exception e) {
            System.out.println("  цикл #" + cycle + " прерван: " + e);
        }
    }

    private EntityOutcome fetchOne(String entityId) {
        try {
            double rate = dataService.fetchRate(entityId);
            return new EntityOutcome(entityId, rate, null);
        } catch (DataFetchException e) {
            return new EntityOutcome(entityId, null, e.getMessage());
        }
    }
}
