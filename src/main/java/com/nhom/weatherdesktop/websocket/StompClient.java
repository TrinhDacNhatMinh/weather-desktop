package com.nhom.weatherdesktop.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhom.weatherdesktop.dto.response.AlertResponse;
import com.nhom.weatherdesktop.dto.response.WeatherDataResponse;
import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * STOMP client over WebSocket for real-time weather data and alerts
 */
public class StompClient {
    
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private static final AtomicInteger subscriptionIdCounter = new AtomicInteger(0);
    
    private WebSocketClient webSocketClient;
    private final Map<String, String> subscriptions = new HashMap<>(); // destination -> subscriptionId
    private Consumer<WeatherDataResponse> weatherDataHandler;
    private Consumer<AlertResponse> alertHandler;
    private Consumer<Boolean> connectionStatusHandler;
    private boolean connected = false;
    
    /**
     * Connect to WebSocket server
     */
    public void connect(String wsUrl) {
        try {
            // Add token as query parameter (workaround for Java-WebSocket header limitation)
            String accessToken = com.nhom.weatherdesktop.session.SessionContext.accessToken();
            if (accessToken != null && !accessToken.isBlank()) {
                String encodedToken = java.net.URLEncoder.encode(accessToken, "UTF-8");
                wsUrl = wsUrl + "?token=" + encodedToken;
            }
            
            final String finalWsUrl = wsUrl;
            webSocketClient = new WebSocketClient(new URI(finalWsUrl)) {
                
                @Override
                public void onOpen(ServerHandshake handshake) {
                    sendConnectFrame();
                }
                
                @Override
                public void onMessage(String message) {
                    handleStompMessage(message);
                }
                
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    connected = false;
                    notifyConnectionStatus(false);
                }
                
                @Override
                public void onError(Exception ex) {
                    // Error is silently handled
                }
            };
            
            webSocketClient.connect();
            
        } catch (Exception e) {
            // Connection error is silently handled
        }
    }
    
    /**
     * Send STOMP CONNECT frame with authentication
     */
    private void sendConnectFrame() {
        String accessToken = com.nhom.weatherdesktop.session.SessionContext.accessToken();
        
        StompFrame connectFrame = new StompFrame("CONNECT")
                .header("accept-version", "1.2")
                .header("host", "localhost");
        
        if (accessToken != null && !accessToken.isBlank()) {
            connectFrame.header("Authorization", "Bearer " + accessToken);
        }
        
        send(connectFrame.encode());
    }
    
    /**
     * Subscribe to a STOMP topic
     */
    public void subscribe(String destination, String type) {
        if (!connected) {
            return;
        }
        
        String subscriptionId = "sub-" + subscriptionIdCounter.getAndIncrement();
        subscriptions.put(destination, subscriptionId);
        
        StompFrame subscribeFrame = new StompFrame("SUBSCRIBE")
                .header("id", subscriptionId)
                .header("destination", destination);
        
        send(subscribeFrame.encode());
    }
    
    /**
     * Unsubscribe from a STOMP topic
     */
    public void unsubscribe(String destination) {
        String subscriptionId = subscriptions.remove(destination);
        if (subscriptionId == null) {
            return;
        }
        
        StompFrame unsubscribeFrame = new StompFrame("UNSUBSCRIBE")
                .header("id", subscriptionId);
        
        send(unsubscribeFrame.encode());
    }
    
    /**
     * Handle incoming STOMP messages
     */
    private void handleStompMessage(String message) {
        StompFrame frame = StompFrame.decode(message);
        if (frame == null) {
            return;
        }
        
        switch (frame.getCommand()) {
            case "CONNECTED":
                connected = true;
                notifyConnectionStatus(true);
                break;
                
            case "MESSAGE":
                handleMessageFrame(frame);
                break;
                
            case "ERROR":
                break;
                
            default:
                break;
        }
    }
    
    /**
     * Handle MESSAGE frame - parse weather data or alert
     */
    private void handleMessageFrame(StompFrame frame) {
        String destination = frame.getHeader("destination");
        String body = frame.getBody();
        
        if (destination == null || body == null) {
            return;
        }
        
        try {
            if (destination.contains("/weather")) {
                WeatherDataResponse weatherData = MAPPER.readValue(body, WeatherDataResponse.class);
                notifyWeatherData(weatherData);
                
            } else if (destination.contains("/alerts")) {
                AlertResponse alert = MAPPER.readValue(body, AlertResponse.class);
                notifyAlert(alert);
            }
            
        } catch (Exception e) {
            // Message parsing error is silently handled
        }
    }
    
    /**
     * Send raw message to WebSocket
     */
    private void send(String message) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(message);
        }
    }
    
    /**
     * Disconnect from WebSocket
     */
    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
        }
        connected = false;
        subscriptions.clear();
    }
    
    // ========== Handlers ==========
    
    public void setWeatherDataHandler(Consumer<WeatherDataResponse> handler) {
        this.weatherDataHandler = handler;
    }
    
    public void setAlertHandler(Consumer<AlertResponse> handler) {
        this.alertHandler = handler;
    }
    
    public void setConnectionStatusHandler(Consumer<Boolean> handler) {
        this.connectionStatusHandler = handler;
    }
    
    private void notifyWeatherData(WeatherDataResponse data) {
        if (weatherDataHandler != null) {
            Platform.runLater(() -> weatherDataHandler.accept(data));
        }
    }
    
    private void notifyAlert(AlertResponse alert) {
        if (alertHandler != null) {
            Platform.runLater(() -> alertHandler.accept(alert));
        }
    }
    
    private void notifyConnectionStatus(boolean status) {
        if (connectionStatusHandler != null) {
            Platform.runLater(() -> connectionStatusHandler.accept(status));
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
}
