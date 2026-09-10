package ru.netology.lifecycle;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadStateWatcher implements Runnable {

    private final List<Thread> threads;
    private final Map<Thread, Thread.State> lastStates = new ConcurrentHashMap<>();
    private final long pollIntervalMillis;

    public ThreadStateWatcher(List<Thread> threads, long pollIntervalMillis) {
        this.threads = threads;
        this.pollIntervalMillis = pollIntervalMillis;
        for (Thread thread : threads) {
            Thread.State state = thread.getState();
            lastStates.put(thread, state);
            System.out.println(thread.getName() + ": " + state);
        }
    }

    @Override
    public void run() {
        boolean allTerminated;
        do {
            allTerminated = true;
            for (Thread thread : threads) {
                Thread.State current = thread.getState();
                Thread.State previous = lastStates.get(thread);
                if (current != previous) {
                    System.out.println(thread.getName() + ": " + previous + " -> " + current);
                    lastStates.put(thread, current);
                }
                if (current != Thread.State.TERMINATED) {
                    allTerminated = false;
                }
            }
            sleepQuietly(pollIntervalMillis);
        } while (!allTerminated);
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
