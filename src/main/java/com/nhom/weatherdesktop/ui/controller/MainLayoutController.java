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
import com.nhom.weatherdesktop.ui.NavigationManager;
import com.nhom.weatherdesktop.ui.manager.AlertsManager;
import com.nhom.weatherdesktop.ui.manager.SidebarManager;
import com.nhom.weatherdesktop.ui.manager.WeatherDisplayManager;
import com.nhom.weatherdesktop.util.AlertService;
import com.nhom.weatherdesktop.util.AsyncTaskRunner;
import com.nhom.weatherdesktop.util.ErrorHandler;
import com.nhom.weatherdesktop.websocket.StompClient;
import static com.nhom.weatherdesktop.config.UIConstants.*;
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
    private final com.nhom.weatherdesktop.service.AlertService alertService = new com.nhom.weatherdesktop.service.AlertService();
    private final StompClient stompClient = new StompClient();
    private final NavigationManager navigationManager = new NavigationManager();
    
    // Managers
    private WeatherDisplayManager weatherDisplayManager;
    private SidebarManager sidebarManager;
    private AlertsManager alertsManager;
    
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
    private VBox alertsListContainer;
    
    @FXML
    private Label unreadCountLabel;
    
    @FXML
    private VBox emptyAlertsState;
    
    @FXML
    private Button showStationsButton;
    
    @FXML
    private Button addStationButton;
    
    @FXML
    private VBox stationListContainer;

    @FXML
    public void initialize() {
        // Initialize managers
        weatherDisplayManager = new WeatherDisplayManager(
            temperatureValue, humidityValue, windspeedValue,
            rainfallValue, dustValue
        );
        
        sidebarManager = new SidebarManager(
            sidebar, toggleSidebarButton,
            myStationText, publicStationsText, alertsText, settingsText
        );
        
        alertsManager = new AlertsManager(
            alertsListContainer, unreadCountLabel, emptyAlertsState,
            alertService
        );
        alertsManager.setErrorHandler(this::showError);
        
        // Register views with NavigationManager
        navigationManager.registerView("myStation", myStationView, myStationButton);
        navigationManager.registerView("publicStations", publicStationsView, publicStationsButton);
        navigationManager.registerView("alerts", alertsView, alertsButton);
        navigationManager.registerView("settings", settingsView, settingsButton);
        
        // Set initial view to MyStation
        navigationManager.showView("myStation");
        
        // Set initial weather values
        weatherDisplayManager.reset();
        
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
        // Hide station list when switching views
        stationListContainer.setVisible(false);
        stationListContainer.setManaged(false);
        showStationsButton.setText("📋 Show Station List");
        
        navigationManager.showView("myStation");
    }

    @FXML
    private void handlePublicStations() {
        navigationManager.showView("publicStations");
    }

    @FXML
    private void handleAlerts() {
        navigationManager.showView("alerts");
        alertsManager.loadAlerts();
    }

    @FXML
    private void handleSettings() {
        navigationManager.showView("settings");
    }
    
    @FXML
    private void handleToggleSidebar() {
        sidebarManager.toggle();
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
        AsyncTaskRunner.runAsync(
            // Background task
            () -> {
                try {
                    PageResponse<StationResponse> response = stationService.getMyStations(0, DEFAULT_PAGE_SIZE);
                    return response.content();
                } catch (Exception e) {
                    throw new RuntimeException("Cannot load station list: " + e.getMessage(), e);
                }
            },
            // On success
            stations -> {
                userStations = stations;
                displayStations(userStations);
                subscribeToStations(userStations);
            },
            // On error
            e -> showError(ErrorHandler.getUserMessage(e))
        );
    }
    
    /**
     * Subscribe to all alert topics and first station's weather topic
     */
    private void subscribeToStations(List<StationResponse> stations) {
        if (stations == null || stations.isEmpty()) {
            return;
        }
        
        new Thread(() -> {
            int retries = WEBSOCKET_RETRY_COUNT;
            while (!stompClient.isConnected() && retries > 0) {
                try {
                    Thread.sleep(WEBSOCKET_RETRY_DELAY_MS);
                    retries--;
                } catch (InterruptedException e) {
                    break;
                }
            }
            
            if (!stompClient.isConnected()) {
                Platform.runLater(() -> 
                    showError("WebSocket not connected. Cannot subscribe.")
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
        weatherDisplayManager.reset();
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

    // ========== WebSocket Handlers ==========
    
    private void handleWeatherData(WeatherDataResponse data) {
        weatherDisplayManager.updateWeatherData(data);
    }
    
    private void handleAlert(AlertResponse alert) {
        if (userStations == null) return;
        
        String stationName = userStations.stream()
                .filter(s -> s.id().equals(alert.stationId()))
                .map(StationResponse::name)
                .findFirst()
                .orElse("Station #" + alert.stationId());
        
        String title = "⚠️ Alert from " + stationName;
        String message = alert.message();
        com.nhom.weatherdesktop.util.AlertService.showWarning(title, message);
    }
    
    private void handleConnectionStatus(Boolean connected) {
        // TODO: Update connection indicator in UI
    }
    
    // ========== Alert Management ==========
    
    private void loadAlerts() {
        AsyncTaskRunner.runAsync(
            // Background task
            () -> {
                try {
                    List<com.nhom.weatherdesktop.dto.response.AlertResponse> alertResponses = 
                        alertService.getAllMyAlerts();
                    
                    return alertResponses.stream()
                            .map(com.nhom.weatherdesktop.model.Alert::fromResponse)
                            .toList();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load alerts: " + e.getMessage(), e);
                }
            },
            // On success
            this::displayAlerts,
            // On error
            e -> showError(ErrorHandler.getUserMessage(e))
        );
    }
    
    private void displayAlerts(List<com.nhom.weatherdesktop.model.Alert> alerts) {
        alertsListContainer.getChildren().clear();
        
        if (alerts == null || alerts.isEmpty()) {
            emptyAlertsState.setVisible(true);
            unreadCountLabel.setVisible(false);
            return;
        }
        
        emptyAlertsState.setVisible(false);
        
        // Count unread alerts
        long unreadCount = alerts.stream().filter(com.nhom.weatherdesktop.model.Alert::isNew).count();
        if (unreadCount > 0) {
            unreadCountLabel.setText(String.valueOf(unreadCount));
            unreadCountLabel.setVisible(true);
        } else {
            unreadCountLabel.setVisible(false);
        }
        
        // Display alerts
        for (com.nhom.weatherdesktop.model.Alert alert : alerts) {
            com.nhom.weatherdesktop.ui.component.AlertCard card = 
                new com.nhom.weatherdesktop.ui.component.AlertCard(alert, alertService, this::loadAlerts);
            alertsListContainer.getChildren().add(card);
        }
    }
}
