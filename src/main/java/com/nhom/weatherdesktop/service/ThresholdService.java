package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom.weatherdesktop.api.ApiClient;
import com.nhom.weatherdesktop.dto.request.UpdateThresholdRequest;
import com.nhom.weatherdesktop.dto.response.ThresholdResponse;
import com.nhom.weatherdesktop.session.SessionContext;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.nhom.weatherdesktop.api.ApiClient.client;

public class ThresholdService {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().findAndRegisterModules();

    public ThresholdResponse getThresholdByStationId(Long stationId) {
        try {
            String token = SessionContext.accessToken();
            if (token == null) {
                throw new RuntimeException("User not authenticated");
            }

            String url = ApiClient.baseUrl() + "/stations/" + stationId + "/threshold";
            
            // Debug logging
            System.out.println("=== GET THRESHOLD DEBUG ===");
            System.out.println("URL: " + url);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client().send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // Debug logging
            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
            System.out.println("===========================");

            return switch (response.statusCode()) {
                case 200 -> MAPPER.readValue(response.body(), ThresholdResponse.class);
                case 401 -> throw new RuntimeException("Unauthorized - Please login again");
                case 403 -> throw new RuntimeException("Forbidden - Access denied");
                case 404 -> throw new RuntimeException("Station or threshold not found");
                default -> throw new RuntimeException(
                        "Server error (status=" + response.statusCode() + "). Response: " + response.body()
                );
            };

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching threshold: " + e.getMessage(), e);
        }
    }

    public ThresholdResponse updateThreshold(Long id, UpdateThresholdRequest request) {
        try {
            String token = SessionContext.accessToken();
            if (token == null) {
                throw new RuntimeException("User not authenticated");
            }

            String url = ApiClient.baseUrl() + "/thresholds/" + id;
            String json = MAPPER.writeValueAsString(request);
            
            // Debug logging
            System.out.println("=== UPDATE THRESHOLD DEBUG ===");
            System.out.println("URL: " + url);
            System.out.println("Request JSON: " + json);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client().send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // Debug logging
            System.out.println("Response Status: " + response.statusCode());
            System.out.println("Response Body: " + response.body());
            System.out.println("==============================");

            return switch (response.statusCode()) {
                case 200 -> MAPPER.readValue(response.body(), ThresholdResponse.class);
                case 400 -> throw new RuntimeException("Invalid threshold update request");
                case 401 -> throw new RuntimeException("Unauthorized - Please login again");
                case 403 -> throw new RuntimeException("Forbidden - You don't have permission to update this threshold");
                case 404 -> throw new RuntimeException("Threshold not found");
                default -> throw new RuntimeException(
                        "Server error (status=" + response.statusCode() + "). Response: " + response.body()
                );
            };

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating threshold: " + e.getMessage(), e);
        }
    }
}
