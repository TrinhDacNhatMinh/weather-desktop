package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom.weatherdesktop.dto.request.UpdateThresholdRequest;
import com.nhom.weatherdesktop.dto.response.ThresholdResponse;
import com.nhom.weatherdesktop.util.HttpRequestBuilder;
import com.nhom.weatherdesktop.util.ResponseHandler;
import com.nhom.weatherdesktop.exception.AppException;

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

            return ResponseHandler.handle(response, ThresholdResponse.class, MAPPER);

        } catch (AppException e) {
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

            return ResponseHandler.handle(response, ThresholdResponse.class, MAPPER);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error updating threshold: " + e.getMessage(), e);
        }
    }
}
