package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.request.UpdateStationRequest;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.dto.response.ThresholdResponse;
import com.nhom.weatherdesktop.service.LocationService;
import com.nhom.weatherdesktop.service.StationService;
import com.nhom.weatherdesktop.service.ThresholdService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class EditStationDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(EditStationDialogController.class);
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField locationField;
    
    @FXML
    private TextField addressField;
    
    // Public toggle
    @FXML
    private ToggleSwitch publicToggle;
    
    // Temperature threshold
    @FXML
    private ToggleSwitch temperatureActiveToggle;
    @FXML
    private TextField temperatureMinField;
    @FXML
    private TextField temperatureMaxField;
    
    // Humidity threshold
    @FXML
    private ToggleSwitch humidityActiveToggle;
    @FXML
    private TextField humidityMinField;
    @FXML
    private TextField humidityMaxField;
    
    // Rainfall threshold
    @FXML
    private ToggleSwitch rainfallActiveToggle;
    @FXML
    private TextField rainfallMaxField;
    
    // Wind Speed threshold
    @FXML
    private ToggleSwitch windSpeedActiveToggle;
    @FXML
    private TextField windSpeedMaxField;
    
    // Dust threshold
    @FXML
    private ToggleSwitch dustActiveToggle;
    @FXML
    private TextField dustMaxField;
    
    private Stage stage;
    private StationService stationService;
    private ThresholdService thresholdService;
    private LocationService locationService;
    private StationResponse station;
    private ThresholdResponse threshold;
    private Double selectedLatitude;
    private Double selectedLongitude;
    private boolean success = false;
    private boolean isLoadingData = false;
    
    public EditStationDialogController() {
        this.stationService = new StationService();
        this.thresholdService = new ThresholdService();
        this.locationService = new LocationService();
    }
    
    @FXML
    public void initialize() {
        // Add enter key handler for name field
        nameField.setOnAction(e -> locationField.requestFocus());
        locationField.setOnAction(e -> handleSelectLocation());
        
        // Add listener for public toggle to call API
        publicToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            // Only call API if not loading data and value actually changed
            if (station != null && !isLoadingData && !oldValue.equals(newValue)) {
                // Call API to update sharing status
                handlePublicToggleChanged();
            }
        });
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setStation(StationResponse station) {
        this.station = station;
        this.isLoadingData = true;  // Set flag to prevent listener from calling API
        
        // Populate fields with existing data
        nameField.setText(station.name());
        locationField.setText(station.location());
        if (station.isPublic() != null) {
            publicToggle.setSelected(station.isPublic());
        }
        selectedLatitude = station.latitude();
        selectedLongitude = station.longitude();
        
        // Load threshold data
        new Thread(() -> {
            try {
                threshold = thresholdService.getThresholdByStationId(station.id());
                if (threshold != null) {
                    Platform.runLater(() -> {
                        logger.debug("Populating threshold data");
                        // Temperature
                        if (threshold.temperatureActive() != null) temperatureActiveToggle.setSelected(threshold.temperatureActive());
                        if (threshold.temperatureMin() != null) temperatureMinField.setText(String.valueOf(threshold.temperatureMin()));
                        if (threshold.temperatureMax() != null) temperatureMaxField.setText(String.valueOf(threshold.temperatureMax()));
                        
                        // Humidity
                        if (threshold.humidityActive() != null) humidityActiveToggle.setSelected(threshold.humidityActive());
                        if (threshold.humidityMin() != null) humidityMinField.setText(String.valueOf(threshold.humidityMin()));
                        if (threshold.humidityMax() != null) humidityMaxField.setText(String.valueOf(threshold.humidityMax()));
                        
                        // Rainfall
                        if (threshold.rainfallActive() != null) rainfallActiveToggle.setSelected(threshold.rainfallActive());
                        if (threshold.rainfallMax() != null) rainfallMaxField.setText(String.valueOf(threshold.rainfallMax()));
                        
                        // Wind Speed
                        if (threshold.windSpeedActive() != null) windSpeedActiveToggle.setSelected(threshold.windSpeedActive());
                        if (threshold.windSpeedMax() != null) windSpeedMaxField.setText(String.valueOf(threshold.windSpeedMax()));
                        
                        // Dust
                        if (threshold.dustActive() != null) dustActiveToggle.setSelected(threshold.dustActive());
                        if (threshold.dustMax() != null) dustMaxField.setText(String.valueOf(threshold.dustMax()));
                    });
                }
            } catch (Exception e) {
                logger.error("Failed to load threshold data: {}", e.getMessage());
            }
        }).start();
        
        // Reverse geocode to get address from coordinates
        if (selectedLatitude != null && selectedLongitude != null) {
            new Thread(() -> {
                try {
                    logger.debug("Reverse geocoding address for station: lat={}, lng={}", 
                        selectedLatitude, selectedLongitude);
                    String address = locationService.reverseGeocode(selectedLatitude, selectedLongitude);
                    Platform.runLater(() -> {
                        addressField.setText(address);
                        logger.debug("Address populated: {}", address);
                    });
                } catch (Exception e) {
                    logger.warn("Failed to reverse geocode address: {}", e.getMessage());
                    Platform.runLater(() -> {
                        addressField.setText(station.location()); // Fallback to location text
                    });
                }
            }).start();
        } else {
            addressField.setText(station.location()); // Fallback if no coordinates
        }
        
        // Reset loading flag after all data is loaded
        this.isLoadingData = false;
        logger.debug("Loaded station data: id={}, name={}", station.id(), station.name());
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    @FXML
    private void handleSelectLocation() {
        try {
            logger.debug("Opening map picker dialog");
            
            // Load Map Picker Dialog
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/dialogs/map_picker_dialog.fxml")
            );
            javafx.scene.layout.VBox mapRoot = loader.load();
            
            // Get controller
            MapPickerController mapController = loader.getController();
            
            // Set initial location if available
            if (selectedLatitude != null && selectedLongitude != null) {
                mapController.setInitialLocation(selectedLatitude, selectedLongitude);
            }
            
            // Create and configure stage
            javafx.stage.Stage mapStage = new javafx.stage.Stage();
            mapStage.setTitle("Select Location");
            mapStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            mapStage.setScene(new javafx.scene.Scene(mapRoot));
            
            // Set stage reference
            mapController.setStage(mapStage);
            
            // Show dialog and wait
            mapStage.showAndWait();
            
            // Check if location was confirmed
            if (mapController.isConfirmed()) {
                selectedLatitude = mapController.getSelectedLatitude();
                selectedLongitude = mapController.getSelectedLongitude();
                
                logger.info("Location selected: lat={}, lng={}", selectedLatitude, selectedLongitude);
                
                // Get address from coordinates
                String address = locationService.reverseGeocode(selectedLatitude, selectedLongitude);
                addressField.setText(address);
            }
            
        } catch (Exception e) {
            logger.error("Failed to open map picker: {}", e.getMessage(), e);
            showError("Error", "Failed to open map picker: " + e.getMessage());
        }
    }
    
    private void handlePublicToggleChanged() {
        if (station == null) return;
        
        boolean newPublicStatus = publicToggle.isSelected();
        logger.debug("Public toggle changed for station {}: {}", station.id(), newPublicStatus);
        
        // Call API in background thread
        new Thread(() -> {
            try {
                StationResponse response = stationService.updateStationSharing(station.id());
                Platform.runLater(() -> {
                    logger.info("Station sharing updated successfully: id={}, isPublic={}", 
                        response.id(), response.isPublic());
                    // Update local station reference
                    station = response;
                });
            } catch (Exception e) {
                logger.error("Failed to update station sharing: {}", e.getMessage(), e);
                Platform.runLater(() -> {
                    // Revert toggle to previous state on error
                    publicToggle.setSelected(!newPublicStatus);
                    showError("Error", "Failed to update sharing status: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleSave() {
        // Validate inputs
        String name = nameField.getText().trim();
        String location = locationField.getText().trim();
        Boolean isPublic = publicToggle.isSelected();
        
        if (name.isEmpty()) {
            showError("Validation Error", "Please enter a station name.");
            nameField.requestFocus();
            return;
        }
        
        if (location.isEmpty()) {
            showError("Validation Error", "Please enter a location.");
            locationField.requestFocus();
            return;
        }
        
        if (selectedLatitude == null || selectedLongitude == null) {
            showError("Validation Error", "Please select coordinates on the map.");
            return;
        }
        
        // Parse threshold values (handle empty/invalid safely)
        final Float tempMin = parseTextField(temperatureMinField);
        final Float tempMax = parseTextField(temperatureMaxField);
        final Float humMin = parseTextField(humidityMinField);
        final Float humMax = parseTextField(humidityMaxField);
        final Float rainMax = parseTextField(rainfallMaxField);
        final Float windMax = parseTextField(windSpeedMaxField);
        final Float dustMax = parseTextField(dustMaxField);
        
        // Call API in background
        new Thread(() -> {
            try {
                logger.info("Updating station: id={}, name={}, location={}, coords=({}, {}), public={}", 
                    station.id(), name, location, selectedLatitude, selectedLongitude, isPublic);
                
                // 1. Update Station Info
                UpdateStationRequest stationRequest = new UpdateStationRequest(
                    name,
                    location,
                    BigDecimal.valueOf(selectedLatitude),
                    BigDecimal.valueOf(selectedLongitude),
                    isPublic
                );
                
                StationResponse stationResponse = stationService.updateStation(station.id(), stationRequest);
                
                // 2. Update Thresholds (if we have a threshold ID)
                if (threshold != null) {
                    com.nhom.weatherdesktop.dto.request.UpdateThresholdRequest thresholdRequest = 
                        new com.nhom.weatherdesktop.dto.request.UpdateThresholdRequest(
                            tempMin, tempMax, humMin, humMax, rainMax, windMax, dustMax,
                            temperatureActiveToggle.isSelected(),
                            humidityActiveToggle.isSelected(),
                            rainfallActiveToggle.isSelected(),
                            windSpeedActiveToggle.isSelected(),
                            dustActiveToggle.isSelected()
                        );
                        
                    thresholdService.updateThreshold(threshold.id(), thresholdRequest);
                    logger.info("Thresholds updated for station {}", station.id());
                }
                
                Platform.runLater(() -> {
                    logger.info("Station updated successfully! Station: {}, Location: {}, Coordinates: ({}, {})", 
                        stationResponse.name(), stationResponse.location(), stationResponse.latitude(), stationResponse.longitude());
                    success = true;
                    showSuccess("Success", "Station updated successfully!");
                    closeDialog();
                });
                
            } catch (Exception e) {
                logger.error("Failed to update station: {}", e.getMessage(), e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to update station: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private Float parseTextField(TextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) return null;
        try {
            return Float.parseFloat(text);
        } catch (NumberFormatException e) {
            return null; // Or handle error if strict validation needed
        }
    }
    
    @FXML
    private void handleCancel() {
        logger.debug("Edit station cancelled");
        closeDialog();
    }
    
    private void closeDialog() {
        if (stage != null) {
            stage.close();
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
