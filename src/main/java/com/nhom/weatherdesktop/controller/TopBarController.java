package com.nhom.weatherdesktop.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TopBarController {

    private static final Logger logger = LoggerFactory.getLogger(TopBarController.class);

    @FXML
    private Text pageTitle;

    @FXML
    private Button profileBtn;

    @FXML
    public void initialize() {
        // Set up profile button action
        if (profileBtn != null) {
            profileBtn.setOnAction(event -> handleProfileClick());
        }
    }

    public void setPageTitle(String title) {
        pageTitle.setText(title);
    }

    private void handleProfileClick() {
        try {
            logger.info("Opening account screen");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/account.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) profileBtn.getScene().getWindow();

            // Get current window size
            double width = stage.getWidth();
            double height = stage.getHeight();

            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);

        } catch (IOException e) {
            logger.error("Error loading account screen: {}", e.getMessage(), e);
        }
    }
}