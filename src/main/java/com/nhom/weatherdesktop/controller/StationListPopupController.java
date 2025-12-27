package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.response.PageResponse;
import com.nhom.weatherdesktop.dto.response.StationResponse;
import com.nhom.weatherdesktop.service.StationService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class StationListPopupController {

    @FXML
    private ListView<HBox> stationListView;

    private final StationService stationService;
    private Stage stage;

    public StationListPopupController() {
        this.stationService = new StationService();
    }

    @FXML
    public void initialize() {
        loadStations();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void loadStations() {
        // Load stations in background thread
        new Thread(() -> {
            try {
                PageResponse<StationResponse> response = stationService.getMyStations(0, 10);
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    stationListView.getItems().clear();
                    
                    for (StationResponse station : response.content()) {
                        try {
                            // Load station item FXML
                            FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/fxml/components/station_item.fxml")
                            );
                            HBox stationItem = loader.load();
                            
                            // Set station data
                            StationItemController controller = loader.getController();
                            controller.setStationData(station);
                            
                            // Add to list
                            stationListView.getItems().add(stationItem);
                            
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    if (response.content().isEmpty()) {
                        showInfo("No stations found", "You don't have any stations yet.");
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error", "Failed to load stations: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleClose() {
        if (stage != null) {
            stage.close();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
