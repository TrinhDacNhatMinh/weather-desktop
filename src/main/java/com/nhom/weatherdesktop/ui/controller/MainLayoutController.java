package com.nhom.weatherdesktop.ui.controller;

import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.service.StationService;
import com.nhom.weatherdesktop.service.ThresholdService;
import com.nhom.weatherdesktop.ui.component.StationCard;
import com.nhom.weatherdesktop.ui.controller.dialog.AddStationDialogController;
import com.nhom.weatherdesktop.ui.controller.dialog.UpdateStationDialogController;
import com.nhom.weatherdesktop.util.AlertService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
        new AddStationDialogController(stationService)
            .setOnSuccess(this::loadStations)
            .showDialog(sidebar.getScene().getWindow());
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
            StationCard stationCard = new StationCard(station);
            stationCard.setOnEdit(this::handleUpdateStation);
            stationCard.setOnDetach(this::handleDetachStation);
            stationList.getChildren().add(stationCard);
        }
    }
    
    private void handleUpdateStation(StationResponse station) {
        new UpdateStationDialogController(stationService, thresholdService)
            .setOnSuccess(this::loadStations)
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
}
