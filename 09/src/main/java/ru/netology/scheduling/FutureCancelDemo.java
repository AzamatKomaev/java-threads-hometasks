package ru.netology.scheduling;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class FutureCancelDemo {

    public static void run() throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<String> future = executor.submit(() -> {
            System.out.println("  долгая задача начала работу (имитация 6 секунд)");
            Thread.sleep(6000);
            return "результат долгой задачи";
        });

        Thread.sleep(2500);
        System.out.println("  прошло 2.5 секунды, отменяем задачу через cancel(true)");
        boolean cancelled = future.cancel(true);
        System.out.println("  cancel() вернул: " + cancelled);
        System.out.println("  isCancelled(): " + future.isCancelled() + ", isDone(): " + future.isDone());

        try {
            future.get();
        } catch (CancellationException e) {
            System.out.println("  future.get() выбросил CancellationException - задача действительно отменена");
        } catch (ExecutionException e) {
            System.out.println("  future.get() выбросил ExecutionException: " + e.getCause());
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}
