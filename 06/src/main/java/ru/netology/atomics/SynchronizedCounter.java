package ru.netology.atomics;

public class SynchronizedCounter {

    private int value;

    public synchronized void increment() {
        value++;
    }

    public synchronized int get() {
        return value;
    }
}
