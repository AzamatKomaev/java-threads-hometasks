package ru.netology.hazards;

public class HazardsDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Этап 1. Livelock без защиты (симметричный retry + yield)");
        LivelockDemo.Result livelockResult = LivelockDemo.run(500, false);
        System.out.println("  Thread-1: retries=" + livelockResult.retries1()
                + ", завершил работу=" + livelockResult.completed1());
        System.out.println("  Thread-2: retries=" + livelockResult.retries2()
                + ", завершил работу=" + livelockResult.completed2());

        System.out.println();
        System.out.println("Этап 2. Livelock с защитой (случайный backoff перед повтором)");
        LivelockDemo.Result backoffResult = LivelockDemo.run(500, true);
        System.out.println("  Thread-1: retries=" + backoffResult.retries1()
                + ", завершил работу=" + backoffResult.completed1());
        System.out.println("  Thread-2: retries=" + backoffResult.retries2()
                + ", завершил работу=" + backoffResult.completed2());

        System.out.println();
        System.out.println("Этап 3. Starvation на нечестном ReentrantLock(false)");
        long[] unfair = StarvationDemo.run(300, false);
        printDistribution(unfair);

        System.out.println();
        System.out.println("Этап 4. Предотвращение Starvation через ReentrantLock(true)");
        long[] fair = StarvationDemo.run(300, true);
        printDistribution(fair);

        System.out.println();
        System.out.println("Этап 5. Предотвращение Deadlock через упорядоченный захват ресурсов");
        boolean finishedWithoutDeadlock = DeadlockPreventionDemo.run();
        System.out.println("  оба потока завершились без зависания: " + finishedWithoutDeadlock);
    }

    private static void printDistribution(long[] counts) {
        long total = 0;
        for (long count : counts) {
            total += count;
        }
        for (int i = 0; i < counts.length; i++) {
            double share = total == 0 ? 0 : counts[i] * 100.0 / total;
            System.out.printf("  Worker-%d: %d итераций (%.1f%%)%n", i, counts[i], share);
        }
    }
}
