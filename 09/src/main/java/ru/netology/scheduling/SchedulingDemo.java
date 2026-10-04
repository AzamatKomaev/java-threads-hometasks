package ru.netology.scheduling;

import java.util.List;

public class SchedulingDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Этап 1. Future и отмена долгой задачи через cancel()");
        FutureCancelDemo.run();

        System.out.println();
        System.out.println("Этап 2. Несколько задач через invokeAll()");
        InvokeAllDemo.run();

        System.out.println();
        System.out.println("Этап 3. Периодическая задача через ScheduledExecutorService");
        ScheduledTaskDemo.run();

        System.out.println();
        System.out.println("Этап 4. PeriodicDataAggregator - периодический опрос нескольких сущностей");
        List<String> currencies = List.of("USD", "EUR", "GBP", "JPY", "CNY");
        PeriodicDataAggregator aggregator = new PeriodicDataAggregator(currencies, new DataService());
        aggregator.start(2);
        Thread.sleep(4500);
        aggregator.stop();
        System.out.println("  PeriodicDataAggregator остановлен");
    }
}
