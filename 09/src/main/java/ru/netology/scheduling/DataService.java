package ru.netology.scheduling;

import java.util.concurrent.ThreadLocalRandom;

public class DataService {

    public double fetchRate(String entityId) throws DataFetchException {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 400));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (ThreadLocalRandom.current().nextInt(100) < 20) {
            throw new DataFetchException("источник данных недоступен для " + entityId);
        }

        return Math.round(ThreadLocalRandom.current().nextDouble(0.5, 100) * 100) / 100.0;
    }
}
