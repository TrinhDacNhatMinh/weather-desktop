package com.nhom.weatherdesktop.api;

import com.nhom.weatherdesktop.config.AppConfig;

import java.net.http.HttpClient;
import java.time.Duration;

public class ApiClient {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(AppConfig.getApiTimeout()))
            .build();

    public static HttpClient client() {
        return CLIENT;
    }

    public static String baseUrl() {
        return AppConfig.getApiBaseUrl();
    }
}
