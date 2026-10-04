package ru.netology.sync;

public class SafeCounter {

    private long value;

    public synchronized void increment() {
        value++;
    }

    public synchronized long getValue() {
        return value;
    }
}
