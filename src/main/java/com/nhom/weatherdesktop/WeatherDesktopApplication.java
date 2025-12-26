package com.nhom.weatherdesktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WeatherDesktopApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(WeatherDesktopApplication.class.getResource("/ui/view/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        stage.setTitle("Weather Desktop");
        stage.setResizable(true); // Ensure window is resizable
        stage.setScene(scene);
        stage.setMaximized(true); // Start maximized for full screen effect
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}