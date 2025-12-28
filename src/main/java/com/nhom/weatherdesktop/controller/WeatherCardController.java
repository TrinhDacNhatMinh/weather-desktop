package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.dto.response.WeatherDataResponse;
import com.nhom.weatherdesktop.service.StationService;
import com.nhom.weatherdesktop.session.SessionContext;
import com.nhom.weatherdesktop.util.AppConfig;
import com.nhom.weatherdesktop.websocket.StompClient;
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
    
    @FXML
    private Text stationNameText;
    
    private ContextMenu stationMenu;
    private final StationService stationService;
    private volatile boolean isLoadingStations = false;
    private static StationResponse selectedStation; // Static to persist across controller instances
    
    // WebSocket
    private static StompClient stompClient; // Static to share connection across instances
    private static Long currentSubscribedStationId;
    
    public WeatherCardController() {
        this.stationService = new StationService();
    }
    
    @FXML
    public void initialize() {
        // Initialize WebSocket if not already initialized
        initializeWebSocket();
        
        // Load first station on startup if no station selected
        loadFirstStation();
    }
    
    private void initializeWebSocket() {
        if (stompClient == null) {
            stompClient = new StompClient();
            stompClient.setWeatherDataHandler(this::handleWeatherData);
            stompClient.setConnectionStatusHandler(this::handleConnectionStatus);
            
            // Connect to WebSocket
            AppConfig config = AppConfig.getInstance();
            String wsUrl = config.getWebSocketUrl();
            stompClient.connect(wsUrl);
            
            logger.info("WebSocket initialized and connecting to: {}", wsUrl);
        }
    }
    
    private void handleWeatherData(WeatherDataResponse data) {
        logger.debug("Received weather data: temp={}, humidity={}", data.temperature(), data.humidity());
        updateWeatherData(
            data.temperature(),
            data.humidity(),
            data.windSpeed(),
            data.rainfall(),
            data.dust()
        );
    }
    
    private void handleConnectionStatus(Boolean connected) {
        logger.info("WebSocket connection status: {}", connected ? "Connected" : "Disconnected");
        
        if (connected && selectedStation != null) {
            // Re-subscribe to current station when connection is restored
            subscribeToStation(selectedStation.id());
        }
    }
    
    private void loadFirstStation() {
        // If already have a selected station, use it
        if (selectedStation != null) {
            stationNameText.setText(selectedStation.name());
            logger.info("Restored previously selected station: {}", selectedStation.name());
            
            // Subscribe to station weather topic if not already subscribed
            if (!selectedStation.id().equals(currentSubscribedStationId)) {
                subscribeToStation(selectedStation.id());
            }
            return;
        }
        
        // Otherwise, load first station from API
        new Thread(() -> {
            try {
                logger.debug("Loading first station...");
                PageResponse<StationResponse> response = stationService.getMyStations(0, 1);
                
                if (!response.content().isEmpty()) {
                    selectedStation = response.content().get(0);
                    Platform.runLater(() -> {
                        stationNameText.setText(selectedStation.name());
                        logger.info("Loaded first station: {}", selectedStation.name());
                        
                        // Subscribe to station weather topic
                        subscribeToStation(selectedStation.id());
                    });
                } else {
                    Platform.runLater(() -> {
                        stationNameText.setText("No Station");
                        logger.info("No stations found");
                    });
                }
            } catch (Exception e) {
                logger.error("Failed to load first station: {}", e.getMessage(), e);
                Platform.runLater(() -> stationNameText.setText("Current Weather"));
            }
        }).start();
    }
    
    private void subscribeToStation(Long stationId) {
        if (stompClient == null || !stompClient.isConnected()) {
            logger.warn("Cannot subscribe: WebSocket not connected");
            // Retry after delay
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    if (stompClient != null && stompClient.isConnected()) {
                        Platform.runLater(() -> subscribeToStation(stationId));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            return;
        }
        
        // Unsubscribe from previous station
        if (currentSubscribedStationId != null) {
            String oldTopic = "/topic/stations/" + currentSubscribedStationId + "/weather";
            stompClient.unsubscribe(oldTopic);
            logger.debug("Unsubscribed from: {}", oldTopic);
        }
        
        // Subscribe to new station
        String topic = "/topic/stations/" + stationId + "/weather";
        stompClient.subscribe(topic, "weather");
        currentSubscribedStationId = stationId;
        logger.info("Subscribed to weather topic: {}", topic);
        
        // Save to session
        SessionContext.setSelectedStationId(stationId);
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
        
        // Prevent concurrent loading
        if (isLoadingStations) {
            logger.debug("Station loading already in progress, skipping");
            return;
        }
        
        // Load stations in background
        isLoadingStations = true;
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
                                
                                // Add click handler to select station
                                stationItem.setOnMouseClicked(event -> {
                                    selectedStation = station;
                                    stationNameText.setText(station.name());
                                    stationMenu.hide();
                                    logger.info("Selected station: {}", station.name());
                                    
                                    // Subscribe to new station's weather topic
                                    subscribeToStation(station.id());
                                });
                                
                                CustomMenuItem menuItem = new CustomMenuItem(stationItem);
                                menuItem.setHideOnClick(false);  // Keep menu open when clicking items
                                stationMenu.getItems().add(menuItem);
                                logger.debug("Added station: {}", station.name());
                                
                            } catch (Exception e) {
                                logger.error("Failed to load station item for {}: {}", station.name(), e.getMessage(), e);
                            }
                        }
                    }
                    
                    // Add separator and "Add Station" button at the bottom
                    if (!response.content().isEmpty()) {
                        stationMenu.getItems().add(new javafx.scene.control.SeparatorMenuItem());
                    }
                    
                    // Create Add Station button
                    try {
                        HBox addButtonContent = new HBox(8);
                        addButtonContent.setAlignment(javafx.geometry.Pos.CENTER);
                        addButtonContent.setPadding(new javafx.geometry.Insets(12, 16, 12, 16));
                        addButtonContent.setStyle("-fx-cursor: hand;");
                        addButtonContent.setPrefWidth(300); // Match station item width
                        addButtonContent.setMinWidth(300);
                        addButtonContent.setMaxWidth(300);
                        
                        // Add icon
                        javafx.scene.image.ImageView addIcon = new javafx.scene.image.ImageView(
                            new javafx.scene.image.Image(getClass().getResourceAsStream("/icons/add.png"))
                        );
                        addIcon.setFitWidth(16);
                        addIcon.setFitHeight(16);
                        addIcon.setPreserveRatio(true);
                        
                        // Add text
                        Text addText = new Text("Add Station");
                        addText.setStyle("-fx-fill: #0078D4; -fx-font-size: 14px; -fx-font-weight: 600;");
                        
                        addButtonContent.getChildren().addAll(addIcon, addText);
                        
                        // Hover effect
                        addButtonContent.setOnMouseEntered(e -> 
                            addButtonContent.setStyle("-fx-cursor: hand; -fx-background-color: rgba(0, 0, 0, 0.03); -fx-background-radius: 6px;")
                        );
                        addButtonContent.setOnMouseExited(e -> 
                            addButtonContent.setStyle("-fx-cursor: hand; -fx-background-color: transparent;")
                        );
                        
                        // Click handler
                        addButtonContent.setOnMouseClicked(e -> handleAddStation());
                        
                        CustomMenuItem addMenuItem = new CustomMenuItem(addButtonContent);
                        addMenuItem.setHideOnClick(true);
                        stationMenu.getItems().add(addMenuItem);
                        
                    } catch (Exception e) {
                        logger.error("Failed to create Add Station button: {}", e.getMessage(), e);
                    }
                    
                    // Show menu below button, centered
                    logger.debug("Showing context menu");
                    
                    // Calculate offset to center menu relative to button
                    // Menu width is approximately 300px (station item width)
                    double menuWidth = 300;
                    double buttonWidth = menuButton.getWidth();
                    double offsetX = (buttonWidth - menuWidth) / 2;
                    
                    stationMenu.show(menuButton, Side.BOTTOM, offsetX, 5);
                });
                
            } catch (Exception e) {
                logger.error("Failed to fetch stations: {}", e.getMessage(), e);
                Platform.runLater(() -> {
                    CustomMenuItem errorItem = new CustomMenuItem(new Text("Error loading stations"));
                    errorItem.setDisable(true);
                    stationMenu.getItems().add(errorItem);
                });
            } finally {
                isLoadingStations = false;
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

    private void handleAddStation() {
        Platform.runLater(() -> {
            try {
                // Load Add Station Dialog
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/dialogs/add_station_dialog.fxml")
                );
                VBox dialogRoot = loader.load();
                
                // Get controller
                AddStationDialogController controller = loader.getController();
                
                // Create and configure stage
                javafx.stage.Stage dialogStage = new javafx.stage.Stage();
                dialogStage.setTitle("Add Station");
                dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                dialogStage.setScene(new javafx.scene.Scene(dialogRoot));
                dialogStage.setResizable(false);
                
                // Set stage reference
                controller.setStage(dialogStage);
                
                // Show dialog and wait
                dialogStage.showAndWait();
                
                // Refresh station list if successful
                if (controller.isSuccess()) {
                    logger.info("Station added, refreshing menu");
                    // Close and reopen menu with new data
                    stationMenu.hide();
                    handleMenuClick();
                }
                
            } catch (Exception e) {
                logger.error("Failed to open Add Station dialog: {}", e.getMessage(), e);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Failed to open Add Station dialog: " + e.getMessage());
                alert.showAndWait();
            }
        });
    }
}
