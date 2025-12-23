package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom.weatherdesktop.api.ApiClient;
import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.session.SessionContext;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.nhom.weatherdesktop.api.ApiClient.client;

public class StationService {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().findAndRegisterModules();

    public PageResponse<StationResponse> getMyStations(int page, int size) {
        try {
            String token = SessionContext.accessToken();
            if (token == null) {
                throw new RuntimeException("User not authenticated");
            }

            String url = String.format("%s/stations/user/me/stations?page=%d&size=%d",
                    ApiClient.baseUrl(), page, size);

            System.out.println("=== DEBUG: Station API Request ===");
            System.out.println("URL: " + url);
            System.out.println("Token: " + (token != null ? "Bearer " + token.substring(0, Math.min(20, token.length())) + "..." : "null"));

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client().send(httpRequest, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Raw Response Body:");
            System.out.println(response.body());
            System.out.println("=== END DEBUG ===");

            return switch (response.statusCode()) {
                case 200 -> MAPPER.readValue(response.body(),
                        new TypeReference<PageResponse<StationResponse>>() {});
                case 401 -> throw new RuntimeException("Unauthorized - Please login again");
                case 400 -> throw new RuntimeException("Invalid pagination parameters");
                default -> throw new RuntimeException(
                        "Server error (status=" + response.statusCode() + ")"
                );
            };

        } catch (Exception e) {
            throw new RuntimeException("Error fetching stations: " + e.getMessage(), e);
        }
    }
}
