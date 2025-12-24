package com.nhom.weatherdesktop.ui.controller;

import com.nhom.weatherdesktop.dto.request.AddStationRequest;
import com.nhom.weatherdesktop.dto.request.UpdateStationRequest;
import com.nhom.weatherdesktop.dto.request.UpdateThresholdRequest;
import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.dto.response.ThresholdResponse;
import com.nhom.weatherdesktop.service.StationService;
import com.nhom.weatherdesktop.service.ThresholdService;
import com.nhom.weatherdesktop.ui.component.ToggleSwitch;
import com.nhom.weatherdesktop.ui.component.MapLocationPicker;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.util.Optional;

public class MainLayoutController {

    private final StationService stationService = new StationService();
    private final ThresholdService thresholdService = new ThresholdService();

    // Sidebar
    @FXML
    private VBox sidebar;
    
    @FXML
    private Button toggleSidebarButton;
    
    @FXML
    private Label myStationText;
    
    @FXML
    private Label publicStationsText;
    
    @FXML
    private Label alertsText;
    
    @FXML
    private Label settingsText;
    
    // Sidebar state
    private boolean isSidebarCollapsed = false;
    private static final double SIDEBAR_EXPANDED_WIDTH = 220.0;
    private static final double SIDEBAR_COLLAPSED_WIDTH = 60.0;


    // Sidebar buttons
    @FXML
    private Button myStationButton;
    
    @FXML
    private Button publicStationsButton;
    
    @FXML
    private Button alertsButton;
    
    @FXML
    private Button settingsButton;

    // Views
    @FXML
    private AnchorPane myStationView;
    
    @FXML
    private VBox publicStationsView;
    
    @FXML
    private VBox alertsView;
    
    @FXML
    private VBox settingsView;

    // MyStation components
    @FXML
    private Label temperatureValue;
    
    @FXML
    private Label humidityValue;
    
    @FXML
    private Label windspeedValue;
    
    @FXML
    private Label rainfallValue;
    
    @FXML
    private Label dustValue;
    

    

    

    

    
    @FXML
    private Button showStationsButton;
    
    @FXML
    private Button addStationButton;
    
    @FXML
    private VBox stationListContainer;

    @FXML
    public void initialize() {
        // Set initial view to MyStation
        showMyStationView();
        

    }

    @FXML
    private void handleMyStations() {
        showMyStationView();
        updateActiveButton(myStationButton);
    }

    @FXML
    private void handlePublicStations() {
        showView(publicStationsView);
        updateActiveButton(publicStationsButton);
    }

    @FXML
    private void handleAlerts() {
        showView(alertsView);
        updateActiveButton(alertsButton);
    }

    @FXML
    private void handleSettings() {
        showView(settingsView);
        updateActiveButton(settingsButton);
    }
    
    @FXML
    private void handleToggleSidebar() {
        isSidebarCollapsed = !isSidebarCollapsed;
        
        if (isSidebarCollapsed) {
            collapseSidebar();
        } else {
            expandSidebar();
        }
    }
    
    private void collapseSidebar() {
        // Set sidebar width to collapsed size
        sidebar.setPrefWidth(SIDEBAR_COLLAPSED_WIDTH);
        sidebar.setMinWidth(SIDEBAR_COLLAPSED_WIDTH);
        sidebar.setMaxWidth(SIDEBAR_COLLAPSED_WIDTH);
        
        // Hide text labels
        myStationText.setVisible(false);
        myStationText.setManaged(false);
        publicStationsText.setVisible(false);
        publicStationsText.setManaged(false);
        alertsText.setVisible(false);
        alertsText.setManaged(false);
        settingsText.setVisible(false);
        settingsText.setManaged(false);
        
        // Center the toggle button
        toggleSidebarButton.setText("☰");
    }
    
    private void expandSidebar() {
        // Set sidebar width to expanded size
        sidebar.setPrefWidth(SIDEBAR_EXPANDED_WIDTH);
        sidebar.setMinWidth(SIDEBAR_EXPANDED_WIDTH);
        sidebar.setMaxWidth(SIDEBAR_EXPANDED_WIDTH);
        
        // Show text labels
        myStationText.setVisible(true);
        myStationText.setManaged(true);
        publicStationsText.setVisible(true);
        publicStationsText.setManaged(true);
        alertsText.setVisible(true);
        alertsText.setManaged(true);
        settingsText.setVisible(true);
        settingsText.setManaged(true);
        
        // Reset toggle button
        toggleSidebarButton.setText("☰");
    }

    @FXML
    private void handleShowStationList() {
        // Toggle station list visibility
        boolean isVisible = stationListContainer.isVisible();
        
        if (!isVisible) {
            // Load stations when showing the list
            loadStations();
            showStationsButton.setText("🔼 Hide Station List");
        } else {
            showStationsButton.setText("📋 Show Station List");
        }
        
        stationListContainer.setVisible(!isVisible);
        stationListContainer.setManaged(!isVisible);
    }
    
