package ru.netology.httppool;

public record FetchResult(String url, int statusCode, long elapsedMillis, String error) {

    public boolean isSuccess() {
        return error == null && statusCode >= 200 && statusCode < 300;
    }
}
