package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom.weatherdesktop.api.ApiClient;
import com.nhom.weatherdesktop.dto.request.AddStationRequest;
import com.nhom.weatherdesktop.dto.request.UpdateStationRequest;
import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.util.HttpRequestBuilder;
import com.nhom.weatherdesktop.util.ResponseHandler;
import com.nhom.weatherdesktop.exception.AppException;

import java.net.http.HttpResponse;

import static com.nhom.weatherdesktop.api.ApiClient.client;

public class StationService implements IStationService {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().findAndRegisterModules();



    public PageResponse<StationResponse> getMyStations(int page, int size) {
        try {
            var httpRequest = HttpRequestBuilder
                    .create("/stations/user/me/stations?page=" + page + "&size=" + size)
                    .withAuth()
                    .get()
                    .build();

            // Use sendWithRefresh for automatic token refresh on 401
            HttpResponse<String> response = HttpRequestBuilder.sendWithRefresh(httpRequest);

            return ResponseHandler.handle(response, 
                new TypeReference<PageResponse<StationResponse>>() {}, MAPPER);

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

            return ResponseHandler.handle(response, StationResponse.class, MAPPER);

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

            return ResponseHandler.handle(response, StationResponse.class, MAPPER);

        } catch (AppException e) {
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

            return ResponseHandler.handle(response, StationResponse.class, MAPPER);

        } catch (AppException e) {
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

            return ResponseHandler.handle(response, StationResponse.class, MAPPER);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching station: " + e.getMessage(), e);
        }
    }
    
    public void detachStationFromUser(Long id) {
        try {
            var httpRequest = HttpRequestBuilder
                    .create("/stations/" + id + "/user")
                    .withAuth()
                    .delete()
                    .build();

            HttpResponse<String> response =
                    client().send(httpRequest, HttpResponse.BodyHandlers.ofString());

            ResponseHandler.handleVoid(response);

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error detaching station: " + e.getMessage(), e);
        }
    }
}

