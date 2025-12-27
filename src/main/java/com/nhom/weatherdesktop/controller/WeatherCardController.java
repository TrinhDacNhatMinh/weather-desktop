package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.service.StationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class WeatherCardController {
    
    private static final Logger logger = LoggerFactory.getLogger(WeatherCardController.class);
    
    @FXML
    private Text temperatureValue;
    
    @FXML
    private Text humidityValue;
    
    @FXML
    private Text windSpeedValue;
    
    @FXML
    private Text rainfallValue;
    
    @FXML
    private Text dustValue;

    @FXML
    private Button menuButton;
    
    private ContextMenu stationMenu;
    private final StationService stationService;
    
    public WeatherCardController() {
        this.stationService = new StationService();
    }
    
    @FXML
    public void initialize() {
        // Data already set in FXML for demo
    }
    
    @FXML
    private void handleMenuClick() {
        logger.debug("Menu button clicked");
        
        // Create or show context menu
        if (stationMenu == null) {
            stationMenu = new ContextMenu();
            stationMenu.getStyleClass().add("station-dropdown");
            logger.debug("Created new ContextMenu");
        }
        
        // Clear existing items
        stationMenu.getItems().clear();
        logger.debug("Cleared menu items");
        
        // Load stations in background
        new Thread(() -> {
            try {
                logger.debug("Starting station fetch...");
                PageResponse<StationResponse> response = stationService.getMyStations(0, 10);
                logger.info("Fetched {} stations", response.content().size());
                
                Platform.runLater(() -> {
                    if (response.content().isEmpty()) {
                        logger.debug("No stations found");
                        // Show empty message
                        CustomMenuItem emptyItem = new CustomMenuItem(new Text("No stations found"));
                        emptyItem.setDisable(true);
                        stationMenu.getItems().add(emptyItem);
                    } else {
                        logger.debug("Adding {} station items", response.content().size());
                        // Add station items
                        for (StationResponse station : response.content()) {
                            try {
                                logger.debug("Loading item for station: {}", station.name());
                                FXMLLoader loader = new FXMLLoader(
                                    getClass().getResource("/fxml/components/station_item.fxml")
                                );
                                HBox stationItem = loader.load();
                                
                                StationItemController controller = loader.getController();
                                controller.setStationData(station);
                                
                                CustomMenuItem menuItem = new CustomMenuItem(stationItem);
                                menuItem.setHideOnClick(false);  // Keep menu open when clicking items
                                stationMenu.getItems().add(menuItem);
                                logger.debug("Added station: {}", station.name());
                                
                            } catch (Exception e) {
                                logger.error("Failed to load station item for {}: {}", station.name(), e.getMessage(), e);
                            }
                        }
                    }
                    
                    // Show menu below button
                    logger.debug("Showing context menu");
                    stationMenu.show(menuButton, Side.BOTTOM, 0, 5);
                });
                
            } catch (Exception e) {
                logger.error("Failed to fetch stations: {}", e.getMessage(), e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to load stations: " + e.getMessage());
                });
            }
        }).start();
    }
    
    public void updateWeatherData(double temp, double humidity, double windSpeed, double rainfall, double dust) {
        temperatureValue.setText(String.format("%.1f", temp));
        humidityValue.setText(String.format("%.0f", humidity));
        windSpeedValue.setText(String.format("%.1f", windSpeed));
        rainfallValue.setText(String.format("%.1f", rainfall));
        dustValue.setText(String.format("%.0f", dust));
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
