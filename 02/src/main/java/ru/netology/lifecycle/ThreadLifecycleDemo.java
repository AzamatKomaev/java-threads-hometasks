package ru.netology.lifecycle;

import java.util.List;

public class ThreadLifecycleDemo {

    public static void main(String[] args) throws InterruptedException {
        Object lock = new Object();
        Object waitLock = new Object();

        Thread lockHolder = new Thread(() -> {
            synchronized (lock) {
                sleepQuietly(1500);
            }
        }, "LockHolder");

        Thread blockedWorker = new Thread(() -> {
            synchronized (lock) {
                sleepQuietly(500);
            }
        }, "BlockedWorker");

        Thread waitingWorker = new Thread(() -> {
            synchronized (waitLock) {
                try {
                    waitLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "WaitingWorker");

        Thread notifier = new Thread(() -> {
            sleepQuietly(1000);
            synchronized (waitLock) {
                waitLock.notify();
            }
        }, "Notifier");

        List<Thread> observed = List.of(lockHolder, blockedWorker, waitingWorker);
        Thread watcherThread = new Thread(new ThreadStateWatcher(observed, 50), "StateWatcher");

        watcherThread.start();
        lockHolder.start();
        sleepQuietly(100);
        blockedWorker.start();
        waitingWorker.start();
        notifier.start();

        lockHolder.join();
        blockedWorker.join();
        waitingWorker.join();
        notifier.join();
        watcherThread.join();
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
