package com.nhom.weatherdesktop.ui.controller.dialog;

import com.nhom.weatherdesktop.dto.request.AddStationRequest;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.service.StationService;
import com.nhom.weatherdesktop.ui.component.MapLocationPicker;
import com.nhom.weatherdesktop.util.AlertService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.util.Optional;

public class AddStationDialogController {
    
    private final StationService stationService;
    private Runnable onSuccess;
    private Runnable onError;
    
    public AddStationDialogController(StationService stationService) {
        this.stationService = stationService;
    }
    
    public void showDialog(Window owner) {
        // Create dialog
        Dialog<AddStationRequest> dialog = new Dialog<>();
        dialog.setTitle("Add New Station");
        dialog.setHeaderText("Enter station details");
        
        // Buttons
        ButtonType addButtonType = new ButtonType("Add Station", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Weather Station 1");
        
        TextField apiKeyField = new TextField();
        apiKeyField.setPromptText("your-api-key-here");
        
        TextField latitudeField = new TextField();
        latitudeField.setPromptText("10.762622");
        latitudeField.setEditable(false);
        latitudeField.setStyle("-fx-background-color: #F3F4F6;");
        
        TextField longitudeField = new TextField();
        longitudeField.setPromptText("106.660720");
        longitudeField.setEditable(false);
        longitudeField.setStyle("-fx-background-color: #F3F4F6;");
        
        // Hidden values to store actual lat/lng
        final double[] selectedLat = {0};
        final double[] selectedLng = {0};
        
        Button pickLocationBtn = new Button("Pick on Map");
        pickLocationBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-cursor: hand;");
        pickLocationBtn.setOnAction(e -> {
            MapLocationPicker mapPicker = new MapLocationPicker();
            mapPicker.showMapPicker((javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow());
            
            // Store actual values
            selectedLat[0] = mapPicker.getLatitude();
            selectedLng[0] = mapPicker.getLongitude();
            
            // Display address instead of numbers
            String address = mapPicker.getAddress();
            if (!address.isEmpty()) {
                latitudeField.setText(address);
                longitudeField.setText(""); // Clear longitude field
            } else {
                // Fallback to numbers if no address
                latitudeField.setText(String.valueOf(selectedLat[0]));
                longitudeField.setText(String.valueOf(selectedLng[0]));
            }
        });

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("API Key:"), 0, 1);
        grid.add(apiKeyField, 1, 1);
        grid.add(new Label("Address:"), 0, 2);
        grid.add(latitudeField, 1, 2);
        grid.add(pickLocationBtn, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Convert result to AddStationRequest when user clicks Add
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String name = nameField.getText().trim();
                    String apiKey = apiKeyField.getText().trim();
                    
                    if (name.isEmpty() || apiKey.isEmpty()) {
                        throw new IllegalArgumentException("Name and API Key cannot be empty");
                    }
                    
                    // Use selected lat/lng from map picker
                    BigDecimal latitude = BigDecimal.valueOf(selectedLat[0]);
                    BigDecimal longitude = BigDecimal.valueOf(selectedLng[0]);
                    
                    return new AddStationRequest(name, apiKey, latitude, longitude);
                } catch (Exception e) {
                    showError("Invalid input: " + e.getMessage());
                    return null;
                }
            }
            return null;
        });

        // Show dialog and process result
        Optional<AddStationRequest> result = dialog.showAndWait();
        
        result.ifPresent(request -> {
            // Call API in background thread
            new Thread(() -> {
                try {
                    StationResponse response = stationService.addStationToUser(request);
                    
                    Platform.runLater(() -> {
                        showSuccess("Station '" + response.name() + "' added successfully!");
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showError("Failed to add station: " + e.getMessage());
                        if (onError != null) {
                            onError.run();
                        }
                    });
                }
            }).start();
        });
    }
    
    public AddStationDialogController setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
        return this;
    }
    
    public AddStationDialogController setOnError(Runnable callback) {
        this.onError = callback;
        return this;
    }
    
    private void showError(String message) {
        AlertService.showError(message);
    }
    
    private void showSuccess(String message) {
        AlertService.showSuccess(message);
    }
}
