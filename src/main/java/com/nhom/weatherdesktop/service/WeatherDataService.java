package com.nhom.weatherdesktop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom.weatherdesktop.config.AppConfig;
import com.nhom.weatherdesktop.dto.response.DailyWeatherSummaryResponse;
import com.nhom.weatherdesktop.util.HttpRequestBuilder;

import java.math.BigDecimal;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Service for weather data operations
 */
public class WeatherDataService implements IWeatherDataService {
    
    private static final String BASE_URL = AppConfig.getApiBaseUrl() + "/weather-data";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    @Override
    public List<DailyWeatherSummaryResponse> getDailySummary(Long stationId, int days) throws Exception {
        String url = BASE_URL + "/stations/" + stationId + "/daily-summary?days=" + days;
        
        var httpRequest = HttpRequestBuilder
                .createWithFullUrl(url)
                .withAuth()
                .get()
                .build();
        
        HttpResponse<String> response = HttpRequestBuilder.sendWithRefresh(httpRequest);
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to get daily summary: " + response.statusCode());
        }
        
        List<DailyWeatherSummaryResponse> allData = MAPPER.readValue(
            response.body(), 
            new TypeReference<List<DailyWeatherSummaryResponse>>() {}
        );
        
        // Filter out records where all values are null (no actual data)
        return allData.stream()
            .filter(this::hasValidData)
            .toList();
    }
    
    /**
     * Check if a daily summary record has valid, realistic data
     * Returns false if:
     * - All values are null (no data)
     * - All min/max/avg values are identical (likely fake/sample data)
     */
    private boolean hasValidData(DailyWeatherSummaryResponse record) {
        // Check if all values are null
        boolean hasAnyNonNull = record.minTemperature() != null || 
                                 record.maxTemperature() != null || 
                                 record.avgTemperature() != null ||
                                 record.minHumidity() != null || 
                                 record.maxHumidity() != null || 
                                 record.avgHumidity() != null ||
                                 record.minWindSpeed() != null || 
                                 record.maxWindSpeed() != null || 
                                 record.avgWindSpeed() != null ||
                                 record.minDust() != null || 
                                 record.maxDust() != null || 
                                 record.avgDust() != null ||
                                 record.totalRainfall() != null;
        
        if (!hasAnyNonNull) {
            return false; // No data at all
        }
        
        // Check for fake data: min = max = avg (unrealistic for weather data)
        // Real weather data should have natural variation
        boolean tempIsFake = isFakeMinMaxAvg(record.minTemperature(), record.maxTemperature(), record.avgTemperature());
        boolean humidityIsFake = isFakeMinMaxAvg(record.minHumidity(), record.maxHumidity(), record.avgHumidity());
        boolean windIsFake = isFakeMinMaxAvg(record.minWindSpeed(), record.maxWindSpeed(), record.avgWindSpeed());
        boolean dustIsFake = isFakeMinMaxAvg(record.minDust(), record.maxDust(), record.avgDust());
        
        // If all measured values are fake (identical), reject the record
        if (tempIsFake && humidityIsFake && windIsFake && dustIsFake) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if min/max/avg values are identical (sign of fake data)
     * Real weather data should have some variation
     */
    private boolean isFakeMinMaxAvg(BigDecimal min, BigDecimal max, BigDecimal avg) {
        // If all three are null, it's not fake, just missing
        if (min == null && max == null && avg == null) {
            return false;
        }
        
        // If all three are non-null and identical, it's likely fake
        if (min != null && max != null && avg != null) {
            return min.compareTo(max) == 0 && max.compareTo(avg) == 0;
        }
        
        // If only some are present, can't determine if fake
        return false;
    }
}
