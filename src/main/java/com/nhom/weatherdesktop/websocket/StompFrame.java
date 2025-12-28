package com.nhom.weatherdesktop.websocket;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a STOMP protocol frame
 */
public class StompFrame {
    
    private final String command;
    private final Map<String, String> headers;
    private String body;
    
    public StompFrame(String command) {
        this.command = command;
        this.headers = new HashMap<>();
        this.body = "";
    }
    
    public StompFrame header(String key, String value) {
        headers.put(key, value);
        return this;
    }
    
    public StompFrame body(String body) {
        this.body = body;
        return this;
    }
    
    /**
     * Convert to STOMP protocol string
     */
    public String encode() {
        StringBuilder sb = new StringBuilder();
        sb.append(command).append("\n");
        
        headers.forEach((key, value) -> 
            sb.append(key).append(":").append(value).append("\n")
        );
        
        sb.append("\n");
        if (body != null && !body.isEmpty()) {
            sb.append(body);
        }
        sb.append("\0"); // NULL terminator
        
        return sb.toString();
    }
    
    /**
     * Parse STOMP frame from string
     */
    public static StompFrame decode(String rawFrame) {
        if (rawFrame == null || rawFrame.isEmpty()) {
            return null;
        }
        
        // Remove NULL terminator
        rawFrame = rawFrame.replace("\0", "");
        
        String[] lines = rawFrame.split("\n");
        if (lines.length == 0) {
            return null;
        }
        
        // First line is command
        StompFrame frame = new StompFrame(lines[0]);
        
        int i = 1;
        // Parse headers until empty line
        for (; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                i++;
                break;
            }
            
            int colonIndex = lines[i].indexOf(':');
            if (colonIndex > 0) {
                String key = lines[i].substring(0, colonIndex);
                String value = lines[i].substring(colonIndex + 1);
                frame.header(key, value);
            }
        }
        
        // Remaining is body
        if (i < lines.length) {
            StringBuilder bodyBuilder = new StringBuilder();
            for (; i < lines.length; i++) {
                bodyBuilder.append(lines[i]);
                if (i < lines.length - 1) {
                    bodyBuilder.append("\n");
                }
            }
            frame.body(bodyBuilder.toString());
        }
        
        return frame;
    }
    
    public String getCommand() {
        return command;
    }
    
    public String getHeader(String key) {
        return headers.get(key);
    }
    
    public String getBody() {
        return body;
    }
    
    @Override
    public String toString() {
        return "StompFrame{" +
                "command='" + command + '\'' +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
}
