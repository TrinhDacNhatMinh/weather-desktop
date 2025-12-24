package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom.weatherdesktop.api.ApiClient;
import com.nhom.weatherdesktop.dto.request.AddStationRequest;
import com.nhom.weatherdesktop.dto.request.UpdateStationRequest;
import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.util.HttpRequestBuilder;

import java.net.http.HttpResponse;

import static com.nhom.weatherdesktop.api.ApiClient.client;

public class StationService {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().findAndRegisterModules();


    public PageResponse<StationResponse> getMyStations(int page, int size) {
        try {
            var httpRequest = HttpRequestBuilder
                    .create("/stations/user/me/stations?page=" + page + "&size=" + size)
                    .withAuth()
                    .get()
                    .build();

            HttpResponse<String> response =
                    client().send(httpRequest, HttpResponse.BodyHandlers.ofString());

            return switch (response.statusCode()) {
                case 200 -> MAPPER.readValue(
                        response.body(),
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
            String json = MAPPER.writeValueAsString(request);

            var httpRequest = HttpRequestBuilder
                    .create("/stations/attach")
                    .withAuth()
                    .put(json)
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

    public StationResponse updateStation(Long id, UpdateStationRequest request) {
        try {
            String json = MAPPER.writeValueAsString(request);

            var httpRequest = HttpRequestBuilder
                    .create("/stations/" + id)
                    .withAuth()
                    .put(json)
                    .build();

            HttpResponse<String> response =
                    client().send(httpRequest, HttpResponse.BodyHandlers.ofString());

            return switch (response.statusCode()) {
                case 200 -> MAPPER.readValue(response.body(), StationResponse.class);
                case 400 -> throw new RuntimeException("Invalid update station request");
                case 401 -> throw new RuntimeException("Unauthorized - Please login again");
                case 403 -> throw new RuntimeException("Forbidden - You don't have permission to update this station");
                case 404 -> throw new RuntimeException("Station not found");
                default -> throw new RuntimeException(
                        "Server error (status=" + response.statusCode() + ")"
                );
            };

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating station: " + e.getMessage(), e);
        }
    }

    public StationResponse updateStationSharing(Long id) {
        try {
            var httpRequest = HttpRequestBuilder
                    .create("/stations/" + id + "/public")
                    .withAuth()
                    .put()
                    .build();

            HttpResponse<String> response =
                    client().send(httpRequest, HttpResponse.BodyHandlers.ofString());

            return switch (response.statusCode()) {
                case 200 -> MAPPER.readValue(response.body(), StationResponse.class);
                case 401 -> throw new RuntimeException("Unauthorized - Please login again");
                case 403 -> throw new RuntimeException("Forbidden - You don't have permission to update this station");
                case 404 -> throw new RuntimeException("Station not found");
                default -> throw new RuntimeException(
                        "Server error (status=" + response.statusCode() + ")"
                );
            };

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating station sharing status: " + e.getMessage(), e);
        }
    }

    public StationResponse getStationById(Long id) {
        try {
            var httpRequest = HttpRequestBuilder
                    .create("/stations/" + id)
                    .withAuth()
                    .get()
                    .build();

            HttpResponse<String> response =
                    client().send(httpRequest, HttpResponse.BodyHandlers.ofString());

            return switch (response.statusCode()) {
                case 200 -> MAPPER.readValue(response.body(), StationResponse.class);
                case 401 -> throw new RuntimeException("Unauthorized - Please login again");
                case 403 -> throw new RuntimeException("Forbidden - Access denied");
                case 404 -> throw new RuntimeException("Station not found");
                default -> throw new RuntimeException(
                        "Server error (status=" + response.statusCode() + ")"
                );
            };

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching station: " + e.getMessage(), e);
        }
    }
}

