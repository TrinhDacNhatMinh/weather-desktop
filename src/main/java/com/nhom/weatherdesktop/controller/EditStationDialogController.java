package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.request.UpdateStationRequest;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.service.LocationService;
import com.nhom.weatherdesktop.service.StationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
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
    
    private Stage stage;
    private StationService stationService;
    private LocationService locationService;
    private StationResponse station;
    private Double selectedLatitude;
    private Double selectedLongitude;
    private boolean success = false;
    
    public EditStationDialogController() {
        this.stationService = new StationService();
        this.locationService = new LocationService();
    }
    
    @FXML
    public void initialize() {
        // Add enter key handler for name field
        nameField.setOnAction(e -> locationField.requestFocus());
        locationField.setOnAction(e -> handleSelectLocation());
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setStation(StationResponse station) {
        this.station = station;
        
        // Populate fields with existing data
        nameField.setText(station.name());
        locationField.setText(station.location());
        selectedLatitude = station.latitude();
        selectedLongitude = station.longitude();
        
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
    
    @FXML
    private void handleSave() {
        // Validate inputs
        String name = nameField.getText().trim();
        String location = locationField.getText().trim();
        
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
        
        // Call API in background
        new Thread(() -> {
            try {
                logger.info("Updating station: id={}, name={}, location={}, coords=({}, {})", 
                    station.id(), name, location, selectedLatitude, selectedLongitude);
                
                UpdateStationRequest request = new UpdateStationRequest(
                    name,
                    location,
                    BigDecimal.valueOf(selectedLatitude),
                    BigDecimal.valueOf(selectedLongitude)
                );
                
                StationResponse response = stationService.updateStation(station.id(), request);
                
                Platform.runLater(() -> {
                    logger.info("Station updated successfully! Station: {}, Location: {}, Coordinates: ({}, {})", 
                        response.name(), response.location(), response.latitude(), response.longitude());
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
