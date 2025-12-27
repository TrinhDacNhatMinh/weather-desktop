package com.nhom.weatherdesktop.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;

public class MainController {
    
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
        
        // Future: Load different content based on selected page
        // For now, just updating the title
    }
}
