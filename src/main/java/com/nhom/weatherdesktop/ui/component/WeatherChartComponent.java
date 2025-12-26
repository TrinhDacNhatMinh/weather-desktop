package com.nhom.weatherdesktop.ui.component;

import com.nhom.weatherdesktop.dto.response.DailyWeatherSummaryResponse;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Weather chart component with multiple chart types and navigation
 */
public class WeatherChartComponent extends VBox {
    
    private final LineChart<String, Number> chart;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd");
    
    private List<DailyWeatherSummaryResponse> currentData;
    private ChartType currentChartType = ChartType.TEMPERATURE;
    private double zoomLevel = 1.0; // 1.0 = normal, <1.0 = zoomed in, >1.0 = zoomed out
    
    private final Label titleLabel;
    private final Button prevButton;
    private final Button nextButton;
    private final Button zoomInButton;
    private final Button zoomOutButton;
    
    public enum ChartType {
        TEMPERATURE("Temperature (°C)", "°C"),
        HUMIDITY("Humidity (%)", "%"),
        RAINFALL("Rainfall (mm)", "mm"),
        WIND_SPEED("Wind Speed (m/s)", "m/s"),
        DUST("Air Quality - Dust (μg/m³)", "μg/m³");
        
        final String title;
        final String unit;
        
        ChartType(String title, String unit) {
            this.title = title;
            this.unit = unit;
        }
    }
    
    public WeatherChartComponent() {
        setSpacing(10);
        setPadding(new Insets(15));
        setAlignment(Pos.CENTER);
        
        // Header with navigation
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER);
        
