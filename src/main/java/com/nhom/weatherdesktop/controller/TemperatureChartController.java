package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.response.DailyWeatherSummaryResponse;
import com.nhom.weatherdesktop.service.WeatherDataService;
import com.nhom.weatherdesktop.session.SessionContext;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class TemperatureChartController {
    
    private static final Logger logger = LoggerFactory.getLogger(TemperatureChartController.class);
    
    @FXML
    private Text chartTitle;
    
    @FXML
    private StackPane chartsStack;
    
    @FXML
    private LineChart<String, Number> temperatureChart;
    
    @FXML
    private LineChart<String, Number> humidityChart;
    
    @FXML
    private LineChart<String, Number> windSpeedChart;
    
    @FXML
    private LineChart<String, Number> dustChart;
    
    @FXML
    private BarChart<String, Number> rainfallChart;
    
    @FXML
    private Button backButton;
    
    @FXML
    private Button forwardButton;
    
    private int currentChartIndex = 0;
    private final String[] chartTitles = {
        "Temperature - Last 7 Days",
        "Humidity - Last 7 Days",
        "Wind Speed - Last 7 Days",
        "Dust Level - Last 7 Days",
        "Rainfall - Last 7 Days"
    };
    
    private final WeatherDataService weatherDataService;
    private List<DailyWeatherSummaryResponse> weatherData;
    private Long lastLoadedStationId = null; // Track last loaded station
    
    public TemperatureChartController() {
        this.weatherDataService = WeatherDataService.getInstance();
    }
    
    @FXML
    public void initialize() {
        // Show first chart
        updateChartVisibility();
        
        // Load weather data from API (with retry if station not yet selected)
        loadWeatherDataWithRetry();
        
        // Start watching for station changes
        startStationChangeListener();
    }
    
    /**
     * Monitor SessionContext for station changes and reload chart data
     * Uses JavaFX Timeline to check every 500ms
     */
    private void startStationChangeListener() {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(500),
                event -> {
                    Long currentStationId = SessionContext.selectedStationId();
                    
                    // Check if station has changed
                    if (currentStationId != null && !currentStationId.equals(lastLoadedStationId)) {
                        logger.info("Station changed from {} to {}, reloading chart data", 
                            lastLoadedStationId, currentStationId);
                        loadWeatherData();
                    }
                }
            )
        );
        timeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        timeline.play();
    }
    
    /**
     * Retry loading weather data if station ID is not immediately available
     * This handles the case where chart controller initializes before station is selected
     */
    private void loadWeatherDataWithRetry() {
        Long stationId = SessionContext.selectedStationId();
        
        if (stationId != null) {
            // Station already selected, load immediately
            loadWeatherData();
        } else {
            // Station not yet selected, retry after delay
            logger.info("Station not yet selected, will retry in 500ms");
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    Platform.runLater(this::loadWeatherData);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
    
    /**
     * Public method to refresh chart data
     * Call this when station selection changes
     */
    public void refreshChartData() {
        logger.info("Refreshing chart data...");
        loadWeatherData();
    }
    
    private void loadWeatherData() {
        // Get selected station ID from session
        Long stationId = SessionContext.selectedStationId();
        
        if (stationId == null) {
            logger.warn("No station selected, showing empty charts");
            showErrorMessage("Please select a station to view weather data");
            return;
        }
        
        // Update last loaded station ID
        lastLoadedStationId = stationId;
        
        // Fetch data in background thread to avoid blocking UI
        new Thread(() -> {
            try {
                logger.info("Fetching weather data for station: {}", stationId);
                List<DailyWeatherSummaryResponse> data = weatherDataService.getDailySummary(stationId, 7);
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    this.weatherData = data;
                    populateAllCharts();
                    logger.info("Successfully loaded weather data for {} days", data.size());
                });
                
            } catch (Exception e) {
                logger.error("Failed to load weather data: {}", e.getMessage(), e);
                Platform.runLater(() -> showErrorMessage("Failed to load weather data: " + e.getMessage()));
            }
        }).start();
    }
    
    private void populateAllCharts() {
        if (weatherData == null || weatherData.isEmpty()) {
            logger.warn("No weather data available");
            return;
        }
        
        populateTemperatureData();
        populateHumidityData();
        populateWindSpeedData();
        populateDustData();
        populateRainfallData();
    }
    
    private void showErrorMessage(String message) {
        chartTitle.setText("Error: " + message);
    }
    
    @FXML
    private void handleBack() {
        currentChartIndex--;
        if (currentChartIndex < 0) {
            currentChartIndex = 4; // Wrap to last chart
        }
        updateChartVisibility();
        logger.info("Switched to chart: {}", chartTitles[currentChartIndex]);
    }
    
    @FXML
    private void handleForward() {
        currentChartIndex++;
        if (currentChartIndex > 4) {
            currentChartIndex = 0; // Wrap to first chart
        }
        updateChartVisibility();
        logger.info("Switched to chart: {}", chartTitles[currentChartIndex]);
    }
    
    private void updateChartVisibility() {
        // Update title
        chartTitle.setText(chartTitles[currentChartIndex]);
        
        // Hide all charts
        temperatureChart.setVisible(false);
        humidityChart.setVisible(false);
        windSpeedChart.setVisible(false);
        dustChart.setVisible(false);
        rainfallChart.setVisible(false);
        
        // Show current chart
        switch (currentChartIndex) {
            case 0 -> temperatureChart.setVisible(true);
            case 1 -> humidityChart.setVisible(true);
            case 2 -> windSpeedChart.setVisible(true);
            case 3 -> dustChart.setVisible(true);
            case 4 -> rainfallChart.setVisible(true);
        }
    }
    
    private void populateTemperatureData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Temperature");
        
        for (DailyWeatherSummaryResponse day : weatherData) {
            String dayLabel = formatDateToDay(day.date());
            Float temp = day.avgTemperature();
            if (temp != null) {
                series.getData().add(new XYChart.Data<>(dayLabel, temp));
            }
        }
        
        temperatureChart.getData().clear();
        temperatureChart.getData().add(series);
    }
    
    private void populateHumidityData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Humidity");
        
        for (DailyWeatherSummaryResponse day : weatherData) {
            String dayLabel = formatDateToDay(day.date());
            Float humidity = day.avgHumidity();
            if (humidity != null) {
                series.getData().add(new XYChart.Data<>(dayLabel, humidity));
            }
        }
        
        humidityChart.getData().clear();
        humidityChart.getData().add(series);
    }
    
    private void populateWindSpeedData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Wind Speed");
        
        for (DailyWeatherSummaryResponse day : weatherData) {
            String dayLabel = formatDateToDay(day.date());
            Float windSpeed = day.avgWindSpeed();
            if (windSpeed != null) {
                series.getData().add(new XYChart.Data<>(dayLabel, windSpeed));
            }
        }
        
        windSpeedChart.getData().clear();
        windSpeedChart.getData().add(series);
    }
    
    private void populateDustData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Dust");
        
        for (DailyWeatherSummaryResponse day : weatherData) {
            String dayLabel = formatDateToDay(day.date());
            Float dust = day.avgDust();
            if (dust != null) {
                series.getData().add(new XYChart.Data<>(dayLabel, dust));
            }
        }
        
        dustChart.getData().clear();
        dustChart.getData().add(series);
    }
    
    private void populateRainfallData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Rainfall");
        
        for (DailyWeatherSummaryResponse day : weatherData) {
            String dayLabel = formatDateToDay(day.date());
            Float rainfall = day.totalRainfall();
            if (rainfall != null) {
                series.getData().add(new XYChart.Data<>(dayLabel, rainfall));
            }
        }
        
        rainfallChart.getData().clear();
        rainfallChart.getData().add(series);
    }
    
    /**
     * Format date string (yyyy-MM-dd) to day of week (e.g., "Mon", "Tue")
     */
    private String formatDateToDay(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        } catch (Exception e) {
            logger.warn("Failed to parse date: {}", dateStr);
            return dateStr;
        }
    }
}
