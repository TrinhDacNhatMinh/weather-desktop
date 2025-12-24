package com.nhom.weatherdesktop.model;

import java.time.Instant;

public class Alert {
    
    public enum Status {
        NEW,
        SEEN
    }
    
    private Long id;
    private String message;
    private Status status;
    private Instant createdAt;
    private Long weatherDataId;
    private Long stationId;
    private String stationName;
    
    public Alert(Long id, String message, String statusStr, Instant createdAt,
                 Long weatherDataId, Long stationId, String stationName) {
        this.id = id;
        this.message = message;
        this.status = "SEEN".equals(statusStr) ? Status.SEEN : Status.NEW;
        this.createdAt = createdAt;
        this.weatherDataId = weatherDataId;
        this.stationId = stationId;
        this.stationName = stationName;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getMessage() { return message; }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Long getWeatherDataId() { return weatherDataId; }
    public Long getStationId() { return stationId; }
    public String getStationName() { return stationName; }
    
    // Setters
    public void setStatus(Status status) { this.status = status; }
    
    public boolean isNew() {
        return status == Status.NEW;
    }
    
    public void markAsSeen() {
        this.status = Status.SEEN;
    }
    
    // Factory method to create from AlertResponse
    public static Alert fromResponse(com.nhom.weatherdesktop.dto.response.AlertResponse response) {
        return new Alert(
            response.id(),
            response.message(),
            response.status(),
            response.createdAt(),
            response.weatherDataId(),
            response.stationId(),
            response.stationName()
        );
    }
}
