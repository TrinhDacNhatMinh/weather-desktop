package com.nhom.weatherdesktop.ui.controller.dialog;

import com.nhom.weatherdesktop.dto.request.UpdateStationRequest;
import com.nhom.weatherdesktop.dto.request.UpdateThresholdRequest;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.dto.response.ThresholdResponse;
import com.nhom.weatherdesktop.service.StationService;
import com.nhom.weatherdesktop.service.ThresholdService;
import com.nhom.weatherdesktop.ui.component.MapLocationPicker;
import com.nhom.weatherdesktop.ui.component.ToggleSwitch;
import com.nhom.weatherdesktop.util.AlertService;
import com.nhom.weatherdesktop.util.GeocodingUtil;
import com.nhom.weatherdesktop.util.ValidationUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;

public class UpdateStationDialogController {
    
    private final StationService stationService;
    private final ThresholdService thresholdService;
    private Runnable onSuccess;
    
    public UpdateStationDialogController(StationService stationService, ThresholdService thresholdService) {
        this.stationService = stationService;
        this.thresholdService = thresholdService;
    }
    
    public void showDialog(Long stationId) {
        // Load fresh data from server in background
        new Thread(() -> {
            try {
                // Fetch latest station data
                StationResponse freshStation = stationService.getStationById(stationId);
                
                // Fetch threshold data
                ThresholdResponse threshold = thresholdService.getThresholdByStationId(stationId);
                
                // Open dialog on UI thread with fresh data
                Platform.runLater(() -> openDialog(freshStation, threshold));
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Failed to load station data: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void openDialog(StationResponse station, ThresholdResponse initialThreshold) {
        // Create dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Update Station");
        dialog.setHeaderText("Update station information and threshold settings");
        
        // Buttons
        ButtonType saveButtonType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create scrollable content
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(500);
        scrollPane.setPrefViewportWidth(500);
        
        VBox mainContent = new VBox(16);
        mainContent.setPadding(new Insets(20));
        
        // === STATION INFO SECTION ===
        Label sectionTitle1 = new Label("Station Information");
        sectionTitle1.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        GridPane stationInfoGrid = new GridPane();
        stationInfoGrid.setHgap(10);
        stationInfoGrid.setVgap(10);
        
        TextField nameField = new TextField(station.name());
        TextField locationField = new TextField(station.location());
        
        // Display current coordinates as address placeholder
        TextField addressField = new TextField("");
        addressField.setEditable(false);
        addressField.setStyle("-fx-background-color: #F3F4F6;");
        addressField.setPromptText("Click 'Pick on Map' to select location");
        
        // Hidden values to store actual lat/lng
        final double[] selectedLat = {station.latitude() != null ? station.latitude().doubleValue() : 0};
        final double[] selectedLng = {station.longitude() != null ? station.longitude().doubleValue() : 0};
        
        // Populate address field with current address from lat/lng in background
        if (station.latitude() != null && station.longitude() != null) {
            new Thread(() -> {
                try {
                    String address = GeocodingUtil.reverseGeocode(station.latitude().doubleValue(), station.longitude().doubleValue());
                    Platform.runLater(() -> {
                        if (!address.isEmpty()) {
                            addressField.setText(address);
                        } else {
                            addressField.setText(String.format("%.6f, %.6f", 
                                station.latitude().doubleValue(), 
                                station.longitude().doubleValue()));
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        addressField.setText(String.format("%.6f, %.6f", 
                            station.latitude().doubleValue(), 
                            station.longitude().doubleValue()));
                    });
                }
            }).start();
        }
        
        Button pickLocationBtn = new Button("🗺️ Pick on Map");
        pickLocationBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 16; -fx-cursor: hand;");
        pickLocationBtn.setOnAction(e -> {
            MapLocationPicker mapPicker = new MapLocationPicker();
            mapPicker.showMapPicker((javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow());
            
            // Store actual values
            selectedLat[0] = mapPicker.getLatitude();
            selectedLng[0] = mapPicker.getLongitude();
            
            // Display address in address field only
            String address = mapPicker.getAddress();
            if (!address.isEmpty()) {
                addressField.setText(address);
            } else {
                // Fallback to numbers
                addressField.setText(String.format("%.6f, %.6f", selectedLat[0], selectedLng[0]));
            }
        });
        
        stationInfoGrid.add(new Label("Name:"), 0, 0);
        stationInfoGrid.add(nameField, 1, 0);
        stationInfoGrid.add(new Label("Location:"), 0, 1);
        stationInfoGrid.add(locationField, 1, 1);
        stationInfoGrid.add(new Label("Address:"), 0, 2);
        stationInfoGrid.add(addressField, 1, 2);
        stationInfoGrid.add(pickLocationBtn, 1, 3);
        
        // Public station toggle
        HBox publicBox = new HBox(10);
        publicBox.setAlignment(Pos.CENTER_LEFT);
        Label publicLabel = new Label("Public Station:");
        ToggleSwitch publicStationToggle = new ToggleSwitch();
        publicStationToggle.setSwitchedOn(station.isPublic() != null && station.isPublic());
        publicBox.getChildren().addAll(publicLabel, publicStationToggle);
        
        Separator separator1 = new Separator();
        
        // === THRESHOLD SETTINGS SECTION ===
        Label sectionTitle2 = new Label("Threshold Settings");
        sectionTitle2.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Temperature
        VBox tempBox = createThresholdBox("Temperature (°C)", 
            new String[]{"Min", "Max", "Active"});
        TextField tempMinField = (TextField) ((HBox)tempBox.getChildren().get(1)).getChildren().get(1);
        TextField tempMaxField = (TextField) ((HBox)tempBox.getChildren().get(2)).getChildren().get(1);
        ToggleSwitch tempActiveToggle = (ToggleSwitch) ((HBox)tempBox.getChildren().get(3)).getChildren().get(1);
        
        // Humidity
        VBox humidityBox = createThresholdBox("Humidity (%)", 
            new String[]{"Min", "Max", "Active"});
        TextField humidityMinField = (TextField) ((HBox)humidityBox.getChildren().get(1)).getChildren().get(1);
        TextField humidityMaxField = (TextField) ((HBox)humidityBox.getChildren().get(2)).getChildren().get(1);
        ToggleSwitch humidityActiveToggle = (ToggleSwitch) ((HBox)humidityBox.getChildren().get(3)).getChildren().get(1);
        
        // Rainfall
        VBox rainfallBox = createThresholdBox("Rainfall (mm)", 
            new String[]{"Max", "Active"});
        TextField rainfallMaxField = (TextField) ((HBox)rainfallBox.getChildren().get(1)).getChildren().get(1);
        ToggleSwitch rainfallActiveToggle = (ToggleSwitch) ((HBox)rainfallBox.getChildren().get(2)).getChildren().get(1);
        
        // Wind Speed
        VBox windSpeedBox = createThresholdBox("Wind Speed (m/s)", 
            new String[]{"Max", "Active"});
        TextField windSpeedMaxField = (TextField) ((HBox)windSpeedBox.getChildren().get(1)).getChildren().get(1);
        ToggleSwitch windSpeedActiveToggle = (ToggleSwitch) ((HBox)windSpeedBox.getChildren().get(2)).getChildren().get(1);
        
        // Dust
        VBox dustBox = createThresholdBox("Dust (AQI)", 
            new String[]{"Max", "Active"});
        TextField dustMaxField = (TextField) ((HBox)dustBox.getChildren().get(1)).getChildren().get(1);
        ToggleSwitch dustActiveToggle = (ToggleSwitch) ((HBox)dustBox.getChildren().get(2)).getChildren().get(1);
        
        // Populate threshold fields with loaded data
        if (initialThreshold != null) {
            if (initialThreshold.temperatureMin() != null) {
                tempMinField.setText(initialThreshold.temperatureMin().toString());
            }
            if (initialThreshold.temperatureMax() != null) {
                tempMaxField.setText(initialThreshold.temperatureMax().toString());
            }
            tempActiveToggle.setSwitchedOn(initialThreshold.temperatureActive() != null && initialThreshold.temperatureActive());
            
            if (initialThreshold.humidityMin() != null) {
                humidityMinField.setText(initialThreshold.humidityMin().toString());
            }
            if (initialThreshold.humidityMax() != null) {
                humidityMaxField.setText(initialThreshold.humidityMax().toString());
            }
            humidityActiveToggle.setSwitchedOn(initialThreshold.humidityActive() != null && initialThreshold.humidityActive());
            
            if (initialThreshold.rainfallMax() != null) {
                rainfallMaxField.setText(initialThreshold.rainfallMax().toString());
            }
            rainfallActiveToggle.setSwitchedOn(initialThreshold.rainfallActive() != null && initialThreshold.rainfallActive());
            
            if (initialThreshold.windSpeedMax() != null) {
                windSpeedMaxField.setText(initialThreshold.windSpeedMax().toString());
            }
            windSpeedActiveToggle.setSwitchedOn(initialThreshold.windSpeedActive() != null && initialThreshold.windSpeedActive());
            
            if (initialThreshold.dustMax() != null) {
                dustMaxField.setText(initialThreshold.dustMax().toString());
            }
            dustActiveToggle.setSwitchedOn(initialThreshold.dustActive() != null && initialThreshold.dustActive());
        }
        
        // Add all to main content
        mainContent.getChildren().addAll(
            sectionTitle1,
            stationInfoGrid,
            publicBox,
            separator1,
            sectionTitle2,
            tempBox,
            humidityBox,
            rainfallBox,
            windSpeedBox,
            dustBox
        );
        
        scrollPane.setContent(mainContent);
        dialog.getDialogPane().setContent(scrollPane);
        
        // Handle save button
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                new Thread(() -> {
                    try {
                        boolean hasChanges = false;
                        StringBuilder successMessage = new StringBuilder();
                        
                        // Check if station info changed
                        boolean stationInfoChanged = !nameField.getText().equals(station.name()) ||
                                                     !locationField.getText().equals(station.location()) ||
                                                     selectedLat[0] != (station.latitude() != null ? station.latitude().doubleValue() : 0) ||
                                                     selectedLng[0] != (station.longitude() != null ? station.longitude().doubleValue() : 0);
                        
                        if (stationInfoChanged) {
                            UpdateStationRequest updateRequest = new UpdateStationRequest(
                                nameField.getText().trim(),
                                locationField.getText().trim(),
                                BigDecimal.valueOf(selectedLat[0]),
                                BigDecimal.valueOf(selectedLng[0])
                            );
                            stationService.updateStation(station.id(), updateRequest);
                            successMessage.append("✓ Station info updated\n");
                            hasChanges = true;
                        }
                        
                        // Check if public status changed
                        boolean publicStatusChanged = publicStationToggle.isSwitchedOn() != 
                                                     (station.isPublic() != null && station.isPublic());
                        
                        if (publicStatusChanged) {
                            stationService.updateStationSharing(station.id());
                            successMessage.append("✓ Public status updated\n");
                            hasChanges = true;
                        }
                        
                        // Check if threshold changed (compare with loaded threshold)
                        if (initialThreshold != null) {
                            ThresholdResponse current = initialThreshold;
                            
                            boolean thresholdChanged = 
                                !ValidationUtil.equalValues(ValidationUtil.parseDecimal(tempMinField.getText()), current.temperatureMin()) ||
                                !ValidationUtil.equalValues(ValidationUtil.parseDecimal(tempMaxField.getText()), current.temperatureMax()) ||
                                !ValidationUtil.equalValues(ValidationUtil.parseDecimal(humidityMinField.getText()), current.humidityMin()) ||
                                !ValidationUtil.equalValues(ValidationUtil.parseDecimal(humidityMaxField.getText()), current.humidityMax()) ||
                                !ValidationUtil.equalValues(ValidationUtil.parseDecimal(rainfallMaxField.getText()), current.rainfallMax()) ||
                                !ValidationUtil.equalValues(ValidationUtil.parseDecimal(windSpeedMaxField.getText()), current.windSpeedMax()) ||
                                !ValidationUtil.equalValues(ValidationUtil.parseDecimal(dustMaxField.getText()), current.dustMax()) ||
                                tempActiveToggle.isSwitchedOn() != (current.temperatureActive() != null && current.temperatureActive()) ||
                                humidityActiveToggle.isSwitchedOn() != (current.humidityActive() != null && current.humidityActive()) ||
                                rainfallActiveToggle.isSwitchedOn() != (current.rainfallActive() != null && current.rainfallActive()) ||
                                windSpeedActiveToggle.isSwitchedOn() != (current.windSpeedActive() != null && current.windSpeedActive()) ||
                                dustActiveToggle.isSwitchedOn() != (current.dustActive() != null && current.dustActive());
                            
                            if (thresholdChanged) {
                                UpdateThresholdRequest request = new UpdateThresholdRequest(
                                    ValidationUtil.parseDecimal(tempMinField.getText()),
                                    ValidationUtil.parseDecimal(tempMaxField.getText()),
                                    ValidationUtil.parseDecimal(humidityMinField.getText()),
                                    ValidationUtil.parseDecimal(humidityMaxField.getText()),
                                    ValidationUtil.parseDecimal(rainfallMaxField.getText()),
                                    ValidationUtil.parseDecimal(windSpeedMaxField.getText()),
                                    ValidationUtil.parseDecimal(dustMaxField.getText()),
                                    tempActiveToggle.isSwitchedOn(),
                                    humidityActiveToggle.isSwitchedOn(),
                                    rainfallActiveToggle.isSwitchedOn(),
                                    windSpeedActiveToggle.isSwitchedOn(),
                                    dustActiveToggle.isSwitchedOn()
                                );
                                
                                thresholdService.updateThreshold(current.id(), request);
                                successMessage.append("✓ Threshold settings updated\n");
                                hasChanges = true;
                            }
                        }
                        
                        if (hasChanges) {
                            final String message = successMessage.toString();
                            Platform.runLater(() -> {
                                showSuccess(message);
                                if (onSuccess != null) {
                                    onSuccess.run();
                                }
                            });
                        } else {
                            Platform.runLater(() -> {
                                showSuccess("No changes detected");
                            });
                        }
                        
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showError("Update failed: " + e.getMessage());
                        });
                    }
                }).start();
            }
            return dialogButton;
        });
        
        dialog.showAndWait();
    }
    
    private VBox createThresholdBox(String title, String[] fields) {
        VBox box = new VBox(8);
        box.setStyle("-fx-padding: 12; -fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 6;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        box.getChildren().add(titleLabel);
        
        for (String field : fields) {
            HBox fieldBox = new HBox(10);
            fieldBox.setAlignment(Pos.CENTER_LEFT);
            
            Label label = new Label(field + ":");
            label.setPrefWidth(60);
            
            if ("Active".equals(field)) {
                ToggleSwitch toggle = new ToggleSwitch();
                fieldBox.getChildren().addAll(label, toggle);
            } else {
                TextField textField = new TextField();
                textField.setPrefWidth(100);
                textField.setPromptText("0");
                fieldBox.getChildren().addAll(label, textField);
            }
            
            box.getChildren().add(fieldBox);
        }
        
        return box;
    }
    
    public UpdateStationDialogController setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
        return this;
    }
    
    private void showError(String message) {
        AlertService.showError(message);
    }
    
    private void showSuccess(String message) {
        AlertService.showSuccess(message);
    }
}
