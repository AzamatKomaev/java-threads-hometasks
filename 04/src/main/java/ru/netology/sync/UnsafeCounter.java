package ru.netology.sync;

public class UnsafeCounter {

    private long value;

    public void increment() {
        value++;
    }

    public long getValue() {
        return value;
    }
}
