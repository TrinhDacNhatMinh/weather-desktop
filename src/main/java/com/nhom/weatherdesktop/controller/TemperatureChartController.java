package com.nhom.weatherdesktop.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        "Temperature Trend (7 Days)",
        "Humidity Trend (7 Days)",
        "Wind Speed Trend (7 Days)",
        "Dust Level Trend (7 Days)",
        "Rainfall Trend (7 Days)"
    };
    
    @FXML
    public void initialize() {
        // Populate all charts with dummy data
        populateTemperatureData();
        populateHumidityData();
        populateWindSpeedData();
        populateDustData();
        populateRainfallData();
        
        // Show first chart
        updateChartVisibility();
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
        
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        double[] temps = {22, 24, 23, 25, 27, 26, 24};
        
        for (int i = 0; i < days.length; i++) {
            series.getData().add(new XYChart.Data<>(days[i], temps[i]));
        }
        
        temperatureChart.getData().add(series);
    }
    
    private void populateHumidityData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Humidity");
        
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        double[] humidity = {75, 72, 78, 80, 68, 70, 73};
        
        for (int i = 0; i < days.length; i++) {
            series.getData().add(new XYChart.Data<>(days[i], humidity[i]));
        }
        
        humidityChart.getData().add(series);
    }
    
    private void populateWindSpeedData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Wind Speed");
        
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        double[] windSpeed = {3.2, 4.5, 3.8, 4.0, 5.2, 4.8, 3.5};
        
        for (int i = 0; i < days.length; i++) {
            series.getData().add(new XYChart.Data<>(days[i], windSpeed[i]));
        }
        
        windSpeedChart.getData().add(series);
    }
    
    private void populateDustData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Dust");
        
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        double[] dust = {65, 72, 85, 95, 110, 88, 70};
        
        for (int i = 0; i < days.length; i++) {
            series.getData().add(new XYChart.Data<>(days[i], dust[i]));
        }
        
        dustChart.getData().add(series);
    }
    
    private void populateRainfallData() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Rainfall");
        
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        double[] rainfall = {0.5, 1.2, 0, 2.5, 3.8, 1.5, 0.2};
        
        for (int i = 0; i < days.length; i++) {
            series.getData().add(new XYChart.Data<>(days[i], rainfall[i]));
        }
        
        rainfallChart.getData().add(series);
    }
}
