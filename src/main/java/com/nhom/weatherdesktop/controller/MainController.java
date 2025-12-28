package com.nhom.weatherdesktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ScrollPane;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    @FXML
    private SidebarController sidebarController;
    
    @FXML
    private TopBarController topBarController;
    
    @FXML
    private ScrollPane contentScrollPane;
    
    @FXML
    public void initialize() {
        // Connect sidebar navigation to top bar title updates
        sidebarController.setOnNavigate(this::handleNavigation);
        
        // Increase scroll speed
        configureScrollSpeed();
        
        // Load My Station screen by default
        handleNavigation("My Station");
    }
    
    private void configureScrollSpeed() {
        final double SCROLL_MULTIPLIER = 3.0;
        
        contentScrollPane.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                double vValue = contentScrollPane.getVvalue();
                double contentHeight = contentScrollPane.getContent().getBoundsInLocal().getHeight();
                double viewportHeight = contentScrollPane.getViewportBounds().getHeight();
                double scrollAmount = event.getDeltaY() * SCROLL_MULTIPLIER;
                double deltaV = scrollAmount / (contentHeight - viewportHeight);
                double newVValue = vValue - deltaV;
                newVValue = Math.max(0.0, Math.min(1.0, newVValue));
                
                contentScrollPane.setVvalue(newVValue);
                event.consume();
            }
        });
    }
    
    private void handleNavigation(String page) {
        // Update top bar title when sidebar item is clicked
        topBarController.setPageTitle(page);
        
        // Load different content based on selected page
        try {
            if ("Alerts".equals(page)) {
                logger.info("Navigating to Alerts screen");
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/screens/alert_screen.fxml")
                );
                Parent alertScreen = loader.load();
                
                // Pass sidebar controller to alert screen so it can update icon
                AlertScreenController alertController = loader.getController();
                if (alertController != null) {
                    alertController.setSidebarController(sidebarController);
                }
                
                contentScrollPane.setContent(alertScreen);
                logger.debug("Alert screen loaded successfully");
            } else if ("My Station".equals(page)) {
                logger.info("Navigating to My Station screen");
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/screens/my_station_screen.fxml")
                );
                Parent myStationScreen = loader.load();
                contentScrollPane.setContent(myStationScreen);
                logger.debug("My Station screen loaded successfully");
            } else if ("Settings".equals(page)) {
                logger.info("Navigating to Settings screen");
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/screens/settings_screen.fxml")
                );
                Parent settingsScreen = loader.load();
                contentScrollPane.setContent(settingsScreen);
                logger.debug("Settings screen loaded successfully");
            }
        } catch (Exception e) {
            logger.error("Failed to load {} screen: {}", page, e.getMessage(), e);
        }
    }
}
