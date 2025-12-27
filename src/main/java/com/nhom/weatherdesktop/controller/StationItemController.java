package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.response.StationResponse;
import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class StationItemController {

    @FXML
    private Text stationName;

    @FXML
    private Text stationLocation;

    @FXML
    private Text statusIcon;
    
    @FXML
    private Text statusText;

    private StationResponse station;

    public void setStationData(StationResponse station) {
        this.station = station;
        
        // Set station name
        stationName.setText(station.name());
        
        // Set location
        stationLocation.setText(station.location());
        
        // Set status icon and text based on status field
        if ("ON".equalsIgnoreCase(station.status())) {
            // Green circle for ON status
            statusIcon.setStyle("-fx-fill: #10B981; -fx-font-size: 12px;");
            statusText.setText("on");
        } else {
            // Red circle for OFF status
            statusIcon.setStyle("-fx-fill: #EF4444; -fx-font-size: 12px;");
            statusText.setText("off");
        }
    }

    public StationResponse getStation() {
        return station;
    }
}
