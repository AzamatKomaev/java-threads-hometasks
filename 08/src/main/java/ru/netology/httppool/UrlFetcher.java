package ru.netology.httppool;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Callable;

public class UrlFetcher implements Callable<FetchResult> {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final String url;

    public UrlFetcher(String url) {
        this.url = url;
    }

    @Override
    public FetchResult call() {
        long start = System.nanoTime();
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<Void> response = CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            return new FetchResult(url, response.statusCode(), elapsed, null);
        } catch (Exception e) {
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            return new FetchResult(url, -1, elapsed, e.getClass().getSimpleName());
        }
    }
}
