package com.nhom.weatherdesktop.ui.component;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MapLocationPicker {
    
    private final DoubleProperty latitude = new SimpleDoubleProperty(21.0285); // Hanoi default
    private final DoubleProperty longitude = new SimpleDoubleProperty(105.8542);
    private String address = "";
    
    public MapLocationPicker() {
    }
    
    public void showMapPicker(Stage owner) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Select Location on Map");
        dialog.setHeaderText("Click on the map to select a location");
        
        // Buttons
        ButtonType selectButtonType = new ButtonType("Select Location", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);
        
        // WebView setup
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webView.setPrefSize(800, 600);
        
        // Info label
        Label coordsLabel = new Label("Selected: Lat 21.0285, Lng 105.8542");
        coordsLabel.setStyle("-fx-font-size: 12px; -fx-padding: 8;");
        
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(webView, coordsLabel);
        
        dialog.getDialogPane().setContent(content);
        
        // Load Google Maps HTML
        String htmlContent = getGoogleMapsHtml();
        webEngine.loadContent(htmlContent);
        
        // Listen for coordinates from JavaScript
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                // Setup bridge to receive coordinates from JavaScript
                webEngine.executeScript(
                    "window.javaApp = {" +
                    "  updateCoords: function(lat, lng) {" +
                    "    document.title = lat + ',' + lng;" +
                    "  }" +
                    "};"
                );
                
                // Listen for title changes (coordinates only, address from Java)
                webEngine.titleProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null && newValue.contains(",")) {
                        try {
                            String[] coords = newValue.split(",");
                            double lat = Double.parseDouble(coords[0]);
                            double lng = Double.parseDouble(coords[1]);
                            latitude.set(lat);
                            longitude.set(lng);
                            
                            // Call Java reverse geocoding
                            reverseGeocode(lat, lng, coordsLabel);
                            
                            // Initial label update (will be replaced by address when loaded)
                            coordsLabel.setText(String.format("Loading address..."));
                        } catch (Exception e) {
                            // Ignore parse errors
                        }
                    }
                });
            }
        });
        
        dialog.showAndWait();
        
        // Fetch address synchronously if not already loaded
        if (address.isEmpty() && latitude.get() != 0 && longitude.get() != 0) {
            address = reverseGeocodeSync(latitude.get(), longitude.get());
        }
    }
    
    // Reverse geocoding using Nominatim API (synchronous)
    private String reverseGeocodeSync(double lat, double lng) {
        try {
            String url = String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%.6f&lon=%.6f", lat, lng);
            
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("User-Agent", "WeatherDesktopApp/1.0")
                .GET()
                .build();
            
            java.net.http.HttpResponse<String> response = client.send(request, 
                java.net.http.HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));
            
            if (response.statusCode() == 200) {
                // Simple JSON parsing for display_name
                String body = response.body();
                int startIdx = body.indexOf("\"display_name\":\"");
                if (startIdx != -1) {
                    startIdx += 16; // length of "display_name":"
                    int endIdx = body.indexOf("\"", startIdx);
                    if (endIdx != -1) {
                        String foundAddress = body.substring(startIdx, endIdx);
                        // Unescape JSON unicode sequences
                        foundAddress = unescapeJson(foundAddress);
                        return foundAddress;
                    }
                }
            }
        } catch (Exception e) {
            // Silent fail for reverse geocoding
        }
        return "";
    }
    
    // Reverse geocoding using Nominatim API (async for UI updates)
    private void reverseGeocode(double lat, double lng, Label coordsLabel) {
        new Thread(() -> {
            String foundAddress = reverseGeocodeSync(lat, lng);
            if (!foundAddress.isEmpty()) {
                address = foundAddress;
                
                // Update label on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    coordsLabel.setText(String.format("📍 %s", address));
                });
            }
        }).start();
    }
    
    // Unescape JSON string
    private String unescapeJson(String str) {
        str = str.replace("\\/", "/");
        // Handle unicode escape sequences like uXXXX
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            if (str.charAt(i) == '\\' && i + 1 < str.length()) {
                char next = str.charAt(i + 1);
                if (next == 'u' && i + 5 < str.length()) {
                    // Unicode escape
                    String hex = str.substring(i + 2, i + 6);
                    try {
                        int code = Integer.parseInt(hex, 16);
                        result.append((char) code);
                        i += 6;
                        continue;
                    } catch (NumberFormatException e) {
                        // Not a valid unicode escape, keep as is
                    }
                }
            }
            result.append(str.charAt(i));
            i++;
        }
        return result.toString();
    }
    
    private String getGoogleMapsHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                <style>
                    #map {
                        height: 100%;
                        width: 100%;
                    }
                    html, body {
                        height: 100%;
                        margin: 0;
                        padding: 0;
                    }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    // Default location: Hanoi
                    const defaultLat = 21.0285;
                    const defaultLng = 105.8542;
                    
                    // Initialize map
                    const map = L.map('map').setView([defaultLat, defaultLng], 12);
                    
                    // Add OpenStreetMap tiles
                    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap contributors',
                        maxZoom: 19
                    }).addTo(map);
                    
                    // Add marker
                    let marker = L.marker([defaultLat, defaultLng], {
                        draggable: true
                    }).addTo(map);
                    
                    // Update coordinates function
                    function updateCoordinates(lat, lng) {
                        if (window.javaApp && window.javaApp.updateCoords) {
                            window.javaApp.updateCoords(lat, lng);
                        }
                        document.title = lat + ',' + lng;
                    }
                    
                    // Click on map to move marker
                    map.on('click', function(e) {
                        marker.setLatLng(e.latlng);
                        updateCoordinates(e.latlng.lat, e.latlng.lng);
                    });
                    
                    // Drag marker event
                    marker.on('dragend', function(e) {
                        const position = e.target.getLatLng();
                        updateCoordinates(position.lat, position.lng);
                    });
                    
                    // Send initial coordinates
                    updateCoordinates(defaultLat, defaultLng);
                </script>
            </body>
            </html>
            """;
    }
    
    public double getLatitude() {
        return latitude.get();
    }
    
    public double getLongitude() {
        return longitude.get();
    }
    
    public String getAddress() {
        return address;
    }
    
    public DoubleProperty latitudeProperty() {
        return latitude;
    }
    
    public DoubleProperty longitudeProperty() {
        return longitude;
    }
}
