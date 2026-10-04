package ru.netology.scheduling;

import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ScheduledTaskDemo {

    public static void run() throws InterruptedException {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        AtomicInteger runCount = new AtomicInteger();

        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(() -> {
            int run = runCount.incrementAndGet();
            System.out.println("  [" + LocalTime.now().withNano(0) + "] проверка статуса системы, запуск #" + run);
        }, 0, 1, TimeUnit.SECONDS);

        Thread.sleep(4200);
        scheduledFuture.cancel(false);
        System.out.println("  периодическая задача остановлена после " + runCount.get() + " запусков");

        scheduler.shutdown();
        scheduler.awaitTermination(5, TimeUnit.SECONDS);
    }
}
