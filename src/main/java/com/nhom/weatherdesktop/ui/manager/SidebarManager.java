package com.nhom.weatherdesktop.ui.manager;

import javafx.animation.TranslateTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import static com.nhom.weatherdesktop.config.UIConstants.*;

/**
 * Manages sidebar collapse/expand functionality
 */
public class SidebarManager {
    
    private final VBox sidebar;
    private final Button toggleButton;
    private final Label myStationText;
    private final Label alertsText;
    private final Label settingsText;
    
    private boolean isCollapsed = false;
    
    public SidebarManager(
        VBox sidebar,
        Button toggleButton,
        Label myStationText,
        Label alertsText,
        Label settingsText
    ) {
        this.sidebar = sidebar;
        this.toggleButton = toggleButton;
        this.myStationText = myStationText;
        this.alertsText = alertsText;
        this.settingsText = settingsText;
    }
    
    /**
     * Toggle sidebar between collapsed and expanded states
     */
    public void toggle() {
        isCollapsed = !isCollapsed;
        
        if (isCollapsed) {
            collapse();
        } else {
            expand();
        }
    }
    
    private void collapse() {
        sidebar.setPrefWidth(SIDEBAR_COLLAPSED_WIDTH);
        sidebar.setMaxWidth(SIDEBAR_COLLAPSED_WIDTH);
        
        // Hide text labels
        myStationText.setVisible(false);
        myStationText.setManaged(false);
        alertsText.setVisible(false);
        alertsText.setManaged(false);
        settingsText.setVisible(false);
        settingsText.setManaged(false);
    }
    
    private void expand() {
        sidebar.setPrefWidth(SIDEBAR_EXPANDED_WIDTH);
        sidebar.setMaxWidth(SIDEBAR_EXPANDED_WIDTH);
        
        // Show text labels
        myStationText.setVisible(true);
        myStationText.setManaged(true);
        alertsText.setVisible(true);
        alertsText.setManaged(true);
        settingsText.setVisible(true);
        settingsText.setManaged(true);
    }
    
    public boolean isCollapsed() {
        return isCollapsed;
    }
}