    @FXML
    private void handleAddStation() {
        // Create custom dialog
        Dialog<AddStationRequest> dialog = new Dialog<>();
        dialog.setTitle("Add New Station");
        dialog.setHeaderText("Enter station details to attach to your account");

        // Set button types
        ButtonType addButtonType = new ButtonType("Add Station", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Station name");
        
        TextField apiKeyField = new TextField();
        apiKeyField.setPromptText("XXXX-XXXX-XXXX-XXXX");
        
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
        
        Button pickLocationBtn = new Button("🗺️ Pick on Map");
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
                        // Reload station list
                        loadStations();
                    });
                    
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        showError("Failed to add station: " + e.getMessage());
                    });
                }
            }).start();
        });
    }
    
    private void loadStations() {
        // Run API call in background thread
        new Thread(() -> {
            try {
                PageResponse<StationResponse> response = stationService.getMyStations(0, 10);
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    displayStations(response.content());
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Không thể tải danh sách trạm: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void displayStations(java.util.List<StationResponse> stations) {
        // Find the stationList VBox in the stationListContainer
        VBox stationList = null;
        for (Node node : stationListContainer.getChildren()) {
            if (node instanceof javafx.scene.control.ScrollPane) {
                javafx.scene.control.ScrollPane scrollPane = (javafx.scene.control.ScrollPane) node;
                if (scrollPane.getContent() instanceof VBox) {
                    stationList = (VBox) scrollPane.getContent();
                    break;
                }
            }
        }
        
        if (stationList == null) {
            showError("Cannot find station list container");
            return;
        }
        
        // Clear existing items
        stationList.getChildren().clear();
        
        // Add stations dynamically
        for (StationResponse station : stations) {
            VBox stationCard = createStationCard(station);
            stationList.getChildren().add(stationCard);
        }
    }
    
    private VBox createStationCard(StationResponse station) {
        // Main container
        VBox card = new VBox(8);
        card.setStyle("-fx-padding: 16; -fx-background-color: rgba(59, 130, 246, 0.15); -fx-background-radius: 8; -fx-cursor: hand;");
        
        // Content HBox (main row with icon and info)
        HBox content = new HBox(12);
        content.setAlignment(Pos.CENTER_LEFT);
        
        // Icon
        Label icon = new Label("📍");
        icon.setStyle("-fx-font-size: 24px;");
        
        // Info VBox
        VBox info = new VBox(4);
        
        // Name
        Label nameLabel = new Label(station.name());
        nameLabel.setStyle("-fx-font-weight: 600; -fx-text-fill: white; -fx-font-size: 15px;");
        
        // Location
        Label locationLabel = new Label("Location: " + station.location());
        locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.7);");
        
        // Status
        String statusText = "ONLINE".equalsIgnoreCase(station.status()) ? "Online" : "Offline";
        String statusColor = "ONLINE".equalsIgnoreCase(station.status()) ? "#10B981" : "#EF4444";
        Label statusLabel = new Label("Status: " + statusText);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + statusColor + ";");
        
        info.getChildren().addAll(nameLabel, locationLabel, statusLabel);
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        // Action buttons container (initially hidden)
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);
        actionButtons.setVisible(false);
        actionButtons.setManaged(false);
        
        // Update button
        Button updateBtn = new Button("✏️");
        updateBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-size: 16px; " +
                          "-fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");
        updateBtn.setOnAction(e -> handleUpdateStation(station));
        
        // Detach button
        Button detachBtn = new Button("🗑️");
        detachBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-size: 16px; " +
                          "-fx-padding: 6 12; -fx-background-radius: 6; -fx-cursor: hand;");
        detachBtn.setOnAction(e -> handleDetachStation(station));
        
        actionButtons.getChildren().addAll(updateBtn, detachBtn);
        
        content.getChildren().addAll(icon, info, spacer, actionButtons);
        card.getChildren().add(content);
        
        // Hover effect to show/hide action buttons
        card.setOnMouseEntered(e -> {
            card.setStyle("-fx-padding: 16; -fx-background-color: rgba(59, 130, 246, 0.25); -fx-background-radius: 8; -fx-cursor: hand;");
            actionButtons.setVisible(true);
            actionButtons.setManaged(true);
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-padding: 16; -fx-background-color: rgba(59, 130, 246, 0.15); -fx-background-radius: 8; -fx-cursor: hand;");
            actionButtons.setVisible(false);
            actionButtons.setManaged(false);
        });
        
        return card;
    }
    
    private void handleUpdateStation(StationResponse station) {
        // Load fresh data from server in background
        new Thread(() -> {
            try {
                // Fetch latest station data
                StationResponse freshStation = stationService.getStationById(station.id());
                
                // Fetch threshold data
                ThresholdResponse threshold = thresholdService.getThresholdByStationId(station.id());
                
                // Open dialog on UI thread with fresh data
                Platform.runLater(() -> openUpdateDialog(freshStation, threshold));
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Failed to load station data: " + e.getMessage());
                });
            }
        }).start();
    }
    
    private void openUpdateDialog(StationResponse station, ThresholdResponse initialThreshold) {
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
                    String address = reverseGeocode(station.latitude().doubleValue(), station.longitude().doubleValue());
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
                                !equalValues(parseDecimal(tempMinField.getText()), current.temperatureMin()) ||
                                !equalValues(parseDecimal(tempMaxField.getText()), current.temperatureMax()) ||
                                !equalValues(parseDecimal(humidityMinField.getText()), current.humidityMin()) ||
                                !equalValues(parseDecimal(humidityMaxField.getText()), current.humidityMax()) ||
                                !equalValues(parseDecimal(rainfallMaxField.getText()), current.rainfallMax()) ||
                                !equalValues(parseDecimal(windSpeedMaxField.getText()), current.windSpeedMax()) ||
                                !equalValues(parseDecimal(dustMaxField.getText()), current.dustMax()) ||
                                tempActiveToggle.isSwitchedOn() != (current.temperatureActive() != null && current.temperatureActive()) ||
                                humidityActiveToggle.isSwitchedOn() != (current.humidityActive() != null && current.humidityActive()) ||
                                rainfallActiveToggle.isSwitchedOn() != (current.rainfallActive() != null && current.rainfallActive()) ||
                                windSpeedActiveToggle.isSwitchedOn() != (current.windSpeedActive() != null && current.windSpeedActive()) ||
                                dustActiveToggle.isSwitchedOn() != (current.dustActive() != null && current.dustActive());
                            
                            if (thresholdChanged) {
                                UpdateThresholdRequest request = new UpdateThresholdRequest(
                                    parseDecimal(tempMinField.getText()),
                                    parseDecimal(tempMaxField.getText()),
                                    parseDecimal(humidityMinField.getText()),
                                    parseDecimal(humidityMaxField.getText()),
                                    parseDecimal(rainfallMaxField.getText()),
                                    parseDecimal(windSpeedMaxField.getText()),
                                    parseDecimal(dustMaxField.getText()),
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
                                loadStations(); // Reload station list
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
    
    private boolean equalValues(BigDecimal v1, BigDecimal v2) {
        if (v1 == null && v2 == null) return true;
        if (v1 == null || v2 == null) return false;
        return v1.compareTo(v2) == 0;
    }
    
    private BigDecimal parseDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private VBox createThresholdBox(String title, String[] fields) {
        VBox box = new VBox(8);
        box.setStyle("-fx-padding: 12; -fx-background-color: rgba(59, 130, 246, 0.1); -fx-background-radius: 6;");
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold;");
        box.getChildren().add(titleLabel);
        
        for (String field : fields) {
            if (field.equals("Active")) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                Label label = new Label(field + ":");
                label.setPrefWidth(60);
                ToggleSwitch toggleSwitch = new ToggleSwitch();
                row.getChildren().addAll(label, toggleSwitch);
                box.getChildren().add(row);
            } else {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                Label label = new Label(field + ":");
                label.setPrefWidth(60);
                TextField textField = new TextField();
                textField.setPrefWidth(150);
                textField.setPromptText("Enter " + field.toLowerCase());
                row.getChildren().addAll(label, textField);
                box.getChildren().add(row);
            }
        }
        
        return box;
    }
    
    private void handleDetachStation(StationResponse station) {
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Detach Station");
        confirmDialog.setHeaderText("Are you sure you want to detach this station?");
        confirmDialog.setContentText("Station: " + station.name() + "\nLocation: " + station.location());
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // TODO: Call detach API
            showError("Detach station API not implemented yet!");
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showMyStationView() {
        // Hide station list when switching views
        stationListContainer.setVisible(false);
        stationListContainer.setManaged(false);
        showStationsButton.setText("📋 Show Station List");
        
        showView(myStationView);
    }

    private void showView(Node viewToShow) {
        // Hide all views
        myStationView.setVisible(false);
        myStationView.setManaged(false);
        
        publicStationsView.setVisible(false);
        publicStationsView.setManaged(false);
        
        alertsView.setVisible(false);
        alertsView.setManaged(false);
        
        settingsView.setVisible(false);
        settingsView.setManaged(false);

        // Show the selected view
        viewToShow.setVisible(true);
        viewToShow.setManaged(true);
    }

    private void updateActiveButton(Button activeButton) {
        // Remove active class from all buttons
        myStationButton.getStyleClass().remove("active");
        publicStationsButton.getStyleClass().remove("active");
        alertsButton.getStyleClass().remove("active");
        settingsButton.getStyleClass().remove("active");

        // Add active class to the clicked button
        if (!activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }
    
    private String reverseGeocode(double lat, double lng) {
        try {
            String url = String.format(
                "https://nominatim.openstreetmap.org/reverse?format=json&lat=%.6f&lon=%.6f&zoom=18&addressdetails=1",
                lat, lng
            );
            
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("User-Agent", "WeatherDesktop/1.0")
                .GET()
                .build();
                
            java.net.http.HttpResponse<String> response = 
                client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
                
            if (response.statusCode() == 200) {
                com.fasterxml.jackson.databind.JsonNode json = 
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.body());
                    
                if (json.has("display_name")) {
                    return json.get("display_name").asText();
                }
            }
            
            return "";
        } catch (Exception e) {
            System.err.println("Reverse geocoding failed: " + e.getMessage());
            return "";
        }
    }
}
