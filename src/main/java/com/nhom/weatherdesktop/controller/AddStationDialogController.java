package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.request.AddStationRequest;
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

public class AddStationDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(AddStationDialogController.class);
    
    @FXML
    private TextField nameField;
    
    @FXML
    private TextField apiKeyField;
    
    @FXML
    private TextField locationField;
    
    private Stage stage;
    private StationService stationService;
    private LocationService locationService;
    private Double selectedLatitude;
    private Double selectedLongitude;
    private boolean success = false;
    
    public AddStationDialogController() {
        this.stationService = new StationService();
        this.locationService = new LocationService();
    }
    
    @FXML
    public void initialize() {
        // Add enter key handler for name field
        nameField.setOnAction(e -> apiKeyField.requestFocus());
        apiKeyField.setOnAction(e -> handleSelectLocation());
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
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
                locationField.setText(address);
            }
            
        } catch (Exception e) {
            logger.error("Failed to open map picker: {}", e.getMessage(), e);
            showError("Error", "Failed to open map picker: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAdd() {
        // Validate inputs
        String name = nameField.getText().trim();
        String apiKey = apiKeyField.getText().trim();
        
        if (name.isEmpty()) {
            showError("Validation Error", "Please enter a station name.");
            nameField.requestFocus();
            return;
        }
        
        if (apiKey.isEmpty()) {
            showError("Validation Error", "Please enter an API key.");
            apiKeyField.requestFocus();
            return;
        }
        
        if (selectedLatitude == null || selectedLongitude == null) {
            showError("Validation Error", "Please select a location.");
            return;
        }
        
        // Call API in background
        new Thread(() -> {
            try {
                logger.info("Adding station: name={}, location=({}, {})", 
                    name, selectedLatitude, selectedLongitude);
                
                AddStationRequest request = new AddStationRequest(
                    name,
                    apiKey,
                    BigDecimal.valueOf(selectedLatitude),
                    BigDecimal.valueOf(selectedLongitude)
                );
                
                StationResponse response = stationService.addStationToUser(request);
                
                Platform.runLater(() -> {
                    logger.info("Station added successfully: {}", response.name());
                    success = true;
                    showSuccess("Success", "Station added successfully!");
                    closeDialog();
                });
                
            } catch (Exception e) {
                logger.error("Failed to add station: {}", e.getMessage(), e);
                Platform.runLater(() -> {
                    showError("Error", "Failed to add station: " + e.getMessage());
                });
            }
        }).start();
    }
    
    @FXML
    private void handleCancel() {
        logger.debug("Add station cancelled");
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
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
