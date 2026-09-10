package ru.netology.threads;

public class CounterWorker extends Thread {

    private final int iterations;

    public CounterWorker(String name, int iterations) {
        super(name);
        this.iterations = iterations;
    }

    @Override
    public void run() {
        for (int i = 1; i <= iterations; i++) {
            System.out.println(getName() + ", порядковый номер: " + i);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
