package ru.netology.sync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataCollector {

    private final List<Item> items = new ArrayList<>();
    private final Set<String> processedKeys = new HashSet<>();
    private final int expectedTotal;
    private int processedCount;
    private long sum;

    public DataCollector(int expectedTotal) {
        this.expectedTotal = expectedTotal;
    }

    public synchronized void collectItem(Item item) {
        items.add(item);
        sum += item.value();
    }

    public synchronized boolean isAlreadyProcessed(String key) {
        return !processedKeys.add(key);
    }

    public synchronized void incrementProcessed() {
        processedCount++;
        if (processedCount >= expectedTotal) {
            notifyAll();
        }
    }

    public synchronized void awaitCompletion() throws InterruptedException {
        while (processedCount < expectedTotal) {
            wait();
        }
    }

    public synchronized int getProcessedCount() {
        return processedCount;
    }

    public synchronized int getItemsCount() {
        return items.size();
    }

    public synchronized long getSum() {
        return sum;
    }
}
