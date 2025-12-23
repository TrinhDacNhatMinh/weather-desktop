package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom.weatherdesktop.api.ApiClient;
import com.nhom.weatherdesktop.dto.request.AddStationRequest;
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

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    client().send(httpRequest, HttpResponse.BodyHandlers.ofString());

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
    
    public StationResponse addStationToUser(AddStationRequest request) {
        try {
            String token = SessionContext.accessToken();
            if (token == null) {
                throw new RuntimeException("User not authenticated");
            }

            String url = ApiClient.baseUrl() + "/stations/attach";
            String json = MAPPER.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client().send(httpRequest, HttpResponse.BodyHandlers.ofString());

            return switch (response.statusCode()) {
                case 200 -> MAPPER.readValue(response.body(), StationResponse.class);
                case 400 -> throw new RuntimeException("Invalid request data");
                case 401 -> throw new RuntimeException("Unauthorized - Please login again");
                case 404 -> throw new RuntimeException("Station not found with this API key");
                case 409 -> throw new RuntimeException("Station already attached to another user");
                default -> throw new RuntimeException(
                        "Server error (status=" + response.statusCode() + ")"
                );
            };

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error adding station: " + e.getMessage(), e);
        }
    }
}
