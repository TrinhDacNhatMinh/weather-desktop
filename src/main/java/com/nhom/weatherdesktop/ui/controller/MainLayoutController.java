package com.nhom.weatherdesktop.ui.controller;

import com.nhom.weatherdesktop.config.AppConfig;
import com.nhom.weatherdesktop.dto.response.AlertResponse;
import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.dto.response.WeatherDataResponse;
import com.nhom.weatherdesktop.service.StationService;
import com.nhom.weatherdesktop.service.ThresholdService;
import com.nhom.weatherdesktop.ui.component.StationCard;
import com.nhom.weatherdesktop.ui.controller.dialog.AddStationDialogController;
import com.nhom.weatherdesktop.ui.controller.dialog.UpdateStationDialogController;
import com.nhom.weatherdesktop.util.AlertService;
import com.nhom.weatherdesktop.websocket.StompClient;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

public class MainLayoutController {

    private final StationService stationService = new StationService();
    private final ThresholdService thresholdService = new ThresholdService();
    private final StompClient stompClient = new StompClient();
    
    private List<StationResponse> userStations;
    private Long currentStationId; // Currently subscribed station for weather data

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
    private Label stationNameLabel;
    
    @FXML
    private Label stationLocationLabel;
    

    

    

    

    
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
        
        // Set initial weather values
        resetWeatherValues();
        
        // Initialize WebSocket connection
        initializeWebSocket();
    }
    
    private void initializeWebSocket() {
        stompClient.setWeatherDataHandler(this::handleWeatherData);
        stompClient.setAlertHandler(this::handleAlert);
        stompClient.setConnectionStatusHandler(this::handleConnectionStatus);
        
        String wsUrl = AppConfig.getWebSocketUrl();
        stompClient.connect(wsUrl);
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
        new AddStationDialogController(stationService)
            .setOnSuccess(this::loadStations)
            .showDialog(sidebar.getScene().getWindow());
    }
    
    private void loadStations() {
        // Run API call in background thread
        new Thread(() -> {
            try {
                PageResponse<StationResponse> response = stationService.getMyStations(0, 10);
                userStations = response.content();
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    displayStations(userStations);
                    subscribeToStations(userStations);
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Không thể tải danh sách trạm: " + e.getMessage());
                });
            }
        }).start();
    }
    
    /**
     * Subscribe to all alert topics and first station's weather topic
     */
    private void subscribeToStations(List<StationResponse> stations) {
        if (stations == null || stations.isEmpty()) {
            return;
        }
        
        new Thread(() -> {
            int retries = 30;
            while (!stompClient.isConnected() && retries > 0) {
                try {
                    Thread.sleep(100);
                    retries--;
                } catch (InterruptedException e) {
                    break;
                }
            }
            
            if (!stompClient.isConnected()) {
                Platform.runLater(() -> 
                    showError("WebSocket chưa kết nối. Không thể subscribe.")
                );
                return;
            }
            
            Platform.runLater(() -> {
                // Subscribe to all alert topics
                for (StationResponse station : stations) {
                    String alertTopic = "/topic/stations/" + station.id() + "/alerts";
                    stompClient.subscribe(alertTopic, "alert");
                }
                
                // Subscribe to saved station or first station
                Long savedStationId = com.nhom.weatherdesktop.session.SessionContext.selectedStationId();
                StationResponse stationToSubscribe = null;
                
                if (savedStationId != null) {
                    // Find saved station in list
                    stationToSubscribe = stations.stream()
                            .filter(s -> s.id().equals(savedStationId))
                            .findFirst()
                            .orElse(null);
                }
                
                // Fallback to first station if saved station not found
                if (stationToSubscribe == null && !stations.isEmpty()) {
                    stationToSubscribe = stations.get(0);
                }
                
                if (stationToSubscribe != null) {
                    currentStationId = stationToSubscribe.id();
                    String weatherTopic = "/topic/stations/" + currentStationId + "/weather";
                    stompClient.subscribe(weatherTopic, "weather");
                    
                    // Update station info display
                    stationNameLabel.setText(stationToSubscribe.name());
                    stationLocationLabel.setText(stationToSubscribe.location());
                }
            });
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
            StationCard stationCard = new StationCard(station);
            stationCard.setOnMouseClicked(event -> switchWeatherStation(station.id()));
            stationCard.setOnEdit(this::handleUpdateStation);
            stationCard.setOnDetach(this::handleDetachStation);
            stationList.getChildren().add(stationCard);
        }
    }
    
    private void switchWeatherStation(Long newStationId) {
        if (newStationId.equals(currentStationId)) return;
        if (currentStationId != null) {
            stompClient.unsubscribe("/topic/stations/" + currentStationId + "/weather");
        }
        
        // Reset weather values when switching
        resetWeatherValues();
        
        stompClient.subscribe("/topic/stations/" + newStationId + "/weather", "weather");
        currentStationId = newStationId;
        
        // Update station info display
        if (userStations != null) {
            userStations.stream()
                    .filter(s -> s.id().equals(newStationId))
                    .findFirst()
                    .ifPresent(station -> {
                        stationNameLabel.setText(station.name());
                        stationLocationLabel.setText(station.location());
                    });
        }
        
        // Save selected station for next time
        com.nhom.weatherdesktop.session.SessionContext.setSelectedStationId(newStationId);
    }
    
    private void resetWeatherValues() {
        temperatureValue.setText("--");
        humidityValue.setText("--");
        windspeedValue.setText("--");
        rainfallValue.setText("--");
        dustValue.setText("--");
    }
    
    private void handleUpdateStation(StationResponse station) {
        new UpdateStationDialogController(stationService, thresholdService)
            .setOnSuccess(() -> {
                loadStations(); // Refresh station list to get updated name
            })
            .showDialog(station.id());
    }
    
    private void handleDetachStation(StationResponse station) {
        if (AlertService.confirm("Detach Station", 
            "Are you sure you want to detach station '" + station.name() + "' from your account?")) {
            try {
                stationService.detachStationFromUser(station.id());
                AlertService.showSuccess("Station '" + station.name() + "' detached successfully!");
                
                // Reload station list
                loadStations();
                
            } catch (Exception e) {
                AlertService.showError("Failed to detach station: " + e.getMessage());
            }
        }
    }
    
    private void showError(String message) {
        AlertService.showError(message);
    }
    
    private void showSuccess(String message) {
        AlertService.showSuccess(message);
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
    
    // ========== WebSocket Handlers ==========
    
    private void handleWeatherData(WeatherDataResponse data) {
        if (data.temperature() != null) temperatureValue.setText(String.format("%.1f", data.temperature()));
        if (data.humidity() != null) humidityValue.setText(String.format("%.0f", data.humidity()));
        if (data.windSpeed() != null) windspeedValue.setText(String.format("%.1f", data.windSpeed()));
        if (data.rainfall() != null) rainfallValue.setText(String.format("%.1f", data.rainfall()));
        if (data.dust() != null) dustValue.setText(String.format("%.1f", data.dust()));
    }
    
    private void handleAlert(AlertResponse alert) {
        if (userStations == null) return;
        
        String stationName = userStations.stream()
                .filter(s -> s.id().equals(alert.stationId()))
                .map(StationResponse::name)
                .findFirst()
                .orElse("Station #" + alert.stationId());
        
        String title = "⚠️ Alert from " + stationName;
        String message = alert.message() + "\nType: " + alert.type() + "\nSeverity: " + alert.severity();
        AlertService.showWarning(title, message);
    }
    
    private void handleConnectionStatus(Boolean connected) {
        // TODO: Update connection indicator in UI
    }
}
