package com.nhom.weatherdesktop.util;

/**
 * Singleton class to store current user session information
 */
public class UserSession {
    private static UserSession instance;
    
    private String name;
    private String email;
    
    private UserSession() {
    }
    
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    
    public void setUserInfo(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public boolean isActive() {
        return name != null && email != null;
    }
    
    public void clear() {
        name = null;
        email = null;
    }
}
