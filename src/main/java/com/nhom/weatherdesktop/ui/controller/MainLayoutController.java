package com.nhom.weatherdesktop.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MainLayoutController {

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
        stationListContainer.setVisible(!isVisible);
        stationListContainer.setManaged(!isVisible);
        
        // Update button text
        if (!isVisible) {
            showStationsButton.setText("🔼 Hide Station List");
        } else {
            showStationsButton.setText("📋 Show Station List");
        }
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
