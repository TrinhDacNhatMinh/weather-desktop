package com.nhom.weatherdesktop;

import com.nhom.weatherdesktop.util.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WeatherDesktopApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Load login FXML
        FXMLLoader fxmlLoader = new FXMLLoader(WeatherDesktopApplication.class.getResource("/fxml/auth/login.fxml"));
        
        // Create scene with minimum size
        Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
        
        // Configure stage
        AppConfig config = AppConfig.getInstance();
        stage.setTitle(config.getAppTitle());
        stage.setScene(scene);
        stage.setMaximized(true);  // Maximize window (still shows title bar)
        
        // Show the stage
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}