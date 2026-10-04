package ru.netology.hazards;

public class DeadlockDemo {

    private static final Object resourceA = new Object();
    private static final Object resourceB = new Object();

    public static void main(String[] args) {
        Thread thread1 = new Thread(() -> {
            synchronized (resourceA) {
                System.out.println("Thread-1 захватил resourceA, пытается захватить resourceB");
                sleepQuietly(300);
                synchronized (resourceB) {
                    System.out.println("Thread-1 захватил оба ресурса");
                }
            }
        }, "Thread-1");

        Thread thread2 = new Thread(() -> {
            synchronized (resourceB) {
                System.out.println("Thread-2 захватил resourceB, пытается захватить resourceA");
                sleepQuietly(300);
                synchronized (resourceA) {
                    System.out.println("Thread-2 захватил оба ресурса");
                }
            }
        }, "Thread-2");

        thread1.start();
        thread2.start();
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
