package com.nhom.weatherdesktop.controller;

import com.nhom.weatherdesktop.dto.response.StationResponse;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StationItemController {

    private static final Logger logger = LoggerFactory.getLogger(StationItemController.class);

    @FXML
    private HBox root;

    @FXML
    private Text stationName;

    @FXML
    private Text stationLocation;

    @FXML
    private Text statusIcon;
    
    @FXML
    private Text statusText;
    
    @FXML
    private HBox actionButtons;

    private StationResponse station;

    @FXML
    public void initialize() {
        // Add hover listeners to show/hide action buttons
        if (root != null && actionButtons != null) {
            root.setOnMouseEntered(e -> {
                actionButtons.setVisible(true);
                actionButtons.setManaged(true);
            });
            
            root.setOnMouseExited(e -> {
                actionButtons.setVisible(false);
                actionButtons.setManaged(false);
            });
        }
    }

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

    @FXML
    private void handleEdit() {
        logger.info("Edit station: {}", station.name());
        
        try {
            // Load Edit Station Dialog
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/dialogs/edit_station_dialog.fxml")
            );
            javafx.scene.layout.VBox dialogRoot = loader.load();
            
            // Get controller and set station data
            EditStationDialogController controller = loader.getController();
            controller.setStation(station);
            
            // Create and configure stage
            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle("Edit Station");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setScene(new javafx.scene.Scene(dialogRoot, 520, 600));
            dialogStage.setResizable(false);
            
            // Set stage reference
            controller.setStage(dialogStage);
            
            // Show dialog and wait
            dialogStage.showAndWait();
            
            // If edit was successful, station list will be refreshed when menu is reopened
            if (controller.isSuccess()) {
                logger.info("Station edited successfully");
            }
            
        } catch (Exception e) {
            logger.error("Failed to open edit dialog: {}", e.getMessage(), e);
        }
    }

    @FXML
    private void handleDelete() {
        logger.info("Delete station requested: {}", station.name());
        
        // Show confirmation dialog
        javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.CONFIRMATION
        );
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to remove \"" + station.name() + "\" from your station list?");
        
        confirmAlert.getButtonTypes().setAll(
            javafx.scene.control.ButtonType.OK,
            javafx.scene.control.ButtonType.CANCEL
        );
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // User confirmed, delete station
                new Thread(() -> {
                    try {
                        logger.debug("Detaching station: id={}, name={}", station.id(), station.name());
                        new com.nhom.weatherdesktop.service.StationService().detachStationFromUser(station.id());
                        
                        javafx.application.Platform.runLater(() -> {
                            logger.info("Station detached successfully! Station: {}, ID: {}", 
                                station.name(), station.id());
                            
                            // Show success message
                            javafx.scene.control.Alert successAlert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.INFORMATION
                            );
                            successAlert.setTitle("Success");
                            successAlert.setHeaderText(null);
                            successAlert.setContentText("Station removed successfully!");
                            successAlert.show();
                        });
                        
                    } catch (Exception e) {
                        logger.error("Failed to detach station: {}", e.getMessage(), e);
                        javafx.application.Platform.runLater(() -> {
                            javafx.scene.control.Alert errorAlert = new javafx.scene.control.Alert(
                                javafx.scene.control.Alert.AlertType.ERROR
                            );
                            errorAlert.setTitle("Error");
                            errorAlert.setHeaderText(null);
                            errorAlert.setContentText("Failed to delete station: " + e.getMessage());
                            errorAlert.showAndWait();
                        });
                    }
                }).start();
            } else {
                logger.debug("Station delete cancelled by user");
            }
        });
    }

    public StationResponse getStation() {
        return station;
    }
}
