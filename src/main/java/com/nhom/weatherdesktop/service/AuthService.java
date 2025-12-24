package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom.weatherdesktop.dto.request.LoginRequest;
import com.nhom.weatherdesktop.dto.response.LoginResponse;
import com.nhom.weatherdesktop.exception.AuthException;
import com.nhom.weatherdesktop.util.HttpRequestBuilder;

import java.net.http.HttpResponse;

import static com.nhom.weatherdesktop.api.ApiClient.client;

public class AuthService {

    private static final ObjectMapper MAPPER =
            new ObjectMapper().findAndRegisterModules();

    public LoginResponse login(LoginRequest request) {

        try {
            String json = MAPPER.writeValueAsString(request);

            var httpRequest = HttpRequestBuilder
                    .create("/auth/login")
                    .post(json)
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
