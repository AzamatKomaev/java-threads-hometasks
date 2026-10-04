package ru.netology.atomics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class SingletonCache {

    private final AtomicReference<String> value = new AtomicReference<>();
    private final AtomicInteger creationCount = new AtomicInteger();

    public String getOrCreate(Supplier<String> supplier) {
        String current = value.get();
        if (current != null) {
            return current;
        }
        String created = supplier.get();
        if (value.compareAndSet(null, created)) {
            creationCount.incrementAndGet();
            return created;
        }
        return value.get();
    }

    public int getCreationCount() {
        return creationCount.get();
    }
}
