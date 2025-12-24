package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom.weatherdesktop.dto.request.UpdateThresholdRequest;
import com.nhom.weatherdesktop.dto.response.ThresholdResponse;
import com.nhom.weatherdesktop.util.HttpRequestBuilder;

import java.net.http.HttpResponse;

import static com.nhom.weatherdesktop.api.ApiClient.client;

public class ThresholdService implements IThresholdService {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().findAndRegisterModules();

    public ThresholdResponse getThresholdByStationId(Long stationId) {
        try {
            var httpRequest = HttpRequestBuilder
                    .create("/stations/" + stationId + "/threshold")
                    .withAuth()
                    .get()
                    .build();

            HttpResponse<String> response =
                    client().send(httpRequest, HttpResponse.BodyHandlers.ofString());

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
            String json = MAPPER.writeValueAsString(request);

            var httpRequest = HttpRequestBuilder
                    .create("/thresholds/" + id)
                    .withAuth()
                    .put(json)
                    .build();

            HttpResponse<String> response =
                    client().send(httpRequest, HttpResponse.BodyHandlers.ofString());

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
