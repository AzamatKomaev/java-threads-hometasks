package ru.netology.buffer;

public class SynchronizedBoundedBuffer<T> implements Buffer<T> {

    private final Object[] items;
    private int putIndex;
    private int takeIndex;
    private int count;

    public SynchronizedBoundedBuffer(int capacity) {
        items = new Object[capacity];
    }

    @Override
    public synchronized void put(T item) throws InterruptedException {
        while (count == items.length) {
            wait();
        }
        items[putIndex] = item;
        putIndex = (putIndex + 1) % items.length;
        count++;
        notifyAll();
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized T take() throws InterruptedException {
        while (count == 0) {
            wait();
        }
        T item = (T) items[takeIndex];
        items[takeIndex] = null;
        takeIndex = (takeIndex + 1) % items.length;
        count--;
        notifyAll();
        return item;
    }
}
