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
    
    private final Label titleLabel;
    private final Button prevButton;
    private final Button nextButton;
    
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
            "-fx-background-color: #3B82F6; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
        prevButton.setOnAction(e -> navigatePrevious());
        
        titleLabel = new Label(currentChartType.title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        nextButton = new Button("→");
        nextButton.setStyle(
            "-fx-font-size: 18px; " +
            "-fx-padding: 5 15; " +
            "-fx-background-color: #3B82F6; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
        nextButton.setOnAction(e -> navigateNext());
        
        header.getChildren().addAll(prevButton, titleLabel, nextButton);
        
        // Create axes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(currentChartType.unit);
        yAxis.setAutoRanging(true);
        
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
}
