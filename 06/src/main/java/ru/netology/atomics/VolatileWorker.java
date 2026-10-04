package ru.netology.atomics;

import java.util.concurrent.atomic.AtomicLong;

public class VolatileWorker implements Runnable {

    private volatile boolean stopped;
    private final AtomicLong iterations = new AtomicLong();

    public void stop() {
        stopped = true;
    }

    @Override
    public void run() {
        while (!stopped) {
            iterations.incrementAndGet();
        }
    }

    public long getIterations() {
        return iterations.get();
    }
}