        prevButton = new Button("←");
        prevButton.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 5 15; " +
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #3B82F6; " +
            "-fx-border-color: #3B82F6; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        );
        prevButton.setOnAction(e -> navigatePrevious());
        // Hover effects
        prevButton.setOnMouseEntered(e -> prevButton.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 5 15; " +
            "-fx-background-color: rgba(59, 130, 246, 0.08); " +
            "-fx-text-fill: #2563EB; " +
            "-fx-border-color: #2563EB; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        ));
        prevButton.setOnMouseExited(e -> prevButton.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 5 15; " +
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #3B82F6; " +
            "-fx-border-color: #3B82F6; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        ));
        
        titleLabel = new Label(currentChartType.title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        nextButton = new Button("→");
        nextButton.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 5 15; " +
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #3B82F6; " +
            "-fx-border-color: #3B82F6; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        );
        nextButton.setOnAction(e -> navigateNext());
        // Hover effects
        nextButton.setOnMouseEntered(e -> nextButton.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 5 15; " +
            "-fx-background-color: rgba(59, 130, 246, 0.08); " +
            "-fx-text-fill: #2563EB; " +
            "-fx-border-color: #2563EB; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        ));
        nextButton.setOnMouseExited(e -> nextButton.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 5 15; " +
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #3B82F6; " +
            "-fx-border-color: #3B82F6; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        ));
        
        // Zoom buttons
        zoomInButton = new Button("+");
        zoomInButton.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 5 15; " +
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #10B981; " +
            "-fx-border-color: #10B981; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        );
        zoomInButton.setOnAction(e -> zoomIn());
        // Hover effects
        zoomInButton.setOnMouseEntered(e -> zoomInButton.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 5 15; " +
            "-fx-background-color: rgba(16, 185, 129, 0.08); " +
            "-fx-text-fill: #059669; " +
            "-fx-border-color: #059669; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        ));
        zoomInButton.setOnMouseExited(e -> zoomInButton.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 5 15; " +
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #10B981; " +
            "-fx-border-color: #10B981; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        ));
        
        zoomOutButton = new Button("-");
        zoomOutButton.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 5 15; " +
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #10B981; " +
            "-fx-border-color: #10B981; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        );
        zoomOutButton.setOnAction(e -> zoomOut());
        // Hover effects
        zoomOutButton.setOnMouseEntered(e -> zoomOutButton.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 5 15; " +
            "-fx-background-color: rgba(16, 185, 129, 0.08); " +
            "-fx-text-fill: #059669; " +
            "-fx-border-color: #059669; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        ));
        zoomOutButton.setOnMouseExited(e -> zoomOutButton.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 5 15; " +
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #10B981; " +
            "-fx-border-color: #10B981; " +
            "-fx-border-width: 2; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-cursor: hand;"
        ));
        
        header.getChildren().addAll(prevButton, titleLabel, nextButton, zoomInButton, zoomOutButton);
        
        // Create axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(currentChartType.unit);
        yAxis.setAutoRanging(false); // Disable auto-ranging for manual zoom control
        
        // Create chart
        chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(true);
        chart.setCreateSymbols(true);
        chart.getStyleClass().add("weather-chart");
        chart.setPrefHeight(300);
        chart.setAnimated(false); // Disable animation for faster switching
        
        getChildren().addAll(header, chart);
    }
    
    private void navigatePrevious() {
        ChartType[] types = ChartType.values();
        int currentIndex = currentChartType.ordinal();
        currentChartType = types[(currentIndex - 1 + types.length) % types.length];
        refreshChart();
    }
    
    private void navigateNext() {
        ChartType[] types = ChartType.values();
        int currentIndex = currentChartType.ordinal();
        currentChartType = types[(currentIndex + 1) % types.length];
        refreshChart();
    }
    
    private void refreshChart() {
        titleLabel.setText(currentChartType.title);
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        yAxis.setLabel(currentChartType.unit);
        
        if (currentData != null) {
            updateChartData();
        }
    }
    
    /**
     * Clear all chart data
     */
    public void clear() {
        this.currentData = null;
        chart.getData().clear();
    }
    
    /**
     * Update chart with new data
     */
    public void updateChart(List<DailyWeatherSummaryResponse> data) {
        this.currentData = data;
        updateChartData();
    }
    
    private void updateChartData() {
        chart.getData().clear();
        
        if (currentData == null || currentData.isEmpty()) {
            return;
        }
        
        XYChart.Series<String, Number> minSeries = new XYChart.Series<>();
        minSeries.setName("Min");
        
        XYChart.Series<String, Number> avgSeries = new XYChart.Series<>();
        avgSeries.setName("Avg");
        
        XYChart.Series<String, Number> maxSeries = new XYChart.Series<>();
        maxSeries.setName("Max");
        
        // Populate data based on chart type
        for (DailyWeatherSummaryResponse day : currentData) {
            String dateLabel = formatDate(day.date());
            
            switch (currentChartType) {
                case TEMPERATURE:
                    addDataPoint(minSeries, dateLabel, day.minTemperature());
                    addDataPoint(avgSeries, dateLabel, day.avgTemperature());
                    addDataPoint(maxSeries, dateLabel, day.maxTemperature());
                    break;
                case HUMIDITY:
                    addDataPoint(minSeries, dateLabel, day.minHumidity());
                    addDataPoint(avgSeries, dateLabel, day.avgHumidity());
                    addDataPoint(maxSeries, dateLabel, day.maxHumidity());
                    break;
                case RAINFALL:
                    // Rainfall only has total, show as single line
                    addDataPoint(avgSeries, dateLabel, day.totalRainfall());
                    break;
                case WIND_SPEED:
                    addDataPoint(minSeries, dateLabel, day.minWindSpeed());
                    addDataPoint(avgSeries, dateLabel, day.avgWindSpeed());
                    addDataPoint(maxSeries, dateLabel, day.maxWindSpeed());
                    break;
                case DUST:
                    addDataPoint(minSeries, dateLabel, day.minDust());
                    addDataPoint(avgSeries, dateLabel, day.avgDust());
                    addDataPoint(maxSeries, dateLabel, day.maxDust());
                    break;
            }
        }
        
        // Add series to chart
        if (currentChartType == ChartType.RAINFALL) {
            avgSeries.setName("Total");
            chart.getData().add(avgSeries);
        } else {
            chart.getData().addAll(minSeries, avgSeries, maxSeries);
        }
        
        // Apply colors
        if (!chart.getData().isEmpty()) {
            chart.getData().get(0).getNode().getStyleClass().add("min-series");
            if (chart.getData().size() > 1) {
                chart.getData().get(1).getNode().getStyleClass().add("avg-series");
            }
            if (chart.getData().size() > 2) {
                chart.getData().get(2).getNode().getStyleClass().add("max-series");
            }
        }
        
        // Update Y-axis bounds based on zoom level
        updateYAxisBounds();
    }
    
    private void addDataPoint(XYChart.Series<String, Number> series, String label, BigDecimal value) {
        if (value != null) {
            series.getData().add(new XYChart.Data<>(label, value));
        }
    }
    
    private String formatDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.format(DATE_FORMATTER);
        } catch (Exception e) {
            return dateStr;
        }
    }
    
    /**
     * Zoom in - decrease range to see more detail
     */
    private void zoomIn() {
        if (zoomLevel > 0.2) { // Minimum zoom level
            zoomLevel *= 0.8; // Reduce range by 20%
            updateYAxisBounds();
        }
    }
    
    /**
     * Zoom out - increase range to see more overview
     */
    private void zoomOut() {
        if (zoomLevel < 3.0) { // Maximum zoom level
            zoomLevel *= 1.25; // Increase range by 25%
            updateYAxisBounds();
        }
    }
    
    /**
     * Update Y-axis bounds based on current zoom level
     */
    private void updateYAxisBounds() {
        if (currentData == null || currentData.isEmpty()) {
            return;
        }
        
        // Calculate min and max values from current data
        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;
        
        for (DailyWeatherSummaryResponse day : currentData) {
            BigDecimal min = null, max = null;
            
            switch (currentChartType) {
                case TEMPERATURE:
                    min = day.minTemperature();
                    max = day.maxTemperature();
                    break;
                case HUMIDITY:
                    min = day.minHumidity();
                    max = day.maxHumidity();
                    break;
                case RAINFALL:
                    min = day.totalRainfall();
                    max = day.totalRainfall();
                    break;
                case WIND_SPEED:
                    min = day.minWindSpeed();
                    max = day.maxWindSpeed();
                    break;
                case DUST:
                    min = day.minDust();
                    max = day.maxDust();
                    break;
            }
            
            if (min != null && min.doubleValue() < minValue) {
                minValue = min.doubleValue();
            }
            if (max != null && max.doubleValue() > maxValue) {
                maxValue = max.doubleValue();
            }
        }
        
        // Calculate center and range
        double center = (minValue + maxValue) / 2;
        double range = (maxValue - minValue) * zoomLevel;
        
        // Add padding (10% of range)
        double padding = range * 0.1;
        range += padding * 2;
        
        // Set bounds
        NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        yAxis.setLowerBound(center - range / 2);
        yAxis.setUpperBound(center + range / 2);
        yAxis.setTickUnit(range / 10); // Approximately 10 ticks
    }
}
