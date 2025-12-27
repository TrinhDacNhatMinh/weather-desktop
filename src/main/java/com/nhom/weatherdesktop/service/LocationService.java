package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Service for interacting with OpenStreetMap Nominatim API
 * Used for reverse geocoding (lat/lng -> address)
 */
public class LocationService {
    
    private static final Logger logger = LoggerFactory.getLogger(LocationService.class);
    private static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Reverse geocode coordinates to get a human-readable address
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @return Address string, or coordinates as fallback
     */
    public String reverseGeocode(double latitude, double longitude) {
        try {
            String endpoint = String.format("%s/reverse?lat=%f&lon=%f&format=json",
                NOMINATIM_BASE_URL, latitude, longitude);
            
            logger.debug("Reverse geocoding: lat={}, lon={}", latitude, longitude);
            
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            try {
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("User-Agent", "WeatherDesktopApp/1.0");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                int responseCode = connection.getResponseCode();
                
                if (responseCode == 200) {
                    JsonNode response = objectMapper.readTree(connection.getInputStream());
                    String displayName = response.path("display_name").asText();
                    
                    if (displayName != null && !displayName.isEmpty()) {
                        logger.info("Reverse geocoded to: {}", displayName);
                        return displayName;
                    }
                }
                
                logger.warn("Reverse geocoding failed, using coordinates");
                return String.format("%.4f, %.4f", latitude, longitude);
                
            } finally {
                connection.disconnect();
            }
            
        } catch (Exception e) {
            logger.error("Error during reverse geocoding: {}", e.getMessage(), e);
            return String.format("%.4f, %.4f", latitude, longitude);
        }
    }
}
