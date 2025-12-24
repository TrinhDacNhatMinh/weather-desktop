package com.nhom.weatherdesktop.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeocodingUtil {
    
    public static String reverseGeocode(double lat, double lng) {
        try {
            String url = String.format(
                "https://nominatim.openstreetmap.org/reverse?format=json&lat=%.6f&lon=%.6f&zoom=18&addressdetails=1",
                lat, lng
            );
            
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "WeatherDesktop/1.0")
                .GET()
                .build();
                
            HttpResponse<String> response = 
                client.send(request, HttpResponse.BodyHandlers.ofString());
                
            if (response.statusCode() == 200) {
                JsonNode json = new ObjectMapper().readTree(response.body());
                    
                if (json.has("display_name")) {
                    return json.get("display_name").asText();
                }
            }
            
            return "";
        } catch (Exception e) {
            System.err.println("Reverse geocoding failed: " + e.getMessage());
            return "";
        }
    }
}
