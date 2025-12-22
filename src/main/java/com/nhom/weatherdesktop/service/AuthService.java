package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom.weatherdesktop.api.ApiClient;
import com.nhom.weatherdesktop.dto.request.LoginRequest;
import com.nhom.weatherdesktop.dto.response.LoginResponse;
import com.nhom.weatherdesktop.exception.AuthException;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.nhom.weatherdesktop.api.ApiClient.client;

public class AuthService {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().findAndRegisterModules();

    public LoginResponse login(LoginRequest request) {

        try {
            String json = MAPPER.writeValueAsString(request);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(ApiClient.baseUrl() + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response =
                    client().send(httpRequest, HttpResponse.BodyHandlers.ofString());

            return switch (response.statusCode()) {
                case 200 -> MAPPER.readValue(response.body(), LoginResponse.class);
                case 401 -> throw new AuthException("Invalid username or password");
                case 403 -> throw new AuthException("Account is not active or access channel not allowed");
                default -> throw new RuntimeException(
                        "Server error (status=" + response.statusCode() + ")"
                );
            };

        } catch (AuthException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Login error", e);
        }
    }

}
