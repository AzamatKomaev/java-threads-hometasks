package ru.netology.threads;

import org.springframework.stereotype.Service;

@Service
public class ThreadDemoService {

    private static final int ITERATIONS = 5;

    public void runDemo() throws InterruptedException {
        Thread counterWorker = new CounterWorker("CounterWorker", ITERATIONS);
        Thread loggerThread = new Thread(new LoggerTask(ITERATIONS), "LoggerThread");

        counterWorker.start();
        loggerThread.start();

        Thread.sleep(20);
        printActiveThreads();

        counterWorker.join();
        loggerThread.join();
    }

    private void printActiveThreads() {
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        while (rootGroup.getParent() != null) {
            rootGroup = rootGroup.getParent();
        }

        Thread[] threads = new Thread[rootGroup.activeCount() * 2];
        int count = rootGroup.enumerate(threads, true);

        System.out.println("Список активных потоков (" + count + "):");
        for (int i = 0; i < count; i++) {
            System.out.println(" - " + threads[i].getName());
        }
    }
}
