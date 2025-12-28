package com.nhom.weatherdesktop.controller;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * Controller for the Map Picker dialog
 * Displays an interactive OpenStreetMap using Leaflet.js
 * Allows user to click on map to select a location
 */
public class MapPickerController {
    
    private static final Logger logger = LoggerFactory.getLogger(MapPickerController.class);
    
    @FXML
    private WebView mapWebView;
    
    private Stage stage;
    private Double selectedLatitude;
    private Double selectedLongitude;
    private boolean confirmed = false;
    
    @FXML
    public void initialize() {
        WebEngine engine = mapWebView.getEngine();
        
        // Load map HTML
        URL mapUrl = getClass().getResource("/html/map_picker.html");
        if (mapUrl != null) {
            logger.debug("Loading map from: {}", mapUrl);
            engine.load(mapUrl.toExternalForm());
        } else {
            logger.error("Could not find map_picker.html");
        }
        
        // Setup JavaScript bridge when page is loaded
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                logger.debug("Map loaded successfully");
                setupJavaScriptBridge(engine);
            }
        });
    }
    
    private void setupJavaScriptBridge(WebEngine engine) {
        try {
            // Get window object
            JSObject window = (JSObject) engine.executeScript("window");
            
            // Create bridge object that JavaScript can call
            JavaScriptBridge bridge = new JavaScriptBridge();
            window.setMember("javaApp", bridge);
            
            logger.debug("JavaScript bridge setup complete");
        } catch (Exception e) {
            logger.error("Failed to setup JavaScript bridge: {}", e.getMessage(), e);
        }
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public Double getSelectedLatitude() {
        return selectedLatitude;
    }
    
    public Double getSelectedLongitude() {
        return selectedLongitude;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
    
    /**
     * Set initial location for the map (used when editing existing station)
     * @param lat Latitude
     * @param lng Longitude
     */
    public void setInitialLocation(double lat, double lng) {
        this.selectedLatitude = lat;
        this.selectedLongitude = lng;
        
        // Set map center via JavaScript after map is loaded
        WebEngine engine = mapWebView.getEngine();
        if (engine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            try {
                String script = String.format("if (typeof setMapCenter === 'function') { setMapCenter(%f, %f); }", lat, lng);
                engine.executeScript(script);
                logger.debug("Set initial map location: lat={}, lng={}", lat, lng);
            } catch (Exception e) {
                logger.warn("Failed to set initial map center: {}", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleConfirm() {
        if (selectedLatitude != null && selectedLongitude != null) {
            logger.info("Location confirmed: lat={}, lng={}", selectedLatitude, selectedLongitude);
            confirmed = true;
            closeDialog();
        } else {
            logger.warn("No location selected");
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.WARNING
            );
            alert.setTitle("No Location Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please click on the map to select a location.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void handleCancel() {
        logger.debug("Map picker cancelled");
        confirmed = false;
        closeDialog();
    }
    
    private void closeDialog() {
        if (stage != null) {
            stage.close();
        }
    }
    
    /**
     * Bridge class that JavaScript can call
     * JavaScript calls: window.javaApp.onLocationSelected(lat, lng)
     */
    public class JavaScriptBridge {
        public void onLocationSelected(double lat, double lng) {
            logger.debug("Location selected from map: lat={}, lng={}", lat, lng);
            selectedLatitude = lat;
            selectedLongitude = lng;
        }
    }
}
