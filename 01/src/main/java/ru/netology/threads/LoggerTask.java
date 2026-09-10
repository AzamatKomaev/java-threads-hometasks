package ru.netology.threads;

public class LoggerTask implements Runnable {

    private final int iterations;

    public LoggerTask(int iterations) {
        this.iterations = iterations;
    }

    @Override
    public void run() {
        String name = Thread.currentThread().getName();
        for (int i = 1; i <= iterations; i++) {
            System.out.println(name + ", порядковый номер: " + i);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
