package com.nhom.weatherdesktop.controller;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class TopBarController {
    
    @FXML
    private Text pageTitle;
    
    public void setPageTitle(String title) {
        pageTitle.setText(title);
    }
}
