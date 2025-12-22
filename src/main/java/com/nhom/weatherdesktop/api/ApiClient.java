package com.nhom.weatherdesktop.api;

import java.net.http.HttpClient;
import java.time.Duration;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static HttpClient client() {
        return CLIENT;
    }

    public static String baseUrl() {
        return BASE_URL;
    }
}
