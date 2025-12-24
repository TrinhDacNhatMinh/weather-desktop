package com.nhom.weatherdesktop.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages navigation between different views
 * Centralizes view switching logic instead of scattering it across controllers
 */
public class NavigationManager {
    
    private final Map<String, Node> views = new HashMap<>();
    private final Map<String, Button> navButtons = new HashMap<>();
    private String currentView;
    
    /**
     * Register a view with its corresponding navigation button
     */
    public void registerView(String viewName, Node view, Button button) {
        views.put(viewName, view);
        navButtons.put(viewName, button);
    }
    
    /**
     * Show a specific view by name
     */
    public void showView(String viewName) {
        if (!views.containsKey(viewName)) {
            throw new IllegalArgumentException("View not registered: " + viewName);
        }
        
        // Hide all views
        views.values().forEach(view -> {
            view.setVisible(false);
            view.setManaged(false);
        });
        
        // Show selected view
        Node selectedView = views.get(viewName);
        selectedView.setVisible(true);
        selectedView.setManaged(true);
        
        // Update active button
        updateActiveButton(viewName);
        
        currentView = viewName;
    }
    
    /**
     * Get currently active view name
     */
    public String getCurrentView() {
        return currentView;
    }
    
    /**
     * Update active button styling
     */
    private void updateActiveButton(String activeViewName) {
        navButtons.forEach((viewName, button) -> {
            button.getStyleClass().remove("active");
            if (viewName.equals(activeViewName)) {
                if (!button.getStyleClass().contains("active")) {
                    button.getStyleClass().add("active");
                }
            }
        });
    }
}
