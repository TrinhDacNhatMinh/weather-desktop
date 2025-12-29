package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nhom.weatherdesktop.dto.response.DailyWeatherSummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Service for fetching weather data from API
 */
public class WeatherDataService {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherDataService.class);
    private static WeatherDataService instance;
    private final HttpClientService httpClient;
    
    private WeatherDataService() {
        this.httpClient = HttpClientService.getInstance();
    }
    
    public static WeatherDataService getInstance() {
        if (instance == null) {
            instance = new WeatherDataService();
        }
        return instance;
    }
    
    /**
     * Get daily weather summary for a station
     * @param stationId ID of the station
     * @param days Number of days to fetch (1-30)
     * @return List of daily weather summaries
     */
    public List<DailyWeatherSummaryResponse> getDailySummary(Long stationId, int days) {
        String endpoint = "/weather-data/stations/" + stationId + "/daily-summary?days=" + days;
        
        try {
            logger.info("Fetching weather data: stationId={}, days={}", stationId, days);
            
            long startTime = System.currentTimeMillis();
            
            // API returns List directly, not wrapped in ApiResponse
            List<DailyWeatherSummaryResponse> data = httpClient.get(
                endpoint,
                new TypeReference<List<DailyWeatherSummaryResponse>>() {},
                true  // requiresAuth = true
            );
            
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("Weather data loaded: {} days in {}ms", data.size(), duration);
            
            return data;
            
        } catch (HttpClientService.HttpException e) {
            logger.error("API error [{}]: {} - {}", e.getStatusCode(), endpoint, e.getMessage());
            throw new RuntimeException("Failed to fetch daily summary: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error("Network error: {} - {}", endpoint, e.getMessage());
            throw new RuntimeException("Network error: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error: {} - {}", endpoint, e.getMessage());
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }
}
